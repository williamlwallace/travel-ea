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

import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class PhotoController extends Controller {

    private PhotoRepository photoRepository;

    @Inject
    public PhotoController(PhotoRepository photoRepository) {
        this.photoRepository = photoRepository;
    }

    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> getAllUserPhotos(Http.Request request) {
        Long userId = request.attrs().get(ActionState.USER).id;

        return photoRepository.getAllUserPhotos(userId)
                .thenApplyAsync(photos -> ok(Json.toJson(photos)));
    }

    /**
     * Uploads any number of photos from a multipart/form-data request
     *
     * @param request Request where body is a json object of trip
     * @return Result of query
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> upload(Http.Request request) {
        return CompletableFuture.supplyAsync(() -> {
            // Get the request body, and turn it into a multipart formdata collection of temporary files
            Http.MultipartFormData<Files.TemporaryFile> body = request.body().asMultipartFormData();

            // Store in a boolean whether or not this is a test file
            boolean isTest = false;
            // Keep track of which file should be uploaded as a profile
            String profilePhotoFilename = null;
            // Keep track of which photos are marked as public
            HashSet<String> publicPhotoFileNames = new HashSet<>();

            // Iterate through all keys in the request
            for(Map.Entry<String, String[]> entry : body.asFormUrlEncoded().entrySet()) {
                // Check whether or not this is a test file
                if(entry.getKey().equals("isTest")) {
                    isTest = Boolean.parseBoolean(entry.getValue()[0]);
                }
                // Check if one of these if meant to be a profile photo
                if(entry.getKey().equals("profilePhotoName")) {
                    profilePhotoFilename = entry.getValue()[0];
                }
                // Check if any photos are marked as public
                if(entry.getKey().equals("publicPhotoFileNames")) {
                    publicPhotoFileNames = new HashSet<>(Arrays.asList(entry.getValue()[0].split(",")));
                }
            }

            // Store photos
            ArrayList<Pair<Photo, Http.MultipartFormData.FilePart<Files.TemporaryFile>>> photos = new ArrayList<>();

            // Iterate through all files in the request
            for(Http.MultipartFormData.FilePart<Files.TemporaryFile> file : body.getFiles()){
                if (file != null) {
                    // Get the filename, filesize and content-type of the file
                    String fileName = System.currentTimeMillis() + "_" + file.getFilename();

                    String contentType = file.getContentType();
                    if(!contentType.equals("image/jpeg") && !contentType.equals("image/png")) {
                        // If any file is found that is not a png or jpeg reject all files with error message
                        return badRequest("Invalid file type given: " + contentType);
                    }

                    // Create a photo object
                    Photo photo = new Photo();
                    photo.filename = fileName;
                    photo.isProfile = profilePhotoFilename != null && profilePhotoFilename.equals(file.getFilename());
                    photo.isPublic = publicPhotoFileNames.contains(file.getFilename()) || photo.isProfile;
                    photo.thumbnailFilename = fileName;
                    photo.uploaded = DateTime.now();
                    photo.userId = request.attrs().get(ActionState.USER).id;

                    // Store file with photo in list to be added later
                    photos.add(new Pair<>(photo, file));

                } else {
                    // If any uploads fail, return bad request immediately
                    return badRequest("Missing file");
                }
            }

            // Add all the photos we found to the database
            for(Pair<Photo, Http.MultipartFormData.FilePart<Files.TemporaryFile>> pair : photos)
            {
                if(isTest) {
                    pair.getValue().getRef().copyTo(Paths.get("public/storage/photos/test/" + pair.getKey().filename), true);
                } else {
                    pair.getValue().getRef().copyTo(Paths.get("public/storage/photos/" + pair.getKey().filename), true);
                }
                // Collect all keys from the list to upload
                photoRepository.addPhotos(photos.stream().map(Pair::getKey).collect(Collectors.toList()));
            }

            // If no photos were actually found, and no other error has been thrown, throw it now
            if(photos.isEmpty()) {
                return badRequest("No files given");
            }

            // Return OK if no issues were encountered
            return status(201, "File(s) uploaded successfully");
        });
    }
}
