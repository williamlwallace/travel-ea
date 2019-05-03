package controllers.backend;

import actions.ActionState;
import actions.Authenticator;
import actions.roles.Everyone;
import com.google.inject.Inject;
import models.Photo;
import org.joda.time.DateTime;
import play.libs.Files;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import repository.PhotoRepository;
import util.customObjects.Pair;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class PhotoController extends Controller {

    // Constant fields defining the directories of regular photos and test photos
    private static final String PHOTO_DIRECTORY = "public/storage/photos/";
    private static final String TEST_PHOTO_DIRECTORY = "public/storage/photos/test/";

    // Photo repository to handle DB transactions
    private PhotoRepository photoRepository;

    @Inject
    public PhotoController(PhotoRepository photoRepository) {
        this.photoRepository = photoRepository;
    }

    /**
     * Uploads any number of photos from a multipart/form-data request
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
                        photos.add(new Pair<>(readFileToPhoto(file, profilePhotoFilename, publicPhotoFileNames, request.attrs().get(ActionState.USER).id), file));
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
                saveMultiplePhotos(photos, isTest);
            }

            // Return OK if no issues were encountered
            return status(201, "File(s) uploaded successfully");
        });
    }

    /**
     * Saves multiple photo files to storage folder, and inserts reference to them to database
     * @param photos Collection of pairs of <Photo, Http.MultipartFormData.FilePart<Files.TemporaryFile>>
     * @param isTest Whether or not these photos should be added to test folder of storage
     */
    private void saveMultiplePhotos(Collection<Pair<Photo, Http.MultipartFormData.FilePart<Files.TemporaryFile>>> photos, boolean isTest) {
        // Add all the photos we found to the database
        for(Pair<Photo, Http.MultipartFormData.FilePart<Files.TemporaryFile>> pair : photos)
        {
            if(isTest) {
                pair.getValue().getRef().copyTo(Paths.get(TEST_PHOTO_DIRECTORY + pair.getKey().filename), true);
            } else {
                pair.getValue().getRef().copyTo(Paths.get(PHOTO_DIRECTORY + pair.getKey().filename), true);
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
     * @return Photo object to be added to database
     * @throws IOException Thrown when an unsupported filetype added (i.e not image/jpeg or image/png)
     */
    private Photo readFileToPhoto(Http.MultipartFormData.FilePart<Files.TemporaryFile> file, String profilePhotoFilename, HashSet<String> publicPhotoFileNames, long userId) throws IOException {
        // Get the filename, file size and content-type of the file
        String fileName = System.currentTimeMillis() + "_" + file.getFilename();

        String contentType = file.getContentType();
        if(!contentType.equals("image/jpeg") && !contentType.equals("image/png")) {
            // If any file is found that is not a png or jpeg reject all files with error message
            throw new IOException("Invalid file type given: " + contentType);
        }

        // Create a photo object
        Photo photo = new Photo();
        photo.filename = fileName;
        photo.isProfile = profilePhotoFilename != null && profilePhotoFilename.equals(file.getFilename());
        photo.isPublic = publicPhotoFileNames.contains(file.getFilename()) || photo.isProfile;
        photo.thumbnailFilename = fileName;
        photo.uploaded = DateTime.now();
        photo.userId = userId;

        // Return the created photo object
        return photo;
    }
}
