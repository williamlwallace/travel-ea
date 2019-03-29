package controllers.backend;

import actions.*;
import actions.roles.*;
import com.fasterxml.jackson.databind.JsonNode;
import models.Destination;
import models.Trip;
import models.TripData;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import repository.TripRepository;
import util.validation.ErrorResponse;
import util.validation.TripValidator;

import javax.inject.Inject;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class TripController extends Controller {

    private final TripRepository tripRepository;

    @Inject
    public TripController(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> getAllUserTrips(Http.Request request) {
        Long userId = request.attrs().get(ActionState.USER).id;

        return tripRepository.getAllUserTrips(userId).thenApplyAsync(trips -> ok(Json.toJson(trips)));
    }

    /**
     * Attempts to fetch all data for a trip with given trip ID. This is returned as a JSON
     * object with 2 fields:
     * uid: This field represents the id of the user who owns the trip
     * tripDataCollection: An array storing all stages of the trip as tripData objects
     * @param id ID of trip to find
     * @return JSON object with uid and trip data
     */
    public CompletableFuture<Result> getTrip(Long id) {
        // Get all the trip data (asynchronously) and then construct and return the json object to send
        return tripRepository.getTripById(id).thenApplyAsync(
                trip -> ok(Json.toJson(trip))
        );
    }

    /**
     * Validates and then updates a trip on the database. This is done by dropping all tripData already linked to the trip
     * and adding all new data. Trying to update individual rows would lead to all sorts of potential traps for data corruption.
     * @param request Request containing JSON trip object to update
     * @return Returns trip id (as json) on success, otherwise bad request
     * @throws IOException
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> updateTrip(Http.Request request) throws IOException {
        // Get the data input by the user as a JSON object
        JsonNode data = request.body().asJson();

        // Sends the received data to the validator for checking
        ErrorResponse validatorResult = new TripValidator(data).validateTrip(true);

        // Checks if the validator found any errors in the data
        if (validatorResult.error()) {
            return CompletableFuture.supplyAsync(() -> badRequest(validatorResult.toJson()));
        }

        Trip trip = Json.fromJson(data.get("trip"), Trip.class);
        trip.userId = request.attrs().get(ActionState.USER).id;

        // Assemble trip data
        ArrayList<TripData> tripDataList = nodeToTripDataList(data, trip);
        trip.tripDataList = tripDataList;

        // Add new trip data to db
        return tripRepository.updateTrip(trip).thenApplyAsync(uploaded ->
                ok(Json.toJson(data.get("id").asLong()))
        );
    }

    /**
     * Deletes a trip (and all trip data) of a trip with given ID
     * @param id ID of trip to delete
     * @return 1 if trip found and deleted, 0 otherwise
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> deleteTrip(Long id) {
        // Delete trip record in trips table
        return tripRepository.deleteTrip(id).thenApplyAsync(rows ->
                ok(Json.toJson(rows)));
    }

    /**
     * Validates and inserts a trip into the database.
     * @param request Request where body is a json object of trip
     * @return JSON object containing id of newly created trip
     * @throws IOException Thrown by failure deserializing
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> insertTrip(Http.Request request) throws IOException {
        // Get the data input by the user as a JSON object
        JsonNode data = request.body().asJson();

        // Sends the received data to the validator for checking
        ErrorResponse validatorResult = new TripValidator(data).validateTrip(false);

        // Checks if the validator found any errors in the data
        if (validatorResult.error()) {
            return CompletableFuture.supplyAsync(() -> badRequest(validatorResult.toJson()));
        }

        // Assemble trip
        Trip trip = new Trip();
        trip.userId = request.attrs().get(ActionState.USER).id;
        trip.tripDataList = nodeToTripDataList(data, trip);
        return tripRepository.insertTrip(trip).thenApplyAsync(result ->
            ok(Json.toJson("Successfully added trip"))
        );
    }

    /**
     * Helper method to convert some json node of the format that is sent by the front end,
     * to an arraylist of trip data, which is much more usable by the rest of the java code.
     * By taking this approach, we are also able to avoid the issue where jackson required all
     * fields to be present when deserializing, but as per our design requirements, the
     * arrival and departure times for each point must be able to not be specified
     * @param data JSON object storing list of tripData
     * @param trip Trip object to be referenced to by tripData
     * @return Arraylist of tripData that has been deserialized from node
     */
    private ArrayList<TripData> nodeToTripDataList(JsonNode data, Trip trip){
        // Store created data points in list
        ArrayList<TripData> tripDataList = new ArrayList<>();

        // For each item in the json node, deserialize to a single trip data
        for(JsonNode node : data.get("tripDataCollection")) {
            // Assemble trip data
            TripData tripData = new TripData();

            // Assign tripdata to correct trip
            tripData.trip = trip;

            // Get position and destinationId from json object, these must be present so no need for try catch
            tripData.position = node.get("position").asLong();
            tripData.destination = new Destination();
            tripData.destination = Json.fromJson(node.get("destination"), Destination.class);

            // Try to get arrivalTime, but set to null if unable to deserialize
            try {
                tripData.arrivalTime = Json.fromJson(node.get("arrivalTime"), LocalDateTime.class);
            }
            catch(Exception e) {
                tripData.arrivalTime = null;
            }

            // Try to get departureTime, but set to null if unable to deserialize
            try {
                tripData.departureTime = Json.fromJson(node.get("departureTime"), LocalDateTime.class);
            }
            catch(Exception e) {
                tripData.departureTime = null;
            }

            // Add created tripData object to list
            tripDataList.add(tripData);
        }

        // Return create trip data list
        return tripDataList;
    }
}
