package controllers.backend;

import akka.http.javadsl.model.HttpRequest;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import models.Trip;
import models.TripData;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import repository.DestinationRepository;
import repository.TripDataRepository;
import repository.TripRepository;
import util.validation.DestinationValidator;
import util.validation.ErrorResponse;
import util.validation.TripValidator;

import javax.inject.Inject;
import java.io.IOException;
import java.security.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class TripController extends Controller {

    private final TripRepository tripRepository;
    private final TripDataRepository tripDataRepository;
    private final FormFactory formFactory;
    private final HttpExecutionContext httpExecutionContext;
    private final MessagesApi messagesApi;

    @Inject
    public TripController(FormFactory formFactory,
                         TripRepository tripRepository,
                         TripDataRepository tripDataRepository,
                         HttpExecutionContext httpExecutionContext,
                         MessagesApi messagesApi) {
        this.tripDataRepository = tripDataRepository;
        this.tripRepository = tripRepository;
        this.formFactory = formFactory;
        this.httpExecutionContext = httpExecutionContext;
        this.messagesApi = messagesApi;
    }

    public CompletableFuture<Result> getAllUserTrips(Long uid) {
        ArrayList<Long> tripIds = new ArrayList<>();

        // Trip to get the trip with a given ID
        try {
            // Query DB for trip with id
            ArrayList<Trip> trips = new ArrayList<>();
            for(Trip data: tripRepository.getAllUserTrips(uid).get()){
                trips.add(data);
            }
            // If trip returned is null, then trip with given ID exists, return bad request
            if(trips == null) {
                return CompletableFuture.supplyAsync(() -> badRequest(Json.toJson("User has no trips")));
            }
            for(Trip trip : trips) {
                tripIds.add(trip.id);
            }
        }
        // Catch error conditions from getting result
        catch (ExecutionException ex) {
            return CompletableFuture.supplyAsync(() -> status(500, Json.toJson("Could not access data from database")));
        }
        // If the operation was interrupted before completion
        catch (InterruptedException ex) {
            return CompletableFuture.supplyAsync(() -> status(500, Json.toJson("Interrupted connection with database")));
        }

        ArrayList<ObjectNode> returnedTrips = new ArrayList<>();

        for(Long tripId : tripIds) {
            try{
                ArrayList<TripData> tripDataList = new ArrayList<>();
                for(TripData tripData : tripDataRepository.getAllTripData(tripId).get()){
                    tripDataList.add(tripData);
                }
                // Create new JSON object to store returned data
                ObjectNode node = Json.newObject();
                // Put the UID that was previously found
                node.put("uid", uid);
                node.put("id", tripId);
                // Convert found trip data points to an array node
                ArrayNode array = new ObjectMapper().valueToTree(tripDataList);
                // Add array node to return json object
                node.putArray("tripDataCollection").addAll(array);
                returnedTrips.add(node);
            }
            // Catch error conditions from getting result
            catch (ExecutionException ex) {
                return CompletableFuture.supplyAsync(() -> status(500, Json.toJson("Could not access data from database")));
            }
            // If the operation was interrupted before completion
            catch (InterruptedException ex) {
                return CompletableFuture.supplyAsync(() -> status(500, Json.toJson("Interrupted connection with database")));
            }
        }
        ObjectNode returnNode = Json.newObject();
        ObjectMapper mapper = new ObjectMapper();

        ArrayNode arrayNode = mapper.valueToTree(returnedTrips);
        returnNode.putArray("allTrips").addAll(arrayNode);

        return CompletableFuture.supplyAsync(() -> ok(returnNode));
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
        // Store user ID
        Long userId;
        // Trip to get the trip with a given ID
        try {
            // Query DB for trip with id
            Trip trip = tripRepository.getTripById(id).get();
            // If trip returned is null, then trip with given ID exists, return bad request
            if(trip == null) {
                return CompletableFuture.supplyAsync(() -> badRequest(Json.toJson("No such trip")));
            }
            // If trip was found, keep track of who owned that trip (this is not stored in tripData)
            userId = trip.uid;
        }
        // Catch error conditions from getting result
        catch (ExecutionException ex) {
            return CompletableFuture.supplyAsync(() -> status(500, Json.toJson("Could not access data from database")));
        }
        // If the operation was interrupted before completion
        catch (InterruptedException ex) {
            return CompletableFuture.supplyAsync(() -> status(500, Json.toJson("Interrupted connection with database")));
        }

        // Get all the trip data (asynchronously) and then construct and return the json object to send
        return tripDataRepository.getAllTripData(id).thenApplyAsync(
                allDestinations -> { // All found tripData objects for the trip
                    // Create new JSON object to store returned data
                    ObjectNode node = Json.newObject();
                    // Put the UID that was previously found
                    node.put("uid", userId);
                    // Convert found trip data points to an array node
                    ArrayNode array = new ObjectMapper().valueToTree(allDestinations);
                    // Add array node to return json object
                    node.putArray("tripDataCollection").addAll(array);
                    // Return ok status with return json object
                    return ok(node);
                });
    }

    /**
     * Validates and then updates a trip on the database. This is done by dropping all tripData already linked to the trip
     * and adding all new data. Trying to update individual rows would lead to all sorts of potential traps for data corruption.
     * @param request Request containing JSON trip object to update
     * @return Returns trip id (as json) on success, otherwise bad request
     * @throws IOException
     */
    public CompletableFuture<Result> updateTrip(Http.Request request) throws IOException {
        // Get the data input by the user as a JSON object
        JsonNode data = request.body().asJson();

        // Sends the received data to the validator for checking
        ErrorResponse validatorResult = new TripValidator(data).validateTrip(true);

        // Checks if the validator found any errors in the data
        if (validatorResult.error()) {
            return CompletableFuture.supplyAsync(() -> badRequest(validatorResult.toJson()));
        }

        // Delete all existing trip data
        try {
            tripDataRepository.deleteAllTripData(data.get("id").asLong()).get();
        }
        // Catch error conditions from getting result
        catch (ExecutionException ex) {
            return CompletableFuture.supplyAsync(() -> status(500, Json.toJson("Could not access data from database")));
        }
        // If operation was interrupted before completion
        catch (InterruptedException ex) {
            return CompletableFuture.supplyAsync(() -> status(500, Json.toJson("Interrupted connection with database")));
        }

        // Assemble trip data
        ArrayList<TripData> tripDataList = nodeToTripDataList(data, data.get("id").asLong());

        // Add new trip data to db
        return CompletableFuture.supplyAsync(() -> {
            tripDataRepository.insertTripDataList(tripDataList);
            // Return json id of updated trip
            return ok(Json.toJson(data.get("id").asLong()));
        });
    }

    /**
     * Deletes a trip (and all trip data) of a trip with given ID
     * @param id ID of trip to delete
     * @return 1 if trip found and deleted, 0 otherwise
     */
    public CompletableFuture<Result> deleteTrip(Long id) {
        // Delete all existing trip data
        try {
            tripDataRepository.deleteAllTripData(id).get();
        }
        // Catch error conditions from getting result
        catch (ExecutionException ex) {
            return CompletableFuture.supplyAsync(() -> status(500, Json.toJson("Could not access data from database")));
        }
        // If operation was interrupted before completion
        catch (InterruptedException ex) {
            return CompletableFuture.supplyAsync(() -> status(500, Json.toJson("Interrupted connection with database")));
        }

        // Now delete trip record in trips table
        return tripRepository.deleteTrip(id).thenApplyAsync(rows ->
                ok(Json.toJson(rows)));
    }

    /**
     * Validates and inserts a trip into the database.
     * @param request Request where body is a json object of trip
     * @return JSON object containing id of newly created trip
     * @throws IOException Thrown by failure deserializing
     */
    public CompletableFuture<Result> insertTrip(Http.Request request) throws IOException {
        // Get the data input by the user as a JSON object
        JsonNode data = request.body().asJson();
        System.out.println(data);
        // Sends the received data to the validator for checking
        ErrorResponse validatorResult = new TripValidator(data).validateTrip(false);
        // Checks if the validator found any errors in the data
        if (validatorResult.error()) {
            return CompletableFuture.supplyAsync(() -> badRequest(validatorResult.toJson()));
        }

        // Assemble trip
        Trip trip = new Trip();
        trip.uid = data.get("uid").asLong();

        // Store result of trip adding operation
        Result tripAddResult;
        // Attempt to add the trip to database, and (by blocking) get the result
        try {
            tripAddResult = tripRepository.insertTrip(trip).get();
        }
        // Catch error conditions from getting result
        catch (ExecutionException ex) {
            return CompletableFuture.supplyAsync(() -> status(500, Json.toJson("Could not access data from database")));
        }
        catch (InterruptedException ex) {
            return CompletableFuture.supplyAsync(() -> status(500, Json.toJson("Interrupted connection with database")));
        }

        // Assemble trip data
        ArrayList<TripData> tripDataList = nodeToTripDataList(data, trip.id);
        System.out.println("3");

        // Add trip to db
        if(tripAddResult.status() == ok().status()) {
            return CompletableFuture.supplyAsync(() -> {
                tripDataRepository.insertTripDataList(tripDataList);
                return ok(Json.toJson(trip.id));
            });
        } else {
            return CompletableFuture.supplyAsync(() -> internalServerError());
        }

    }

    /**
     * Helper method to convert some json node of the format that is sent by the front end,
     * to an arraylist of trip data, which is much more usable by the rest of the java code.
     * By taking this approach, we are also able to avoid the issue where jackson required all
     * fields to be present when deserializing, but as per our design requirements, the
     * arrival and departure times for each point must be able to not be specified
     * @param data JSON object storing list of tripData
     * @param tripId ID of trip to set to tripData objects
     * @return Arraylist of tripData that has been deserialized from node
     */
    private ArrayList<TripData> nodeToTripDataList(JsonNode data, Long tripId){
        // Store created data points in list
        ArrayList<TripData> tripDataList = new ArrayList<>();

        // For each item in the json node, deserialize to a single trip data
        for(JsonNode node : data.get("tripDataCollection")) {
            // Assemble trip data
            TripData tripData = new TripData();
            tripData.key = new TripData.TripDataKey();
            tripData.key.tripId = tripId;

            // Get position and destinationId from json object, these must be present so no need for try catch
            tripData.key.position = node.get("position").asLong();
            tripData.destinationId = node.get("destinationId").asLong();

            // Try to get arrivalTime, but set to null if unable to deserialize
            try {
                tripData.arrivalTime = Json.fromJson(node.get("arrivalTime"), Timestamp.class);
            }
            catch(Exception e) {
                tripData.arrivalTime = null;
            }

            // Try to get departureTime, but set to null if unable to deserialize
            try {
                tripData.departureTime = Json.fromJson(node.get("departureTime"), Timestamp.class);
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
