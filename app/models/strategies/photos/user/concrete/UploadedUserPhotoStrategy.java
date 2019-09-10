package models.strategies.photos.user.concrete;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.concurrent.CompletableFuture;
import models.strategies.photos.user.UserPhotoStrategy;

public class UploadedUserPhotoStrategy extends UserPhotoStrategy {

    /**
     * Constructor to instantiate an event for some user uploading a new photo
     * @param photoId ID of photo used for profile picture
     * @param userId ID of user who updated their profile picture
     */
    public UploadedUserPhotoStrategy(Long photoId, Long userId) {
        super(photoId, userId);
    }

    /**
     * The method that handles executing whatever relevant code for any news feed strategy
     *
     * @return JSON node containing data that will be sent to front end
     */
    @Override
    public CompletableFuture<JsonNode> execute() {
        // getUserProfileAsync() and getReferencedPhotoAsync() will be useful here

        return null;
    }

}
