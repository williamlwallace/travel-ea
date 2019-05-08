package controllers.backend;

import actions.ActionState;
import actions.Authenticator;
import actions.roles.Everyone;
import com.google.inject.Inject;
import models.Photo;
import org.joda.time.DateTime;
import play.libs.Files;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import repository.PhotoRepository;
import util.customObjects.Pair;
import util.validation.ErrorResponse;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class PhotoController extends Controller {

    // Constant fields defining the directories of regular photos and test photos
    private static final String PHOTO_DIRECTORY = "storage/photos/";
    private static final String TEST_PHOTO_DIRECTORY = "storage/photos/test/";

    // Constant fields defining the directory of publicly available files
    private static final String PUBLIC_DIRECTORY = "../";

    // Default dimensions of thumbnail images
    private static final int THUMB_WIDTH = 400;
    private static final int THUMB_HEIGHT = 266;

    // Photo repository to handle DB transactions
    private PhotoRepository photoRepository;

    @Inject
    public PhotoController(PhotoRepository photoRepository) {
        this.photoRepository = photoRepository;
    }



    public Result getPhotoFromPath(String path, String filePath) {
        System.out.println(path + filePath);
        File file = new File(path + filePath);
        return ok(file, true);
    }


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

    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> getProfilePicture(Long id) {
        return photoRepository.getUserProfilePicture(id)
                .thenApplyAsync(photo -> {
                    if(photo == null) {
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
            boolean isTest = Boolean.parseBoolean(formKeys.getOrDefault("isTest", new String[] {"false"})[0]);
            // Keep track of which file should be uploaded as a profile
            String profilePhotoFilename = formKeys.getOrDefault("profilePhotoName", new String[] {null})[0];
            // Keep track of which photos are marked as public
            HashSet<String> publicPhotoFileNames = new HashSet<>(Arrays.asList(formKeys.getOrDefault("publicPhotoFileNames", new String[] {""})[0].split(",")));

            // Store photos in a list to allow them all to be uploaded at the end if all are read successfully
            ArrayList<Pair<Photo, Http.MultipartFormData.FilePart<Files.TemporaryFile>>> photos = new ArrayList<>();

            // Iterate through all files in the request
            for(Http.MultipartFormData.FilePart<Files.TemporaryFile> file : body.getFiles()){
                if (file != null) {
                    try {
                        // Store file with photo in list to be added later
                        photos.add(new Pair<>(readFileToPhoto(file, profilePhotoFilename, publicPhotoFileNames, request.attrs().get(ActionState.USER).id, isTest), file));
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
            if(photos.isEmpty()) {
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
     * @param photos Collection of pairs of <Photo, Http.MultipartFormData.FilePart<Files.TemporaryFile>>
     */
    private void saveMultiplePhotos(Collection<Pair<Photo, Http.MultipartFormData.FilePart<Files.TemporaryFile>>> photos) {
        // Add all the photos we found to the database
        int thumbWidth = THUMB_WIDTH;
        int thumbHeight = THUMB_HEIGHT;
        for(Pair<Photo, Http.MultipartFormData.FilePart<Files.TemporaryFile>> pair : photos)
        {
            // if photo to add is marked as new profile pic, clear any existing profile pic first
            if(pair.getKey().isProfile) {
                photoRepository.clearProfilePhoto(pair.getKey().userId).thenApply(fileNamesPair -> {
                    if(fileNamesPair != null) {
                        File thumbFile = new File(PUBLIC_DIRECTORY + fileNamesPair.getKey());
                        File mainFile = new File(PUBLIC_DIRECTORY + fileNamesPair.getValue());
                        // Mark the files for deletion
                        if(!thumbFile.delete()) {
                            // If file fails to delete immediately, mark file for deletion when VM shuts down
                            thumbFile.deleteOnExit();
                        }
                        if(!mainFile.delete()) {
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
                pair.getValue().getRef().copyTo(Paths.get(PUBLIC_DIRECTORY + pair.getKey().filename), true);
                createThumbnailFromFile(pair.getValue().getRef(), thumbWidth, thumbHeight)
                        .copyTo(Paths.get(PUBLIC_DIRECTORY + pair.getKey().thumbnailFilename));
            } catch (IOException e) {
                // TODO: Handle case where a file failed to save
            }
        }
        // Collect all keys from the list to upload
        photoRepository.addPhotos(photos.stream().map(Pair::getKey).collect(Collectors.toList()));
    }

    /**
     * Reads a file part from the multipart form and returns a Photo object to add to the database
     * @param file File part from form
     * @param profilePhotoFilename Name (if any) of photo to be set as profile picture
     * @param publicPhotoFileNames Names (if any) of photos to be set to public, defaults to private if referenced here
     * @param userId ID of user who is uploading the files
     * @param isTest Whether or not these photos should be added to test folder of storage
     * @return Photo object to be added to database
     * @throws IOException Thrown when an unsupported filetype added (i.e not image/jpeg or image/png)
     */
    private Photo readFileToPhoto(Http.MultipartFormData.FilePart<Files.TemporaryFile> file, String profilePhotoFilename, HashSet<String> publicPhotoFileNames, long userId, boolean isTest) throws IOException {
        // Get the filename, file size and content-type of the file
        String fileName = System.currentTimeMillis() + "_" + file.getFilename();

        String contentType = file.getContentType();
        if(!contentType.equals("image/jpeg") && !contentType.equals("image/png")) {
            // If any file is found that is not a png or jpeg reject all files with error message
            throw new IOException("Invalid file type given: " + contentType);
        }

        // Create a photo object
        Photo photo = new Photo();
        photo.filename = (((isTest) ? TEST_PHOTO_DIRECTORY : PHOTO_DIRECTORY) + fileName);
        photo.isProfile = profilePhotoFilename != null && profilePhotoFilename.equals(file.getFilename());
        photo.isPublic = publicPhotoFileNames.contains(file.getFilename()) || photo.isProfile;
        photo.thumbnailFilename = (((isTest) ? TEST_PHOTO_DIRECTORY : PHOTO_DIRECTORY) + "thumbnails/" + fileName);
        photo.uploaded = DateTime.now();
        photo.userId = userId;

        // Return the created photo object
        return photo;
    }

    /**
     * Creates a new image at default thumbnail size of a given image
     * @param fullImageFile The full image to create a filename for
     * @return Temporary file of thumbnail
     */
    private Files.TemporaryFile createThumbnailFromFile(Files.TemporaryFile fullImageFile, int thumbWidth, int thumbHeight) throws IOException {
        // Convert full image file to java.awt image
        BufferedImage fullImage = ImageIO.read(fullImageFile.path().toFile());

        BufferedImage tThumbImage = new BufferedImage( thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB );
        Graphics2D tGraphics2D = tThumbImage.createGraphics(); //create a graphics object to paint to
        tGraphics2D.setBackground( Color.WHITE );
        tGraphics2D.setPaint( Color.WHITE );
        tGraphics2D.fillRect(0, 0, thumbWidth, thumbHeight);
        tGraphics2D.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR );
        //tGraphics2D.drawImage( fullImage, 0, 0, THUMB_WIDTH, THUMB_HEIGHT, null ); //draw the image scaled
        // If image is smaller than thumbnail size then center with bars on each side
        if(fullImage.getWidth() < thumbWidth && fullImage.getHeight() < thumbHeight){
            tGraphics2D.drawImage(fullImage,thumbWidth / 2 - fullImage.getWidth() / 2, thumbHeight / 2 - fullImage.getHeight() / 2, fullImage.getWidth(), fullImage.getHeight(), null);
        } // Otherwise scale image down so biggest side is set to max of thumbnail and rest is scaled proportionally
        else {
            // Determine which side is proportionally bigger
            boolean fitWidth = fullImage.getWidth() / thumbWidth > fullImage.getHeight() / thumbHeight;
            double scaleFactor = (fitWidth) ? (double)thumbWidth / (double)fullImage.getWidth() : (double)thumbHeight / (double)fullImage.getHeight();
            if(fitWidth) {
                int newHeight = (int)Math.floor(fullImage.getHeight() * scaleFactor);
                tGraphics2D.drawImage(fullImage, 0, thumbHeight / 2 - newHeight / 2, thumbWidth, newHeight, null);
            } else {
                int newWidth = (int)Math.floor(fullImage.getWidth() * scaleFactor);
                tGraphics2D.drawImage(fullImage, thumbWidth / 2 - newWidth/ 2, 0, newWidth, thumbHeight, null);
            }
        }

        // Create file to store output of thumbnail write
        File thumbFile = new File("./../storage/photos/test/tempThumb.jpg");

        // Write buffered image to thumbnail file
        ImageIO.write(tThumbImage, "jpg", thumbFile);

        // Return temporary file created from the file
        Files.TemporaryFile temporaryFile = (new Files.SingletonTemporaryFileCreator()).create(thumbFile.toPath());

        // Delete the file that it was buffered to
        thumbFile.deleteOnExit();

        return temporaryFile;
    }

    /**
     * Deletes a photo with given id. Return a result with a json int which represents the
     * number of rows that were deleted. So if the return value is 0, no photo was found to
     * delete
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
                File thumbFile = new File(PUBLIC_DIRECTORY + photoDeleted.thumbnailFilename);
                File mainFile = new File(PUBLIC_DIRECTORY + photoDeleted.filename);
                if(!thumbFile.delete()) {
                    // If file fails to delete immediately, mark file for deletion when VM shuts down
                    thumbFile.deleteOnExit();
                }
                if(!mainFile.delete()) {
                    // If file fails to delete immediately, mark file for deletion when VM shuts down
                    mainFile.deleteOnExit();
                }

                // Return number of photos deleted
                return ok(Json.toJson(1));
            }
        });
    }

    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> togglePhotoPrivacy(Long id, Boolean isPublic) {
        return photoRepository.togglePhotoPrivacy(id, isPublic).thenApplyAsync(uploaded ->
                ok(Json.toJson(id))
        );
    }
}
