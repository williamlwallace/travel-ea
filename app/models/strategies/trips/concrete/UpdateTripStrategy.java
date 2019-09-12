package models.strategies.trips.concrete;

import java.util.concurrent.CompletableFuture;
import models.NewsFeedResponseItem;
import models.strategies.trips.TripStrategy;
import repository.ProfileRepository;
import repository.TripRepository;

public class UpdateTripStrategy extends TripStrategy {

    /**
     * Constructor to instantiate both required fields
     * @param tripId ID of trip the news feed event is about
     * @param userId ID of the user who has performed the event
     * @param profileRepository Instance of profileRepository
     * @param tripRepository Instance of tripRepository
     */
    public UpdateTripStrategy(Long tripId, Long userId, ProfileRepository profileRepository,
        TripRepository tripRepository) {
        super(tripId, userId, profileRepository, tripRepository);
    }

    /**
     * The method that handles executing whatever relevant code for any news feed strategy
     *
     * @return JSON node containing data that will be sent to front end
     */
    @Override
    public CompletableFuture<NewsFeedResponseItem> execute() {
        return getReferencedTripAsync().thenComposeAsync(trip ->
            getUserProfileAsync().thenApplyAsync(profile ->
                new NewsFeedResponseItem(
                    String.format("has updated their trip from %s to %s",
                        trip.tripDataList.get(0).destination.name,
                        trip.tripDataList.get(trip.tripDataList.size() - 1).destination.name
                    ),
                    profile.firstName + " " + profile.lastName,
                    profile.profilePhoto,
                    profile.userId,
                    trip)
                )
            );
    }
}
