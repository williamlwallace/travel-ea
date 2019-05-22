package controllers.backend;

import actions.ActionState;
import actions.Authenticator;
import actions.roles.Everyone;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import models.Photo;
import org.joda.time.DateTime;
import play.libs.Files;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import play.routing.JavaScriptReverseRouter;
import repository.PhotoRepository;
import repository.DestinationRepository;
import util.customObjects.Pair;
import util.validation.ErrorResponse;

public class PhotoController extends TEABackController {

    // Constant fields defining the directories of regular photos and test photos
    private static final String PHOTO_DIRECTORY = "/storage/photos/";
    private static final String TEST_PHOTO_DIRECTORY = "/storage/photos/test/";

    // Constant fields defining the directory of publicly available files
    private static final String PUBLIC_DIRECTORY = "/public";

    private String savePath = "";

    // Default dimensions of thumbnail images
    private static final int THUMB_WIDTH = 400;
    private static final int THUMB_HEIGHT = 266;

    // Repositories to handle DB transactions
    private PhotoRepository photoRepository;
    private DestinationRepository destinationRepository;

    private play.Environment environment;

    @Inject
    public PhotoController(DestinationRepository destinationRepository, PhotoRepository photoRepository, play.Environment environment) {
        this.destinationRepository = destinationRepository;
        this.photoRepository = photoRepository;
        this.environment = environment;

        savePath = ((environment.isProd()) ? "/home/sengstudent" : System.getProperty("user.dir")) + PUBLIC_DIRECTORY;
    }

    /**
     * Takes a path to a file and returns the file object
     * 
     * @param filePath path to file to read
     */
    public Result getPhotoFromPath(String filePath) {
        File file = new File(filePath);
        return ok(file, true);
    }

    /**
     * Returns all photos belonging to a user, only returns public photos if it is not the owner of the photos getting them
     * 
     * @param request Request to read cookie data from
     * @param id ID of user to get photos of
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> getAllUserPhotos(Http.Request request, Long id) {
        Long currentUserId = request.attrs().get(ActionState.USER).id;

        // Checks if the photos belong to the user getting them
        if (currentUserId.equals(id)) {
            // get public and private photos
            return photoRepository.getAllUserPhotos(id)
                .thenApplyAsync(photos -> ok(Json.toJson(photos)));
        } else {
            // only get public photos
            return photoRepository.getAllPublicUserPhotos(id)
                .thenApplyAsync(photos -> ok(Json.toJson(photos)));
        }
    }

    /**
     * Gets the profile picture of user with given id
     * 
     * @param id ID of user to get profile photo of
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> getProfilePicture(Long id) {
        return photoRepository.getUserProfilePicture(id)
            .thenApplyAsync(photo -> {
                if (photo == null) {
                    return notFound(Json.toJson("No profile picture found for user"));
                } else {
                    return ok(Json.toJson(photo));
                }
            });
    }

    /**
     * Uploads any number of photos from a multipart/form-data request
     *
     * @param request Request where body is a multipart form-data
     * @return Result of query
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> upload(Http.Request request) {
        return CompletableFuture.supplyAsync(() -> {
            // Get the request body, and turn it into a multipart form data collection of temporary files
            Http.MultipartFormData<Files.TemporaryFile> body = request.body().asMultipartFormData();

            // Get all basic string keys in multipart form
            Map<String, String[]> formKeys = body.asFormUrlEncoded();

            // Store in a boolean whether or not this is a test file
            boolean isTest = Boolean
                .parseBoolean(formKeys.getOrDefault("isTest", new String[]{"false"})[0]);

            // Keep track of which file should be uploaded as a profile
            String profilePhotoFilename = formKeys
                .getOrDefault("profilePhotoName", new String[]{null})[0];

            // Keep track of which photos are marked as public
            HashSet<String> publicPhotoFileNames = new HashSet<>(Arrays.asList(
                formKeys.getOrDefault("publicPhotoFileNames", new String[]{""})[0].split(",")));

            // Store photos in a list to allow them all to be uploaded at the end if all are read successfully
            ArrayList<Pair<Photo, Http.MultipartFormData.FilePart<Files.TemporaryFile>>> photos = new ArrayList<>();

            // Iterate through all files in the request
            for (Http.MultipartFormData.FilePart<Files.TemporaryFile> file : body.getFiles()) {
                if (file != null) {
                    try {
                        // Store file with photo in list to be added later
                        photos.add(new Pair<>(
                            readFileToPhoto(file, profilePhotoFilename, publicPhotoFileNames,
                                request.attrs().get(ActionState.USER).id, isTest), file));
                    } catch (IOException e) {
                        // If an invalid file type given, return bad request with error message generated in exception
                        return badRequest(e.getMessage());
                    }
                } else {
                    // If any uploads fail, return bad request immediately
                    return badRequest("Missing file");
                }
            }

            // If no photos were actually found, and no other error has been thrown, throw it now
            if (photos.isEmpty()) {
                return badRequest("No files given");
            } else {
                saveMultiplePhotos(photos);
            }

            // Return OK if no issues were encountered
            return status(201, Json.toJson("File(s) uploaded successfully"));
        });
    }

    /**
     * Saves multiple photo files to storage folder, and inserts reference to them to database
     *
     * @param photos Collection of pairs of <Photo, Http.MultipartFormData.FilePart<Files.TemporaryFile>>
     */
    private void saveMultiplePhotos(
        Collection<Pair<Photo, Http.MultipartFormData.FilePart<Files.TemporaryFile>>> photos) {
        // Add all the photos we found to the database
        int thumbWidth = THUMB_WIDTH;
        int thumbHeight = THUMB_HEIGHT;
        for (Pair<Photo, Http.MultipartFormData.FilePart<Files.TemporaryFile>> pair : photos) {
            // if photo to add is marked as new profile pic, clear any existing profile pic first
            if (pair.getKey().isProfile) {
                photoRepository.clearProfilePhoto(pair.getKey().userId).thenApply(fileNamesPair -> {
                    if (fileNamesPair != null) {
                        File thumbFile = new File(fileNamesPair.getKey());
                        File mainFile = new File(fileNamesPair.getValue());
                        // Mark the files for deletion
                        if (!thumbFile.delete()) {
                            // If file fails to delete immediately, mark file for deletion when VM shuts down
                            thumbFile.deleteOnExit();
                        }
                        if (!mainFile.delete()) {
                            // If file fails to delete immediately, mark file for deletion when VM shuts down
                            mainFile.deleteOnExit();
                        }
                    }
                    return null;
                });
                // Profile picture small thumbnail dimensions
                thumbWidth = 100;
                thumbHeight = 100;
            }
            try {
                pair.getValue().getRef()
                    .copyTo(Paths.get(pair.getKey().filename), true);
                createThumbnailFromFile(pair.getValue().getRef(), thumbWidth, thumbHeight)
                    .copyTo(Paths.get(pair.getKey().thumbnailFilename));
            } catch (IOException e) {
                // TODO: Handle case where a file failed to save
            }
        }
        // Collect all keys from the list to upload
        photoRepository.addPhotos(photos.stream().map(Pair::getKey).collect(Collectors.toList()));
    }

    /**
     * Reads a file part from the multipart form and returns a Photo object to add to the database
     *
     * @param file File part from form
     * @param profilePhotoFilename Name (if any) of photo to be set as profile picture
     * @param publicPhotoFileNames Names (if any) of photos to be set to public, defaults to private
     * if referenced here
     * @param userId ID of user who is uploading the files
     * @param isTest Whether or not these photos should be added to test folder of storage
     * @return Photo object to be added to database
     * @throws IOException Thrown when an unsupported filetype added (i.e not image/jpeg or
     * image/png)
     */
    private Photo readFileToPhoto(Http.MultipartFormData.FilePart<Files.TemporaryFile> file,
        String profilePhotoFilename, HashSet<String> publicPhotoFileNames, long userId,
        boolean isTest) throws IOException {
        // Get the filename, file size and content-type of the file
        String fileName = System.currentTimeMillis() + "_" + file.getFilename();

        String contentType = file.getContentType();
        if (!contentType.equals("image/jpeg") && !contentType.equals("image/png")) {
            // If any file is found that is not a png or jpeg reject all files with error message
            throw new IOException("Invalid file type given: " + contentType);
        }

        // Create a photo object
        Photo photo = new Photo();
        photo.filename = (savePath + ((isTest) ? TEST_PHOTO_DIRECTORY : PHOTO_DIRECTORY) + fileName);
        photo.isProfile =
            profilePhotoFilename != null && profilePhotoFilename.equals(file.getFilename());
        photo.isPublic = publicPhotoFileNames.contains(file.getFilename()) || photo.isProfile;
        photo.thumbnailFilename = (savePath + ((isTest) ? TEST_PHOTO_DIRECTORY : PHOTO_DIRECTORY)
            + "thumbnails/" + fileName);
        photo.uploaded = DateTime.now();
        photo.userId = userId;

        // Return the created photo object
        return photo;
    }

    /**
     * Creates a new image at default thumbnail size of a given image
     *
     * @param fullImageFile The full image to create a filename for
     * @return Temporary file of thumbnail
     */
    private Files.TemporaryFile createThumbnailFromFile(Files.TemporaryFile fullImageFile,
        int thumbWidth, int thumbHeight) throws IOException {
        // Convert full image file to java.awt image
        BufferedImage fullImage = ImageIO.read(fullImageFile.path().toFile());

        BufferedImage tThumbImage = new BufferedImage(thumbWidth, thumbHeight,
            BufferedImage.TYPE_INT_RGB);
        Graphics2D tGraphics2D = tThumbImage
            .createGraphics(); //create a graphics object to paint to
        tGraphics2D.setBackground(Color.WHITE);
        tGraphics2D.setPaint(Color.WHITE);
        tGraphics2D.fillRect(0, 0, thumbWidth, thumbHeight);
        tGraphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        //tGraphics2D.drawImage( fullImage, 0, 0, THUMB_WIDTH, THUMB_HEIGHT, null ); //draw the image scaled
        // If image is smaller than thumbnail size then center with bars on each side
        if (fullImage.getWidth() < thumbWidth && fullImage.getHeight() < thumbHeight) {
            tGraphics2D.drawImage(fullImage, thumbWidth / 2 - fullImage.getWidth() / 2,
                thumbHeight / 2 - fullImage.getHeight() / 2, fullImage.getWidth(),
                fullImage.getHeight(), null);
        } // Otherwise scale image down so biggest side is set to max of thumbnail and rest is scaled proportionally
        else {
            // Determine which side is proportionally bigger
            boolean fitWidth =
                fullImage.getWidth() / thumbWidth > fullImage.getHeight() / thumbHeight;
            double scaleFactor = (fitWidth) ? (double) thumbWidth / (double) fullImage.getWidth()
                : (double) thumbHeight / (double) fullImage.getHeight();
            if (fitWidth) {
                int newHeight = (int) Math.floor(fullImage.getHeight() * scaleFactor);
                tGraphics2D
                    .drawImage(fullImage, 0, thumbHeight / 2 - newHeight / 2, thumbWidth, newHeight,
                        null);
            } else {
                int newWidth = (int) Math.floor(fullImage.getWidth() * scaleFactor);
                tGraphics2D
                    .drawImage(fullImage, thumbWidth / 2 - newWidth / 2, 0, newWidth, thumbHeight,
                        null);
            }
        }

        // Create file to store output of thumbnail write
        File thumbFile = new File(savePath + TEST_PHOTO_DIRECTORY + "tempThumb.jpg");

        // Write buffered image to thumbnail file
        ImageIO.write(tThumbImage, "jpg", thumbFile);

        // Return temporary file created from the file
        Files.TemporaryFile temporaryFile = (new Files.SingletonTemporaryFileCreator())
            .create(thumbFile.toPath());

        // Delete the file that it was buffered to
        thumbFile.deleteOnExit();

        return temporaryFile;
    }

    /**
     * Deletes a photo with given id. Return a result with a json int which represents the number of
     * rows that were deleted. So if the return value is 0, no photo was found to delete
     *
     * @param id ID of photo to delete
     * @return OK with number of rows deleted, badrequest if none deleted
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> deletePhoto(Long id) {
        return photoRepository.deletePhoto(id).thenApplyAsync(photoDeleted -> {
            if (photoDeleted == null) {
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.map("Photo not found", "other");
                return badRequest(errorResponse.toJson());
            } else {
                // Mark the files for deletion
                File thumbFile = new File(savePath + photoDeleted.thumbnailFilename);
                File mainFile = new File(savePath + photoDeleted.filename);
                if (!thumbFile.delete()) {
                    // If file fails to delete immediately, mark file for deletion when VM shuts down
                    thumbFile.deleteOnExit();
                }
                if (!mainFile.delete()) {
                    // If file fails to delete immediately, mark file for deletion when VM shuts down
                    mainFile.deleteOnExit();
                }

                // Return number of photos deleted
                return ok(Json.toJson(1));
            }
        });
    }

    /**
     * Toggles the privacy of a photo
     *
     * @param request Request to read cookie data from
     * @param id id of photo to toggle
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> togglePhotoPrivacy(Http.Request request, Long id) {
        JsonNode data = request.body().asJson();
        Boolean isPublic = data.get("isPublic").asBoolean();
        return photoRepository.getPhotoById(id).thenComposeAsync(photo -> {
            if (photo != null) {
                photo.isPublic = isPublic;
            } else {
                return CompletableFuture.supplyAsync(() -> notFound());
            }
            return photoRepository.updatePhoto(photo).thenApplyAsync(rows -> ok(Json.toJson(rows)));
        });
    }

    /**
     * Links photo to a destination
     *
     * @param request Request with destination id in body
     * @param photoId id of photo to link
     * @param destId id of destination to link to
     * @return OK with number of rows changed
     */
    //Should this not be in the destination controller? you add a photo to a destination not the other way around?(fiquretivly anyway)
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> linkPhotoToDest(Http.Request request, Long destId, Long photoId) {
        return photoRepository.getPhotoById(photoId).thenComposeAsync(photo -> {
            if (photo != null) {
                return destinationRepository.getDestination(destId).thenComposeAsync(destination -> {
                    if (destination != null) {
                        photo.destinationPhotos.add(destination);
                        return photoRepository.updatePhoto(photo).thenApplyAsync(rows -> ok(Json.toJson(rows)));
                    } else {
                        return CompletableFuture.supplyAsync(() -> notFound());
                    }
                });
            } else {
                return CompletableFuture.supplyAsync(() -> notFound());
            }
        });
    }


    /**
     * Lists routes to put in JS router for use from frontend
     *
     * @return JSRouter Play result
     */
    public Result photoRoutes(Http.Request request) {
        return ok(
            JavaScriptReverseRouter.create("photoRouter", "jQuery.ajax", request.host(),
                controllers.backend.routes.javascript.PhotoController.togglePhotoPrivacy()
            )
        ).as(Http.MimeTypes.JAVASCRIPT);
    }

}
