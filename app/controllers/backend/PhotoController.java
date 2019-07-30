package controllers.backend;

import actions.ActionState;
import actions.Authenticator;
import actions.roles.Everyone;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import controllers.backend.routes.javascript;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import models.Photo;
import models.User;
import org.joda.time.LocalDateTime;
import play.libs.Files;
import play.libs.Json;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.With;
import play.routing.JavaScriptReverseRouter;
import repository.DestinationRepository;
import repository.PhotoRepository;
import util.objects.Pair;
import util.validation.ErrorResponse;


@SuppressWarnings("SpellCheckingInspection")
public class PhotoController extends TEABackController {

    // Constant fields defining the directories of regular photos and test photos
    private static final String PHOTO_DIRECTORY = "/storage/photos/";
    private static final String TEST_PHOTO_DIRECTORY = "/storage/photos/test/";

    // Constant fields defining the directory of publicly available files
    private static final String PUBLIC_DIRECTORY = "/public";
    // Default dimensions of thumbnail images
    private static final int THUMB_WIDTH = 400;
    private static final int THUMB_HEIGHT = 266;
    private final String savePath;
    // Repositories to handle DB transactions
    private PhotoRepository photoRepository;
    private DestinationRepository destinationRepository;


    @Inject
    public PhotoController(DestinationRepository destinationRepository,
        PhotoRepository photoRepository, play.Environment environment) {
        this.destinationRepository = destinationRepository;
        this.photoRepository = photoRepository;

        savePath = ((environment.isProd()) ? "/home/sengstudent" : System.getProperty("user.dir"))
            + PUBLIC_DIRECTORY;
    }

    /**
     * Takes a path to a file and returns the file object.
     *
     * @param filePath path to file to read
     */
    public Result getPhotoFromPath(String filePath) {
        File file = new File(filePath);
        return ok(file, true);
    }

    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> makePhotoProfile(Http.Request request, Long id) {
        User user = request.attrs().get(ActionState.USER);
        Long currentUserId = user.id;

        // Check if user is authorized to perform this action
        if(!id.equals(currentUserId) && !user.admin) {
            return CompletableFuture.supplyAsync(Results::forbidden);
        }

        // Get json parameters
        String bodyText = request.body().asJson().asText();
        final String photoLocation = bodyText.substring(bodyText.lastIndexOf('/') + 1);

        // Get current profile photo, if any exists
        return photoRepository.getUserProfilePicture(id).thenComposeAsync(photo -> {
            // If we are going back to no profile picture
            if(photoLocation.equals("")) {
                photo.isPublic = false;
                photo.isProfile = false;
                photo.filename = photo.filename.replaceFirst("../user_content/", "");
                photo.thumbnailFilename = photo.thumbnailFilename.replaceFirst("../user_content/", "");
                return photoRepository.updatePhoto(photo).thenApplyAsync(returnValue -> ok(Json.toJson(photo.filename)));
            } else {
                Photo currentProfile = new Photo();
                String returnData = "";

                if(photo == null) {
                    currentProfile.isProfile = true;
                    currentProfile.userId = id;
                    currentProfile.uploaded = LocalDateTime.now();
                } else {
                    currentProfile = photo;
                    returnData = photo.filename;
                }

                // Now update profile picture filename (and add it if necessary)
                currentProfile.filename = savePath + PHOTO_DIRECTORY + photoLocation;
                currentProfile.thumbnailFilename = savePath + PHOTO_DIRECTORY + "thumbnails/" + photoLocation;
                currentProfile.isPublic = true;
                final String finalisedReturnData = returnData;
                final Photo finalisedPhoto = currentProfile;
                // Delete the copy of the photo
                return photoRepository.deletePhotoByFilename(savePath + PHOTO_DIRECTORY + photoLocation).thenComposeAsync(deleted -> {
                    if (photo == null) {
                        return photoRepository.addPhoto(finalisedPhoto)
                            .thenApplyAsync(newId -> ok(Json.toJson(finalisedReturnData)));
                    } else {
                        return photoRepository.updatePhoto(finalisedPhoto)
                            .thenApplyAsync(newId -> ok(Json.toJson(finalisedReturnData)));
                    }
                });
            }
        });
    }

    /**
     * Returns all photos belonging to a user. Only returns public photos if it is not the owner of
     * the photos getting them
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
     * Gets the profile picture of user with given id.
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
     * Uploads any number of photos from a multipart/form-data request.
     *
     * @param request Request where body is a multipart form-data
     * @return Result of query
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> upload(Http.Request request) {
        // Get the request body, and turn it into a
        // multipart form data collection of temporary files
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

        // Store photos in a list to allow them all to
        // be uploaded at the end if all are read successfully
        ArrayList<Pair<Photo, Http.MultipartFormData.FilePart<Files.TemporaryFile>>>
            photos = new ArrayList<>();

        // Iterate through all files in the request
        for (Http.MultipartFormData.FilePart<Files.TemporaryFile> file : body.getFiles()) {
            if (file != null) {
                try {
                    // Store file with photo in list to be added later
                    photos.add(new Pair<>(
                        readFileToPhoto(file, profilePhotoFilename, publicPhotoFileNames,
                            request.attrs().get(ActionState.USER).id, isTest), file));
                } catch (IOException e) {
                    // If an invalid file type given, return bad request
                    // with error message generated in exception
                    return CompletableFuture
                        .supplyAsync(() -> badRequest(Json.toJson(e.getMessage())));
                }
            } else {
                // If any uploads fail, return bad request immediately
                return CompletableFuture.supplyAsync(() -> badRequest(Json.toJson("Missing file")));
            }
        }

        // If no photos were actually found, and no other error has been thrown, throw it now
        if (photos.isEmpty()) {
            return CompletableFuture.supplyAsync(() -> badRequest(Json.toJson("No files given")));
        } else {
            try {
                return saveMultiplePhotos(photos);
            } catch (IOException e) {
                return CompletableFuture.supplyAsync(() -> internalServerError(
                    Json.toJson("Unkown number of photos failed to save")));
            }
        }
    }

    /**
     * Saves multiple photo files to storage folder, and inserts reference to them to database.
     *
     * @param photos Collection of pairs of Photo and HTTP multipart form data file parts
     */
    private CompletableFuture<Result> saveMultiplePhotos(
        Collection<Pair<Photo, Http.MultipartFormData.FilePart<Files.TemporaryFile>>> photos)
        throws IOException {
        // Add all the photos we found to the database
        long userToRemoveProfilePhoto = -1;
        int thumbWidth = THUMB_WIDTH;
        int thumbHeight = THUMB_HEIGHT;
        for (Pair<Photo, Http.MultipartFormData.FilePart<Files.TemporaryFile>> pair : photos) {
            // if photo to add is marked as new profile pic, clear any existing profile pic first
            if (pair.getKey().isProfile) {
                userToRemoveProfilePhoto = pair.getKey().userId;
                // Profile picture small thumbnail dimensions
                thumbWidth = 100;
                thumbHeight = 100;
            }

            // Buffer the image and use same file creation process as
            BufferedImage fullImage = ImageIO.read(pair.getValue().getRef().path().toFile());
            // Create and save main image
            createPhotoFromFile(pair.getValue().getRef(), fullImage.getWidth(),
                fullImage.getHeight()).copyTo(Paths.get(pair.getKey().filename));
            // Create and save thumbnail image
            createPhotoFromFile(pair.getValue().getRef(), thumbWidth, thumbHeight)
                .copyTo(Paths.get(pair.getKey().thumbnailFilename));

        }
        // Collect all keys from the list to upload
        List<Photo> photosToAdd = photos.stream().map(Pair::getKey).collect(Collectors.toList());

        // If this photo is going to be added as profile picture, return the name of it
        if (userToRemoveProfilePhoto > 0) {
            // Do not allow profile-ness of a photo to be set here, the makePhotoProfile endpoint
            // must be used for this. This is to allow undoing of adding a profile photo
            for(Photo photo : photosToAdd) {
                photo.isProfile = false;
            }
            photoRepository.addPhotos(photosToAdd);
            // Return filename of photo that was just added
            return CompletableFuture.supplyAsync(() -> created(Json.toJson(photosToAdd.get(0).filename.substring(photosToAdd.get(0).filename.lastIndexOf('/') + 1))));
        } else {
            return CompletableFuture.supplyAsync(() -> {
                photoRepository.addPhotos(photosToAdd);
                return created(Json.toJson("File(s) uploaded successfully"));
            });
        }
    }

    /**
     * Reads a file part from the multipart form and returns a Photo object to add to the database.
     *
     * @param file File part from form
     * @param profilePhotoFilename Name (if any) of photo to be set as profile picture
     * @param publicPhotoFileNames Names (if any) of photos to be set to public, defaults to private
     * if referenced here
     * @param userId ID of user who is uploading the files
     * @param isTest Whether or not these photos should be added to test folder of storage
     * @return Photo object to be added to database
     * @throws IOException Thrown when an unsupported file type added (i.e not image/jpeg or
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
        photo.filename = (savePath + ((isTest) ? TEST_PHOTO_DIRECTORY : PHOTO_DIRECTORY)
            + fileName);
        photo.isProfile =
            profilePhotoFilename != null && profilePhotoFilename.equals(file.getFilename());
        photo.isPublic = publicPhotoFileNames.contains(file.getFilename()) || photo.isProfile;
        photo.thumbnailFilename = (savePath + ((isTest) ? TEST_PHOTO_DIRECTORY : PHOTO_DIRECTORY)
            + "thumbnails/" + fileName);
        photo.uploaded = LocalDateTime.now();
        photo.userId = userId;

        // Return the created photo object
        return photo;
    }

    /**
     * Creates a new image at default thumbnail size of a given image.
     *
     * @param fullImageFile The full image to create a filename for
     * @return Temporary file of thumbnail
     */
    private Files.TemporaryFile createPhotoFromFile(Files.TemporaryFile fullImageFile,
        int thumbWidth, int thumbHeight) throws IOException {
        BufferedImage thumbImage = new BufferedImage(thumbWidth, thumbHeight,
            BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = thumbImage
            .createGraphics(); //create a graphics object to paint to
        graphics2D.setBackground(Color.WHITE);
        graphics2D.setPaint(Color.WHITE);
        graphics2D.fillRect(0, 0, thumbWidth, thumbHeight);
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Convert full image file to java.awt image
        BufferedImage fullImage = ImageIO.read(fullImageFile.path().toFile());

        // If image is smaller than thumbnail size then center with bars on each side
        if (fullImage.getWidth() < thumbWidth && fullImage.getHeight() < thumbHeight) {
            graphics2D.drawImage(fullImage, thumbWidth / 2 - fullImage.getWidth() / 2,
                thumbHeight / 2 - fullImage.getHeight() / 2, fullImage.getWidth(),
                fullImage.getHeight(), null);
        } // Otherwise scale image down so biggest side
        // is set to max of thumbnail and rest is scaled proportionally
        else {
            // Determine which side is proportionally bigger
            boolean fitWidth =
                fullImage.getWidth() / thumbWidth > fullImage.getHeight() / thumbHeight;
            double scaleFactor = (fitWidth) ? (double) thumbWidth / (double) fullImage.getWidth()
                : (double) thumbHeight / (double) fullImage.getHeight();
            if (fitWidth) {
                int newHeight = (int) Math.floor(fullImage.getHeight() * scaleFactor);
                graphics2D
                    .drawImage(fullImage, 0, thumbHeight / 2 - newHeight / 2, thumbWidth, newHeight,
                        null);
            } else {
                int newWidth = (int) Math.floor(fullImage.getWidth() * scaleFactor);
                graphics2D
                    .drawImage(fullImage, thumbWidth / 2 - newWidth / 2, 0, newWidth, thumbHeight,
                        null);
            }
        }

        // Create file to store output of thumbnail write
        File thumbFile = new File(savePath + TEST_PHOTO_DIRECTORY + "tempThumb.jpg");

        // Write buffered image to thumbnail file
        ImageIO.write(thumbImage, "jpg", thumbFile);

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
     * @return OK with number of rows deleted, bad request if none deleted
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
                    // If file fails to delete immediately,
                    // mark file for deletion when VM shuts down
                    thumbFile.deleteOnExit();
                }
                if (!mainFile.delete()) {
                    // If file fails to delete immediately,
                    // mark file for deletion when VM shuts down
                    mainFile.deleteOnExit();
                }

                // Return number of photos deleted
                return ok(Json.toJson(1));
            }
        });
    }

    /**
     * Toggles the privacy of a photo.
     *
     * @param request Request to read cookie data from
     * @param id id of photo to toggle
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> togglePhotoPrivacy(Http.Request request, Long id) {
        JsonNode data = request.body().asJson();
        Boolean isPublic = data.get("isPublic").asBoolean();
        return photoRepository.getPhotoById(id).thenComposeAsync(photo -> {
            Photo existingPhoto = photo;
            if (photo != null) {
                photo.isPublic = isPublic;
            } else {
                return CompletableFuture.supplyAsync(Results::notFound);
            }
            return photoRepository.updatePhoto(photo).thenApplyAsync(rows -> ok(Json.toJson(existingPhoto)));
        });
    }

    /**
     * Links and deletes links from photo to a destination.
     *
     * @param request Request
     * @param photoId id of photo
     * @param destId id of destination
     * @return OK if successful or not found
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> deleteLinkPhotoToDest(Http.Request request, Long destId,
        Long photoId) {
        Long userId = request.attrs().get(ActionState.USER).id;
        return photoRepository.getDeletedDestPhoto(photoId, destId)
            .thenComposeAsync(deletedPhoto -> {
                return destinationRepository.getDestination(destId)
                    .thenComposeAsync(destination -> {
                    return photoRepository.getPhotoById(photoId).thenComposeAsync(photo -> {
                        if (deletedPhoto == null) {
                            if (destination != null) {
                                if (!destination.isPublic && !destination.user.id
                                    .equals(userId)) {
                                    //forbidden if destination is private and user does not own destination
                                    return CompletableFuture.supplyAsync(Results::forbidden);
                                }
                                if (destination.isPublic && !destination.user.id
                                    .equals(userId)) {
                                    //Set destination owner to admin if photo is added from diffrent user
                                    destinationRepository
                                        .changeDestinationOwner(destId, MASTER_ADMIN_ID)
                                        .thenApplyAsync(rows -> rows); //set to master admin
                                }
                                if (destination.isLinked(photoId)) {
                                    //if photo is already linked return badrequest
                                    return CompletableFuture.supplyAsync(Results::badRequest);
                                }
                                if (photo == null) {
                                    return CompletableFuture.supplyAsync(Results::notFound);
                                }
                                destination.destinationPhotos.add(photo);
                                return destinationRepository.updateDestination(destination)
                                    .thenApplyAsync(
                                        dest -> ok(Json.toJson("Succesfully Updated")));
                            }
                            return CompletableFuture.supplyAsync(Results::notFound);
                        } else {
                            if (photo == null) {
                                return CompletableFuture.supplyAsync(Results::notFound);
                            }
                            if (!photo.removeDestination(destId)) {
                                return CompletableFuture.supplyAsync(Results::notFound);
                            }

                        }
                        deletedPhoto.deleted = !deletedPhoto.deleted;
                        return photoRepository.updatePhoto(photo)
                            .thenApplyAsync(rows -> ok(Json.toJson(deletedPhoto.guid)));
                    });
                });
            });
    }

    /**
     * Get Destination photos based on logged in user.
     *
     * @param request Request
     * @param destId id of destination
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> getDestinationPhotos(Http.Request request, Long destId) {
        User user = request.attrs().get(ActionState.USER);
        return destinationRepository.getDestination(destId).thenApplyAsync(destination -> {
            if (destination == null) {
                return notFound(Json.toJson(destId));
            } else if (!destination.isPublic && !destination.user.id.equals(user.id)
                && !user.admin) {
                return forbidden();
            } else {
                List<Photo> photos = filterPhotos(destination.destinationPhotos, user.id);
                try {
                    photos = photoRepository.appendAssetsUrl(photos);
                    return ok(sanitizeJson(Json.toJson(photos)));
                } catch (IOException e) {
                    return internalServerError(Json.toJson(SANITIZATION_ERROR));
                }
            }
        });
    }

    /**
     * Removes photos that shouldnt be seen by given user.
     *
     * @param photos List of photos
     * @param userId Id of authenticated user
     * @return List of filtered photos
     */
    private List<Photo> filterPhotos(List<Photo> photos, Long userId) {
        Iterator<Photo> iter = photos.iterator();
        while (iter.hasNext()) {
            Photo photo = iter.next();
            if (!photo.isPublic && !photo.userId.equals(userId)) {
                iter.remove();
            }
        }
        return photos;
    }

    /**
     * Lists routes to put in JS router for use from frontend.
     *
     * @return JSRouter Play result
     */
    public Result photoRoutes(Http.Request request) {
        return ok(
            JavaScriptReverseRouter.create("photoRouter", "jQuery.ajax", request.host(),
                controllers.backend.routes.javascript.PhotoController.upload(),
                controllers.backend.routes.javascript.PhotoController.togglePhotoPrivacy(),
                controllers.backend.routes.javascript.PhotoController.getAllUserPhotos(),
                controllers.backend.routes.javascript.PhotoController.deleteLinkPhotoToDest(),
                controllers.backend.routes.javascript.PhotoController.getDestinationPhotos(),
                controllers.backend.routes.javascript.PhotoController.makePhotoProfile()
            )
        ).as(Http.MimeTypes.JAVASCRIPT);
    }

}