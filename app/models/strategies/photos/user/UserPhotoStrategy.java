package models.strategies.photos.user;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import javax.inject.Inject;
import models.Profile;
import models.strategies.photos.PhotoStrategy;
import repository.PhotoRepository;
import repository.ProfileRepository;

public abstract class UserPhotoStrategy extends PhotoStrategy {
    // ID of the user which this photo event is related to
    private Long userId;

    // Reference to profile repo singleton for fetching profile data
    private ProfileRepository profileRepository;

    /**
     * Constructor to instantiate an event involving some photo and some user
     * @param photoId ID of photo referenced in event
     * @param userId ID of user referenced in event
     * @param photoRepository Instance of photoRepository
     * @param profileRepository Instance of profileRepository
     */
    public UserPhotoStrategy(Long photoId, Long userId, PhotoRepository photoRepository, ProfileRepository profileRepository, List<Long> eventIds) {
        super(photoId, photoRepository, eventIds);
        this.userId = userId;
        this.profileRepository = profileRepository;
    }

    /**
     * Returns the destination which has been referenced by id given on object construction
     * @return Completable future that will return referenced destination when allowed to complete
     */
    protected CompletableFuture<Profile> getUserProfileAsync() {
        return profileRepository.findID(userId);
    }
}
