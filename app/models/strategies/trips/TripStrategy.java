package models.strategies.trips;

import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import models.Profile;
import models.Trip;
import models.strategies.NewsFeedStrategy;
import repository.ProfileRepository;
import repository.TripRepository;

public abstract class TripStrategy extends NewsFeedStrategy {

    // The id of the trip that is being referenced by the news feed event
    private Long tripId;

    // The id of the user who has performed the event
    private Long userId;

    // Reference to profile repo singleton for fetching profile data
    @Inject
    private ProfileRepository profileRepository;

    // Reference to trip repo singleton for fetching trip data
    @Inject
    private TripRepository tripRepository;

    /**
     * Constructor to instantiate both required fields
     * @param tripId ID of trip the news feed event is about
     * @param userId ID of the user who has performed the event
     */
    public TripStrategy(Long tripId, Long userId) {
        this.tripId = tripId;
        this.userId = userId;
    }

    /**
     * Returns the profile for the user who has performed the event
     * @return Completable future that will return profile when allowed to complete
     */
    protected CompletableFuture<Profile> getUserProfileAsync() {
        return profileRepository.findID(userId);
    }

    /**
     * Returns the trip which has been referenced by id given on object construction
     * @return Completable future that will return referenced trip when allowed to complete
     */
    protected CompletableFuture<Trip> getReferencedTripAsync() {
        return tripRepository.getTripById(tripId);
    }
}
