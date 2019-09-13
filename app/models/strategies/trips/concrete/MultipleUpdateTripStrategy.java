package models.strategies.trips.concrete;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import models.Destination;
import models.NewsFeedResponseItem;
import models.strategies.trips.TripStrategy;
import play.libs.Json;
import repository.ProfileRepository;
import repository.TripRepository;
import util.StreamHelper;

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
        List<Long> newDestIds, List<Long> eventIds) {
        super(tripId, userId, profileRepository, tripRepository, eventIds);
        this.newDestIds = newDestIds;
    }

    /**
     * Returns a newsFeedResponseItem that will be sent to front end to handle grouped trip updates
     * @return newsFeedResponseItem sent to front end
     */
    @Override
    public CompletableFuture<NewsFeedResponseItem> execute() {
        return getUserProfileAsync().thenComposeAsync(profile ->
            getReferencedTripAsync().thenApplyAsync(trip -> {

                // Get destinations newly added
                List<Destination> newDestinations = trip.tripDataList.stream()
                    .filter(x -> newDestIds.contains(x.destination.id)) // Take a subset of trips sets, which are newly added destinations
                    .map(x -> x.destination) // Convert the trip data to destinations
                    .filter(StreamHelper.distinctByKey(x -> x.id)) // Get distinct destinations (if a dest added twice, only show once)
                    .collect(Collectors.toList());

                // Create response object which has the trip and the newly added destinations
                ObjectNode returnObject = new ObjectNode(new JsonNodeFactory(false));
                returnObject.set("trip", Json.toJson(trip));
                returnObject.set("newDestinations", Json.toJson(newDestinations));

                return new NewsFeedResponseItem(
                    String.format("added %d new destination%s to their trip!", newDestinations.size(), newDestinations.size() == 1 ? "" : "s"),
                    profile.firstName + " " + profile.lastName,
                    profile.profilePhoto,
                    profile.userId,
                    returnObject); 
            }));
    }
}
