package models.strategies.trips.concrete;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.concurrent.CompletableFuture;
import models.strategies.trips.TripStrategy;

public class UpdateTripStrategy extends TripStrategy {
    /**
     * Constructor to call super class
     * @param tripId ID of trip being referenced
     * @param userId ID of user performing the event
     */
    public UpdateTripStrategy(Long tripId, Long userId) {
        super(tripId, userId);
    }

    /**
     * The method that handles executing whatever relevant code for any news feed strategy
     *
     * @return JSON node containing data that will be sent to front end
     */
    @Override
    public CompletableFuture<JsonNode> execute() {
        return null;
    }
}
