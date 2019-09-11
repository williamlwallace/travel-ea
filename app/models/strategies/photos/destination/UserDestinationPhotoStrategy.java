package models.strategies.photos.destination;

import java.util.concurrent.CompletableFuture;
import models.Profile;
import repository.DestinationRepository;
import repository.PhotoRepository;
import repository.ProfileRepository;

public abstract class UserDestinationPhotoStrategy extends DestinationPhotoStrategy {
    // ID of the user which this photo event is related to
    private Long userId;

    // Reference to profile repo singleton for fetching destination data
    private ProfileRepository profileRepository;

    /**
     * Constructor to instantiate an event involving some photo, user and destination
     *
     * @param photoId ID of photo referenced in event
     * @param destinationId ID of destination referenced in event
     * @param userId ID of user who has linked the photo
     * @param photoRepository Instance of photoRepository
     * @param destinationRepository Instance of destinationRepository
     * @param profileRepository Instance of profileRepository
     */
    public UserDestinationPhotoStrategy(Long photoId, Long destinationId, Long userId, PhotoRepository photoRepository,
        DestinationRepository destinationRepository, ProfileRepository profileRepository) {
        super(photoId, destinationId, photoRepository, destinationRepository);
        this.userId = userId;
        this.profileRepository = profileRepository;
    }

    /**
     * Returns the profile which has been referenced by id given on object construction
     *
     * @return Completable future that will return referenced profile when allowed to complete
     */
    protected CompletableFuture<Profile> getUserProfileAsync() {
        return profileRepository.findID(userId);
    }
}
