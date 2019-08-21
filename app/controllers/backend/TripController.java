package controllers.backend;

import actions.ActionState;
import actions.Authenticator;
import actions.roles.Everyone;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.inject.Inject;
import models.Destination;
import models.Tag;
import models.Trip;
import models.TripData;
import models.User;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import play.routing.JavaScriptReverseRouter;
import repository.DestinationRepository;
import repository.TagRepository;
import repository.TripRepository;
import repository.UserRepository;
import util.objects.PagingResponse;
import util.validation.ErrorResponse;
import util.validation.TripValidator;

/**
 * Manages trips in the database.
 */
public class TripController extends TEABackController {

    private static final String IS_PUBLIC = "isPublic";
    private final TripRepository tripRepository;
    private final DestinationRepository destinationRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    @Inject
    public TripController(TripRepository tripRepository, TagRepository tagRepository,
        DestinationRepository destinationRepository, UserRepository userRepository) {

        this.tripRepository = tripRepository;
        this.destinationRepository = destinationRepository;
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
    }

    /**
     * Attempts to get all user trips for a given userID.
     *
     * @param request the HTTP request
     * @return JSON object with list of trips that a user has, bad request if user does not exist
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> getAllUserTrips(Http.Request request, Long userId) {
        User loggedInUser = request.attrs().get(ActionState.USER);

        // Returns all trips if requesting user is owner of trips or an admin
        if (loggedInUser.admin || loggedInUser.id.equals(userId)) {
            return tripRepository.getAllUserTrips(userId)
                .thenApplyAsync(trips -> {
                    Collections.sort(trips);
                    try {
                        return ok(sanitizeJson(Json.toJson(trips)));
                    } catch (IOException e) {
                        return internalServerError(Json.toJson(SANITIZATION_ERROR));
                    }
                });
        } else {
            return tripRepository.getAllPublicUserTrips(userId)
                .thenApplyAsync(trips -> {
                    Collections.sort(trips);
                    try {
                        return ok(sanitizeJson(Json.toJson(trips)));
                    } catch (IOException e) {
                        return internalServerError(Json.toJson(SANITIZATION_ERROR));
                    }
                });
        }
    }

    /**
     * Gets all trips if user is admin, otherwise gets all public trips.
     *
     * @return JSON object with list of trips that a user has, bad request if user has no trips
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> getAllTrips(Http.Request request,
                                                Long userId,
                                                String searchQuery,
                                                Boolean ascending,
                                                Integer pageNum,
                                                Integer pageSize,
                                                Integer requestOrder) {
        User user = request.attrs().get(ActionState.USER);
        
        // By default, only get public trips
        boolean getPrivate = false;

        // If user is admin, or getting their own trips, show all results not just public
        if(user.admin || (userId != -1 && userId == user.id)) {
            getPrivate = true;
        }

        return tripRepository.searchTrips(userId, user.id, searchQuery, ascending, pageNum, pageSize, getPrivate, userId == -1)
            .thenApplyAsync(trips -> {
                try {
                    return ok(sanitizeJson(Json.toJson(
                        new PagingResponse<>(trips.getList(), requestOrder, trips.getTotalPageCount())
                    )));
                } catch (IOException e) {
                    return internalServerError(Json.toJson(SANITIZATION_ERROR));
                }});
    }

    /**
     * Attempts to fetch all data for a trip with given trip ID. This is returned as a JSON object
     * with 2 fields: uid: This field represents the id of the user who owns the trip tripDataList:
     * An array storing all stages of the trip as tripData objects
     *
     * @param tripId ID of trip to find
     * @return JSON object with uid and trip data
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> getTrip(Http.Request request, Long tripId) {
        User user = request.attrs().get(ActionState.USER);

        return tripRepository.getTripById(tripId).thenApplyAsync(
            trip -> {
                // If trip was not found in database
                if (trip == null) {
                    return notFound();
                }
                // If trip was found and logged in user has privileges to retrieve trip
                else if (user.admin || user.id.equals(trip.userId) || trip.isPublic) {
                    try {
                        return ok(sanitizeJson(Json.toJson(trip)));
                    } catch (IOException e) {
                        return internalServerError(Json.toJson(SANITIZATION_ERROR));
                    }
                }
                // If logged in user does not have privileges to retrieve trip
                else {
                    return forbidden();
                }
            });
    }

    /**
     * Validates and then updates a trip on the database. This is done by dropping all tripData
     * already linked to the trip and adding all new data. Trying to update individual rows would
     * lead to all sorts of potential traps for data corruption.
     *
     * @param request Request containing JSON trip object to update
     * @return Returns trip id (as json) on success, otherwise bad request
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> updateTrip(Http.Request request) throws IOException {
        // Get the data input by the user as a JSON object
        JsonNode data = request.body().asJson();

        User user = request.attrs().get(ActionState.USER);

        // Sends the received data to the validator for checking
        ErrorResponse validatorResult = new TripValidator(data).validateTrip(true);

        // Checks if the validator found any errors in the data
        if (validatorResult.error()) {
            return CompletableFuture.supplyAsync(() -> badRequest(validatorResult.toJson()));
        }

        // Assemble trip
        Trip trip = new Trip();
        trip.id = data.get("id").asLong();
        trip.userId = data.get("userId").asLong();
        trip.tripDataList = nodeToTripDataList(data, trip);
        trip.isPublic = data.get(IS_PUBLIC).asBoolean();
        trip.tags = new HashSet<>(Arrays.asList(Json.fromJson(new ObjectMapper().readTree(data.get("tags").toString()), Tag[].class)));

        // Transfers ownership of destinations to master admin where necessary
        transferDestinationsOwnership(trip.userId, trip.tripDataList);

        // Update trip in db
        return tripRepository.getTripById(trip.id).thenComposeAsync(oldTrip -> {
            if (oldTrip == null) {
                return CompletableFuture
                    .supplyAsync(() -> notFound(Json.toJson("Trip with provided ID not found")));
            } else {
                return tagRepository.addTags(trip.tags).thenComposeAsync(existingTags -> {
                    userRepository.updateUsedTags(user, oldTrip, trip);
                    trip.tags = existingTags;
                    return tripRepository.updateTrip(trip).thenApplyAsync(uploaded -> {
                        if (uploaded) {
                            return ok(Json.toJson(trip.id));
                        } else {
                            return notFound();
                        }
                    });
                });
            }
        });
    }

    /**
     * Updates the privacy of a trip.
     *
     * @param request Request containing JSON data of trip to update
     * @return Returns ok with trip id on success, otherwise bad request
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> updateTripPrivacy(Http.Request request) {
        // Get logged in users data
        User user = request.attrs().get(ActionState.USER);

        // Get the data input by the user as a JSON object
        JsonNode data = request.body().asJson();

        // Sends the received data to the validator for checking
        ErrorResponse validatorResult = new TripValidator(data).validateTripPrivacyUpdate();

        // Checks if the validator found any errors in the data
        if (validatorResult.error()) {
            return CompletableFuture.supplyAsync(() -> badRequest(validatorResult.toJson()));
        }

        Trip updatedTrip = Json.fromJson(data, Trip.class);

        // Get existing trip details
        return tripRepository.getTripById(updatedTrip.id).thenComposeAsync(existingTrip -> {
            if (existingTrip == null) {
                return CompletableFuture.supplyAsync(() -> notFound(Json.toJson("Not Found")));
            } else if (!user.admin && !user.id.equals(existingTrip.userId)) {
                return CompletableFuture.supplyAsync(() -> forbidden(Json.toJson("Forbidden")));
            } else {
                // Update trip in db
                return tripRepository.updateTrip(updatedTrip).thenApplyAsync(uploaded ->
                    ok(Json.toJson(existingTrip))
                );
            }
        });
    }

    /**
     * Toggles the soft deletion status of a trip with given ID.
     *
     * @param tripId ID of trip to toggle deletion
     * @return On success, ID of trip, otherwise 401, 403, or 404 error code
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> deleteTrip(Http.Request request, Long tripId) {
        // Gets logged in users details
        User user = request.attrs().get(ActionState.USER);

        return tripRepository.getDeletedTrip(tripId).thenComposeAsync(trip -> {
            if (trip == null) {
                return CompletableFuture.supplyAsync(() -> notFound("No such trip exists"));
            } else if (!user.admin && !user.id.equals(trip.userId)) {
                return CompletableFuture.supplyAsync(() -> forbidden(
                    "You do not have permission to delete a trip for someone else"));
            } else {
                // Toggle deleted boolean and update
                trip.deleted = !trip.deleted;
                return tripRepository.updateTrip(trip).thenApplyAsync(updatedTrip ->
                    ok(Json.toJson(tripId)));
            }
        });
    }

    /**
     * Validates and inserts a trip into the database.
     *
     * @param request Request where body is a json object of trip
     * @return JSON object containing id of newly created trip
     * @throws IOException Thrown by failure deserializing
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> insertTrip(Http.Request request) throws IOException {
        // Get the data input by the user as a JSON object
        JsonNode data = request.body().asJson();
        User user = request.attrs().get(ActionState.USER);

        // Sends the received data to the validator for checking
        ErrorResponse validatorResult = new TripValidator(data).validateTrip(false);

        // Checks if the validator found any errors in the data
        if (validatorResult.error()) {
            return CompletableFuture.supplyAsync(() -> badRequest(validatorResult.toJson()));
        }

        // Assemble trip
        Trip trip = new Trip();
        trip.userId = data.get("userId").asLong();
        trip.tripDataList = nodeToTripDataList(data, trip);
        trip.isPublic = data.get(IS_PUBLIC).asBoolean();
        trip.tags = new HashSet<>(Arrays.asList(Json.fromJson(new ObjectMapper().readTree(data.get("tags").toString()), Tag[].class)));

        // Transfers ownership of destinations to master admin where necessary
        transferDestinationsOwnership(trip.userId, trip.tripDataList);
        return tagRepository.addTags(trip.tags).thenComposeAsync(existingTags -> {
            userRepository.updateUsedTags(user, trip);
            trip.tags = existingTags;
            return tripRepository.insertTrip(trip).thenApplyAsync(tripId ->
                ok(Json.toJson(tripId)));
        });

    }

    /**
     * Helper method to convert some json node of the format that is sent by the front end, to an
     * array list of trip data, which is much more usable by the rest of the java code. By taking
     * this approach, we are also able to avoid the issue where jackson required all fields to be
     * present when deserializing, but as per our design requirements, the arrival and departure
     * times for each point must be able to not be specified
     *
     * @param data JSON object storing list of tripData
     * @param trip Trip object to be referenced to by tripData
     * @return Array list of tripData that has been deserialized from node
     */
    private ArrayList<TripData> nodeToTripDataList(JsonNode data, Trip trip) {
        // Store created data points in list
        ArrayList<TripData> tripDataList = new ArrayList<>();

        // For each item in the json node, deserialize to a single trip data
        for (JsonNode node : data.get("tripDataList")) {
            // Assemble trip data
            TripData tripData = new TripData();

            // Assign trip data to correct trip
            tripData.trip = trip;

            // Get position and destinationId from json object,
            // must be present so no need for try catch
            tripData.position = node.get("position").asLong();
            tripData.destination = new Destination();
            tripData.destination = Json.fromJson(node.get("destination"), Destination.class);

            // Try to get arrivalTime, but set to null if unable to deserialize
            try {
                tripData.arrivalTime = Json.fromJson(node.get("arrivalTime"), LocalDateTime.class);
            } catch (Exception e) {
                tripData.arrivalTime = null;
            }

            // Try to get departureTime, but set to null if unable to deserialize
            try {
                tripData.departureTime = Json
                    .fromJson(node.get("departureTime"), LocalDateTime.class);
            } catch (Exception e) {
                tripData.departureTime = null;
            }

            // Add created tripData object to list
            tripDataList.add(tripData);
        }
        // Return create trip data list
        return tripDataList;
    }

    /**
     * Transfers ownership of destinations used in trip to master admin if used by another user.
     *
     * @param userId Owner of trip being created or updated
     * @param destinations List of tripData objects used in trip
     */
    private void transferDestinationsOwnership(Long userId, List<TripData> destinations) {
        List<Long> destIds = destinations.stream().map(x -> x.destination.id)
            .collect(Collectors.toList());
        destinationRepository
            .updateDestinationOwnershipUsedInTrip(destIds, userId, MASTER_ADMIN_ID);
    }

    /**
     * Lists routes to put in JS router for use from frontend.
     *
     * @return JSRouter Play result
     */
    public Result tripRoutes(Http.Request request) {
        return ok(
            JavaScriptReverseRouter.create("tripRouter", "jQuery.ajax", request.host(),
                controllers.backend.routes.javascript.TripController.deleteTrip(),
                controllers.backend.routes.javascript.TripController.getAllUserTrips(),
                controllers.frontend.routes.javascript.TripController.editTrip(),
                controllers.backend.routes.javascript.TripController.getAllTrips(),
                controllers.backend.routes.javascript.TripController.getTrip(),
                controllers.backend.routes.javascript.TripController.updateTripPrivacy(),
                controllers.backend.routes.javascript.TripController.updateTrip(),
                controllers.backend.routes.javascript.TripController.insertTrip()
            )
        ).as(Http.MimeTypes.JAVASCRIPT);
    }
}