package models.strategies.photos.user;

import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import models.Profile;
import models.strategies.photos.PhotoStrategy;
import repository.ProfileRepository;

public abstract class UserPhotoStrategy extends PhotoStrategy {
    // ID of the user which this photo event is related to
    private Long userId;

    // Reference to profile repo singleton for fetching profile data
    @Inject
    private ProfileRepository profileRepository;

    /**
     * Constructor to instantiate an event involving some photo and some destination
     * @param photoId ID of photo referenced in event
     * @param userId ID of user referenced in event
     */
    public UserPhotoStrategy(Long photoId, Long userId) {
        super(photoId);
        this.userId = userId;
    }

    /**
     * Returns the destination which has been referenced by id given on object construction
     * @return Completable future that will return referenced destination when allowed to complete
     */
    protected CompletableFuture<Profile> getUserProfileAsync() {
        return profileRepository.findID(userId);
    }
}
