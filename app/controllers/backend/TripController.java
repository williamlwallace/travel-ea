package controllers.backend;

import com.fasterxml.jackson.databind.JsonNode;
import models.Trip;
import models.TripData;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import repository.TripDataRepository;
import repository.TripRepository;
import util.validation.ErrorResponse;
import util.validation.TripValidator;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Manages trips in the database
 */
public class TripController extends Controller {

    private final TripRepository tripRepository;
    private final TripDataRepository tripDataRepository;

    @Inject
    public TripController(TripRepository tripRepository,
                          TripDataRepository tripDataRepository) {
        this.tripDataRepository = tripDataRepository;
        this.tripRepository = tripRepository;
    }

    /**
     * Attempts to get all user trips for a given userID.
     * @param userId the ID of the user
     * @return JSON object with list of trips that a user has, bad request if user has no trips.
     */
    public CompletableFuture<Result> getAllUserTrips(Long userId) {
        return tripRepository.getAllUserTrips(userId)
                .thenApplyAsync(tripList -> {
                    if (tripList.size() < 1) {
                        ErrorResponse errorResponse = new ErrorResponse();
                        errorResponse.map("User has no trips", "other");
                        return notFound(errorResponse.toJson());
                    } else {
                        ArrayList<CompletableFuture<List<TripData>>> futures = new ArrayList<>();
                        for (Trip trip : tripList) {
                            futures.add(tripDataRepository.getAllTripData(trip.id));
                        }
                        CompletableFuture cf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
                        cf.join();

                        ArrayList<List<TripData>> result = new ArrayList<>();
                        for (CompletableFuture<List<TripData>> future : futures) {
                            result.add(future.join());
                        }
                        return ok(Json.toJson(result));
                    }
                });
    }

    /**
     * Attempts to fetch all data for a trip with given trip ID. This is returned as a JSON
     * object with 2 fields:
     * uid: This field represents the id of the user who owns the trip
     * tripDataCollection: An array storing all stages of the trip as tripData objects
     * @param tripId ID of trip to find
     * @return JSON object with uid and trip data
     */
    public CompletableFuture<Result> getTrip(Long tripId) {
        return tripDataRepository.getAllTripData(tripId)
                .thenApplyAsync(tripData -> ok(Json.toJson(tripData)));
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
        } else {
            Trip updatedTrip = Json.fromJson(data, Trip.class);
            return tripDataRepository.deleteAllTripData(updatedTrip.id)
                    .thenComposeAsync(deletedRows -> {
                        if (deletedRows < 1) {
                            return null;
                        } else {
                            return tripDataRepository.insertTripDataList(updatedTrip.tripDataList, updatedTrip.id);
                        }
                    })
                    .thenApplyAsync(tripId -> {
                        if (tripId == null) {
                            validatorResult.map("Bad request", "other");
                            return badRequest(validatorResult.toJson());
                        } else {
                            return ok(Json.toJson(tripId));
                        }
                    });
        }

    }

    /**
     * Deletes a trip (and all trip data) of a trip with given ID
     * @param tripId ID of trip to delete
     * @return 1 if trip found and deleted, 0 otherwise
     */
    public CompletableFuture<Result> deleteTrip(Long tripId) {
        return tripDataRepository.deleteAllTripData(tripId)
                .thenComposeAsync(rowsDeletedTripData -> {
                    if (rowsDeletedTripData < 1) {
                        return null;
                    } else {
                        return tripRepository.deleteTrip(tripId);
                    }
                }).thenApplyAsync(rowsDeletedTrip -> {
                    if (rowsDeletedTrip == null) {
                        ErrorResponse errorResponse = new ErrorResponse();
                        errorResponse.map("Error deleting trip data, trip id not found", "other");
                        return badRequest(errorResponse.toJson());
                    } else if (rowsDeletedTrip < 1) {
                        ErrorResponse errorResponse = new ErrorResponse();
                        errorResponse.map("Error deleting trip, trip id not found", "other");
                        return badRequest(errorResponse.toJson());
                    } else {
                        return ok(Json.toJson(rowsDeletedTrip));
                    }
                });
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

        // Sends the received data to the validator for checking
        ErrorResponse validatorResult = new TripValidator(data).validateTrip(false);

        // Checks if the validator found any errors in the data
        if (validatorResult.error()) {
            return CompletableFuture.supplyAsync(() -> badRequest(validatorResult.toJson()));
        } else {
            Trip newTrip = Json.fromJson(data, Trip.class);

            return tripRepository.insertTrip(newTrip)
                    .thenComposeAsync(newTripId -> {
                        if (newTripId < 1) {
                            return null;
                        } else {
                            return tripDataRepository.insertTripDataList(newTrip.tripDataList, newTripId);
                        }
                    }).thenApplyAsync(tripId -> {
                        if (tripId == null) {
                            ErrorResponse errorResponse = new ErrorResponse();
                            errorResponse.map("Error inserting new trip", "other");
                            return internalServerError(errorResponse.toJson());
                        } else {
                            return ok(Json.toJson(tripId));
                        }
                    });
        }
    }
}