package models.strategies.trips.concrete;

import java.util.concurrent.CompletableFuture;
import java.util.List;
import models.NewsFeedResponseItem;
import models.strategies.trips.TripStrategy;
import repository.ProfileRepository;
import repository.TripRepository;

public class CreateTripStrategy extends TripStrategy {

    /**
     * Constructor to instantiate both required fields
     * @param tripId ID of trip the news feed event is about
     * @param userId ID of the user who has performed the event
     * @param profileRepository Instance of profileRepository
     * @param tripRepository Instance of tripRepository
     */
    public CreateTripStrategy(Long tripId, Long userId, ProfileRepository profileRepository,
        TripRepository tripRepository, List<Long> eventIds) {
        super(tripId, userId, profileRepository, tripRepository, eventIds);
    }

    /**
     * The method that handles executing whatever relevant code for any news feed strategy
     *
     * @return JSON node containing data that will be sent to front end
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
                    profile.profilePhoto,
                    profile.userId,
                    trip,
                    eventIds)
        ));
    }
}
