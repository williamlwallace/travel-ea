package models.strategies.trips.concrete;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import models.NewsFeedResponseItem;
import models.strategies.trips.TripStrategy;
import repository.ProfileRepository;
import repository.TripRepository;

public class MultipleUpdateTripStrategy extends TripStrategy {

    List<Long> newDestIds;

    /**
     * Constructor to instantiate strategy for multiple updates to a single trip
     *
     * @param tripId ID of trip
     * @param userId User who made the change
     * @param profileRepository Reference to profile repository
     * @param tripRepository Reference to trip repository
     * @param newDestIds The destination ids that were just added to the trip
     */
    public MultipleUpdateTripStrategy(Long tripId, Long userId,
        ProfileRepository profileRepository, TripRepository tripRepository,
        List<Long> newDestIds) {
        super(tripId, userId, profileRepository, tripRepository);
        this.newDestIds = newDestIds;
    }

    /**
     * Returns a newsFeedResponseItem that will be sent to front end to handle grouped trip updates
     * @return newsFeedResponseItem sent to front end
     */
    @Override
    public CompletableFuture<NewsFeedResponseItem> execute() {
        return getUserProfileAsync().thenComposeAsync(profile ->
           getReferencedTripAsync().thenApplyAsync(trip ->
               new NewsFeedResponseItem(
                   String.format("just created a new trip with %d destinations! The trip begins in %s and ends in %s.", trip.tripDataList.size(),
                       trip.tripDataList.get(0).destination.name,
                       trip.tripDataList.get(trip.tripDataList.size() - 1).destination.name),
                   profile.firstName + " " + profile.lastName,
                   (profile.profilePhoto == null) ? null : profile.profilePhoto.thumbnailFilename,
                   profile.userId,
                   new Pair<>(trip, this.newDestIds))
        ));
    }
}
