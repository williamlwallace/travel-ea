package models.strategies.photos.user.concrete;

import java.util.concurrent.CompletableFuture;
import models.NewsFeedResponseItem;
import models.strategies.photos.user.UserPhotoStrategy;
import repository.PhotoRepository;
import repository.ProfileRepository;

public class NewCoverPhotoStrategy extends UserPhotoStrategy {
    /**
     * Constructor to instantiate an event involving a user updating their profile picture
     * @param photoId ID of photo referenced in event
     * @param userId ID of user referenced in event
     * @param photoRepository Instance of photoRepository
     * @param profileRepository Instance of profileRepository
     */
    public NewCoverPhotoStrategy(Long photoId, Long userId,
        PhotoRepository photoRepository, ProfileRepository profileRepository) {
        super(photoId, userId, photoRepository, profileRepository);
    }

    /**
     * The method that handles executing whatever relevant code for any news feed strategy
     *
     * @return JSON node containing data that will be sent to front end
     */
    @Override
    public CompletableFuture<NewsFeedResponseItem> execute() {
        return getReferencedPhotoAsync().thenComposeAsync(photo ->
            getUserProfileAsync().thenApplyAsync(profile ->
                new NewsFeedResponseItem("has updated their cover photo",
                    profile.firstName + " " + profile.lastName,
                    profile.profilePhoto,
                    profile.userId,
                    photo)
            )
        );
    }
}
