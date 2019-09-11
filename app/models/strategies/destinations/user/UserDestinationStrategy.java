package models.strategies.destinations.user;

import java.util.concurrent.CompletableFuture;
import models.Profile;
import models.strategies.destinations.DestinationStrategy;
import repository.DestinationRepository;
import repository.ProfileRepository;

public abstract class UserDestinationStrategy extends DestinationStrategy {

    // ID of the user which this photo event is related to
    private Long userId;

    // Reference to profile repo singleton for fetching profile data
    private ProfileRepository profileRepository;

    /**
     * Constructor to instantiate strategies for events that involve a destination and a user
     *
     * @param destId ID of destination referenced in event
     * @param destinationRepository Reference to destination repository
     * @param userId ID of user performing the event
     * @param profileRepository Reference to profile repository
     */
    public UserDestinationStrategy(Long destId, DestinationRepository destinationRepository,
        Long userId, ProfileRepository profileRepository) {
        super(destId, destinationRepository);
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
