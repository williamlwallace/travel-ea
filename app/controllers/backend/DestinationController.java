package controllers.backend;

import actions.ActionState;
import actions.Authenticator;
import actions.roles.Admin;
import actions.roles.Everyone;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import javax.inject.Inject;
import models.Destination;
import models.User;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import play.routing.JavaScriptReverseRouter;
import repository.DestinationRepository;
import repository.TagRepository;
import repository.PhotoRepository;
import repository.TravellerTypeDefinitionRepository;
import repository.UserRepository;
import util.validation.DestinationValidator;
import util.validation.ErrorResponse;

/**
 * Manages destinations in the database.
 */
public class DestinationController extends TEABackController {

    private static final String DEST_NOT_FOUND = "Destination with provided ID not found";
    private final DestinationRepository destinationRepository;
    private final TravellerTypeDefinitionRepository travellerTypeDefinitionRepository;
    private final WSClient ws;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final PhotoRepository photoRepository;


    @Inject
    public DestinationController(DestinationRepository destinationRepository,
        TravellerTypeDefinitionRepository travellerTypeDefinitionRepository, WSClient ws,
        TagRepository tagRepository, UserRepository userRepository,
        PhotoRepository photoRepository) {
        this.destinationRepository = destinationRepository;
        this.travellerTypeDefinitionRepository = travellerTypeDefinitionRepository;
        this.ws = ws;
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
        this.photoRepository = photoRepository;
    }

    /**
     * Adds a new destination to the database. Destination object to be added must be a json object
     * in the request of the body
     *
     * @param request Request containing destination json object as body
     * @return Ok with id of destination on success, badRequest otherwise
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> addNewDestination(Http.Request request) {
        JsonNode data = request.body().asJson();
        User user = request.attrs().get(ActionState.USER);

        // Sends the received data to the validator for checking, if error returns bad request
        ErrorResponse validatorResult = new DestinationValidator(data).validateDestination(false);
        if (validatorResult.error()) {
            return CompletableFuture.supplyAsync(() -> badRequest(validatorResult.toJson()));
        }

        // Add destination owner to be whichever user uploaded it
        Destination newDestination = Json.fromJson(data, Destination.class);

        // Checks if user logged in is not allowed to create dest for userId
        if (!user.admin && !user.id.equals(newDestination.user.id)) {
            return CompletableFuture.supplyAsync(() -> forbidden(Json.toJson(
                "You do not have permission to create a destination for someone else")));
        }

        // Checks if similar destination already exists
        List<Destination> destinations = destinationRepository
            .getSimilarDestinations(newDestination);
        for (Destination destination : destinations) {
            if (destination.user.id.equals(user.id) || destination.isPublic) {
                return CompletableFuture
                    .supplyAsync(() -> badRequest(Json.toJson("Duplicate destination")));
            }
        }

        return tagRepository.addTags(newDestination.tags).thenComposeAsync(existingTags -> {
            userRepository.updateUsedTags(user, newDestination);
            newDestination.tags = existingTags;
            return destinationRepository.addDestination(newDestination)
                .thenApplyAsync(id -> {
                    try {
                        return ok(sanitizeJson(Json.toJson(id)));
                    } catch (IOException e) {
                        return internalServerError(Json.toJson(SANITIZATION_ERROR));
                    }
                });
        });
    }

    /**
     * Allows a user to mark one of their destinations as public. This will cause it to become
     * immediately visible to all other users, as well as merging with any sufficiently similar
     * destinations that are currently marked as private in the database
     *
     * @param request Request containing authentication header
     * @param id ID of destination to mark as public
     * @return 200 if successful, 400 if already public, 401 unauthorized, 403 forbidden, 404 no
     * such destination
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> makeDestinationPublic(Http.Request request, Long id) {
        User user = request.attrs().get(ActionState.USER);
        return destinationRepository.getDestination(id).thenApplyAsync(destination -> {
            // Check for 404, i.e the destination to make public doesn't exist
            if (destination == null) {
                return notFound(Json.toJson("No such destination exists"));
            }
            // Check if user owns the destination (or is an admin)
            if (!destination.user.id.equals(user.id) && !user.admin) {
                return forbidden(Json.toJson("You are not allowed to perform this action"));
            }
            // Check if destination was already public
            if (destination.isPublic) {
                return badRequest(Json.toJson("Destination was already public"));
            }

            destinationRepository.setDestinationToPublicInDatabase(destination.id);

            // Find all similar destinations that need to be merged and collect only their IDs
            List<Destination> destinations = destinationRepository
                .getSimilarDestinations(destination);
            List<Long> similarIds = destinations.stream().map(x -> x.id)
                .collect(Collectors.toList());

            // Re-reference each instance of the old destinations to the new one, keeping track of how many rows were changed
            // TripData
            int rowsChanged = destinationRepository
                .mergeDestinationsTripData(similarIds, destination.id);
            // Photos
            rowsChanged += destinationRepository
                .mergeDestinationsPhotos(similarIds, destination.id);

            // If any rows were changed when re-referencing, the destination
            // has been used by another user and must be transferred to admin ownership
            if (rowsChanged > 0) {
                destinationRepository.changeDestinationOwner(destination.id, MASTER_ADMIN_ID);
            }

            // Once all old usages have been re-referenced, delete the found similar destinations
            for (Long simId : similarIds) {
                destinationRepository.deleteDestination(simId);
            }

            return ok("Successfully made destination public, and re-referenced " + rowsChanged
                + " to new public destination");
        });
    }

    /**
     * Deletes a destination with given id. Return a result with a json int which represents the
     * number of rows that were deleted. So if the return value is 0, no destination was found to
     * delete
     *
     * @param id ID of destination to delete
     * @return OK with number of rows deleted, bad request if none deleted
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> deleteDestination(Http.Request request, Long id) {
        User user = request.attrs().get(ActionState.USER);
        return destinationRepository.getDeletedDestination(id).thenComposeAsync(destination -> {
            if (destination == null) {
                return CompletableFuture.supplyAsync(() -> notFound("No such destination found"));
            } else if (destination.user.id.equals(user.id) || user.admin) {
                destination.deleted = !destination.deleted;
                return destinationRepository.updateDestination(destination)
                    .thenApplyAsync(upId -> ok(
                        Json.toJson(
                            "Successfully toggled destination deletion of destination with id: "
                                + upId)));
            } else {
                return CompletableFuture.supplyAsync(() -> forbidden("Forbidden"));
            }
        });
    }

    /**
     * Edits a destination's details with given id.
     *
     * @param request The request
     * @param id The id of the destination to edit
     * @return 400 is the request is bad, 404 if the destination is not found, 500 if sanitization
     * fails, 403 if the user cannot edit the destination and 200 if successful
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> editDestination(Http.Request request, Long id) {
        JsonNode data = request.body().asJson();
        User user = request.attrs().get(ActionState.USER);
        ErrorResponse validatorResult = new DestinationValidator(data).validateDestination(true);
        return destinationRepository.getDestination(id).thenComposeAsync(destination -> {
            if (destination == null) {
                return CompletableFuture
                    .supplyAsync(() -> notFound(Json.toJson(DEST_NOT_FOUND)));
            } else if (destination.user.id.equals(user.id) || user.admin) {
                if (validatorResult.error()) {
                    return CompletableFuture
                        .supplyAsync(() -> badRequest(validatorResult.toJson()));
                }

                // Build destination object and set required fields
                Destination editedDestination = Json.fromJson(data, Destination.class);
                editedDestination.id = id;

                // Check if destination already exists, if so rejects update
                List<Destination> destinations = destinationRepository
                    .getSimilarDestinations(editedDestination);
                for (Destination dest : destinations) {
                    if (dest.user.id.equals(user.id) || dest.isPublic) {
                        return CompletableFuture.supplyAsync(() -> badRequest(
                            Json.toJson("Another destination with these details already exists")));
                    }
                }

                return tagRepository.addTags(editedDestination.tags)
                    .thenComposeAsync(existingTags -> {
                        userRepository.updateUsedTags(user, destination, editedDestination);
                        editedDestination.tags = existingTags;
                        return destinationRepository.updateDestination(editedDestination)
                            .thenApplyAsync(updatedDestination -> {
                                try {
                                    return ok(sanitizeJson(Json.toJson(destination)));
                                } catch (IOException e) {
                                    return internalServerError(Json.toJson(SANITIZATION_ERROR));
                                }
                            });
                    });
            } else {
                return CompletableFuture.supplyAsync(() -> forbidden(
                    Json.toJson(
                        "Forbidden, user does not have permission to edit this destination")));
            }
        });
    }

    /**
     * Toggles whether a traveller type is linked to a destination. If the user does not have
     * permission to change, a request to modify will be stored instead.
     *
     * @param request Http request containing authentication information
     * @param destId ID of destination to toggle traveller type for
     * @param travellerTypeId ID of traveller type to add/remove from destination
     * @return Response result containing success/error message
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> toggleDestinationTravellerType(Http.Request request,
        Long destId, Long travellerTypeId) {
        User user = request.attrs().get(ActionState.USER);
        return destinationRepository.getDestination(destId).thenComposeAsync(destination -> {
            if (destination == null) {
                return CompletableFuture.supplyAsync(() -> notFound(Json.toJson(DEST_NOT_FOUND)));
            }

            return travellerTypeDefinitionRepository.getTravellerTypeDefinitionById(travellerTypeId)
                .thenComposeAsync(travellerType -> {
                    if (travellerType == null) {
                        return CompletableFuture.supplyAsync(() -> notFound(
                            Json.toJson("Traveller Type with provided ID not found")));
                    }

                    String message;
                    String fromOrTo;

                    // If destination is linked to traveller type
                    if (destination.isLinkedTravellerType(travellerTypeId)) {
                        // If user is allowed to unlink traveller type from destination
                        if (destination.user.id.equals(user.id) || user.admin) {
                            destination.travellerTypes.remove(travellerType);
                            if (destination.isPendingTravellerType(travellerTypeId)) {
                                destination.removePendingTravellerType(travellerTypeId);
                            }
                            message = "removed";
                        }
                        // If user is not allowed to unlink traveller type from destination
                        else {
                            // If request already exists
                            if (destination.isPendingTravellerType(travellerTypeId)) {
                                return CompletableFuture.supplyAsync(() -> ok(Json.toJson(
                                    "Successfully requested to remove traveller type from destination")));
                            } else {
                                destination.travellerTypesPending.add(travellerType);
                                message = "requested to remove";
                            }
                        }
                        fromOrTo = "from";
                    }
                    // If destination is not linked to traveller type
                    else {
                        // If user is allowed to link traveller type to destination
                        if (destination.user.id.equals(user.id) || user.admin) {
                            destination.travellerTypes.add(travellerType);
                            if (destination.isPendingTravellerType(travellerTypeId)) {
                                destination.removePendingTravellerType(travellerTypeId);
                            }
                            message = "added";
                        }
                        // If user must request to link traveller type to destination
                        else {
                            // If request already exists
                            if (destination.isPendingTravellerType(travellerTypeId)) {
                                return CompletableFuture.supplyAsync(() -> ok(Json.toJson(
                                    "Successfully requested to add traveller type to destination")));
                            } else {
                                destination.travellerTypesPending.add(travellerType);
                                message = "requested to add";
                            }
                        }
                        fromOrTo = "to";
                    }

                    return destinationRepository.updateDestination(destination)
                        .thenApplyAsync(rows -> ok(Json.toJson(
                            "Successfully " + message + " traveller type " + fromOrTo
                                + " destination")));
                });
        });
    }

    /**
     * Toggles a request for a traveller type to be linked/unlinked from a destination.
     *
     * @param request The request
     * @param destId The id of the destination
     * @param travellerTypeId The id of the traveller type
     * @return ok is traveller type request toggled, forbidden if user not admin, not found if
     * destination or traveller type does not exist
     */
    @With({Admin.class, Authenticator.class})
    public CompletableFuture<Result> toggleRejectTravellerType(Http.Request request, Long destId,
        Long travellerTypeId) {
        User user = request.attrs().get(ActionState.USER);
        return destinationRepository.getDestination(destId).thenComposeAsync(dest -> {
            if (dest == null) {
                return CompletableFuture.supplyAsync(() -> notFound(Json.toJson(DEST_NOT_FOUND)));
            }

            return travellerTypeDefinitionRepository.getTravellerTypeDefinitionById(travellerTypeId)
                .thenComposeAsync(travellerType -> {
                    if (travellerType == null) {
                        return CompletableFuture.supplyAsync(() -> notFound(
                            Json.toJson("Traveller Type with provided ID not found")));
                    } else {
                        if (dest.isPendingTravellerType(travellerTypeId)) {
                            dest.removePendingTravellerType(travellerTypeId);
                        } else {
                            dest.addPendingTravellerType(travellerTypeId);
                        }

                        return destinationRepository.updateDestination(dest).thenApplyAsync(
                            rows -> ok(
                                Json.toJson("Successfully rejected traveller type modification")));
                    }
                });
        });
    }

    /**
     * Changes the photo set as the primary photo for a destination. If the user does not own the
     * destination or isn't an admin, then a request to modify is stored instead.
     *
     * @param request Http request containing authentication information
     * @param destId ID of destination to change primary photo of
     * @param photoId Id of photo to add as primary photo for destination
     * @return Response result containing success/error message
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> changeDestinationPrimaryPhoto(Http.Request request,
        Long destId, Long photoId) {
        User user = request.attrs().get(ActionState.USER);
        return destinationRepository.getDestination(destId).thenComposeAsync(destination -> {
            if (destination == null) {
                return CompletableFuture.supplyAsync(() -> notFound(Json.toJson(DEST_NOT_FOUND)));
            }

            return photoRepository.getPhotoById(photoId)
                .thenComposeAsync(photo -> {
                    if (photo == null) {
                        return CompletableFuture.supplyAsync(
                            () -> notFound(Json.toJson("Photo with provided ID not found")));
                    }

                    JsonNode oldDestination = Json.toJson(destination);

                    // Currently a photo can only be set/requested to set as the destination primary
                    // photo by the owner of the photo or an admin. This is because if a user requests
                    // for someone else's photo to be destination primary photo, admin will probably
                    // just accept, without the user's agreement. Also if this user then makes their
                    // photo private again it should no longer be the destination primary photo.
                    // This adds too many complications so it is better to only allow the owner of the
                    // photo or an admin to set/request the photo to become destination primary photo.

                    // If user is destination owner and photo owner, or if user is admin, set the photo
                    if ((destination.user.id.equals(user.id) && photo.userId.equals(user.id))
                        || user.admin) {
                        destination.primaryPhoto = photo;
                        if (destination.hasPhotoPending(photoId)) {
                            destination.removePendingDestinationPrimaryPhoto(photoId);
                        }
                    }
                    // If user is photo owner and wants the photo on the destination, request to set photo
                    else if (photo.userId.equals(user.id)) {
                        // If request already exists
                        if (destination.hasPhotoPending(photoId)) {
                            return CompletableFuture
                                .supplyAsync(() -> ok(Json.toJson(oldDestination)));
                        } else {
                            destination.addPendingDestinationProfilePhoto(photoId);
                        }
                    } else {
                        return CompletableFuture.supplyAsync(() -> forbidden(Json.toJson(
                            "You do not have permission to set this photo as the destination primary photo")));
                    }
                    return destinationRepository.updateDestination(destination)
                        .thenApplyAsync(rows -> ok(Json.toJson(oldDestination)));
                });
        });
    }

    /**
     * Rejects a pending destination primary photo
     *
     * @param destId Id of destination
     * @param photoId Id of photo
     * @return Response result containing success/error message
     */
    @With({Admin.class, Authenticator.class}) //admin auth
    public CompletableFuture<Result> rejectDestinaitonPrimaryPhoto(Http.Request request,
        Long destId, Long photoId) {
        return destinationRepository.getDestination(destId).thenApplyAsync(destination -> {
            if (destination == null || !destination.removePendingDestinationPrimaryPhoto(photoId)) {
                return notFound(Json.toJson("No pending phot destination combo"));
            } else {
                return ok(Json.toJson(photoId));
            }
        });
    }

    /**
     * Accepts a pending destination primary photo and sets it
     *
     * @param destId Id of destination
     * @param photoId Id of photo
     * @return Response result containing success/error message
     */
    @With({Admin.class, Authenticator.class}) //admin auth
    public CompletableFuture<Result> acceptDestinaitonPrimaryPhoto(Http.Request request,
        Long destId, Long photoId) {
        return destinationRepository.getDestination(destId).thenComposeAsync(destination -> {
            if (destination == null || !destination.removePendingDestinationPrimaryPhoto(photoId)) {
                return CompletableFuture.supplyAsync(() -> notFound(Json.toJson("No pending phot destination combo")));
            } else {
                return photoRepository.getPhotoById(photoId).thenComposeAsync(photo -> {
                    if (photo == null) {
                        return CompletableFuture.supplyAsync(() -> notFound(Json.toJson("Photo not found")));
                    }
                    JsonNode oldDestination = Json.toJson(destination);
                    destination.primaryPhoto = photo;
                    return destinationRepository.updateDestination(destination)
                        .thenApplyAsync(rows -> ok(oldDestination));
                });
            }
        });
    }

    /**
     * Gets all destinations that are strictly public, regardless of which user is requesting them
     *
     * @return 200 code with public destination array on success, otherwise 500 if sanitization
     * error
     */
    public CompletableFuture<Result> getAllPublicDestinations() {
        return destinationRepository.getAllPublicDestinations()
            .thenApplyAsync(publicDestinations -> {
                try {
                    return ok(sanitizeJson(Json.toJson(publicDestinations)));
                } catch (IOException e) {
                    return internalServerError(Json.toJson(SANITIZATION_ERROR));
                }
            });
    }

    /**
     * Gets all destinations valid for the requesting user.
     *
     * @param request Http request containing authentication information
     * @param userId ID of user to retrieve destinations for
     * @return OK status code with a list of destinations in the body
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> getAllDestinations(Http.Request request, Long userId) {
        User user = request.attrs().get(ActionState.USER);

        // If user is admin or requesting for their own destinations
        if ((user.admin && user.id.equals(userId))) {
            return destinationRepository.getAllDestinationsAdmin()
                .thenApplyAsync(allDestinations -> {
                    try {
                        return ok(sanitizeJson(Json.toJson(allDestinations)));
                    } catch (IOException e) {
                        return internalServerError(Json.toJson(SANITIZATION_ERROR));
                    }
                });
        } else if (user.id.equals(userId)) {
            return destinationRepository.getAllDestinations(userId)
                .thenApplyAsync(allDestinations -> {
                    try {
                        return ok(sanitizeJson(Json.toJson(allDestinations)));
                    } catch (IOException e) {
                        return internalServerError(Json.toJson(SANITIZATION_ERROR));
                    }
                });
        }
        // Else return only public destinations
        else {
            return destinationRepository.getAllPublicDestinations()
                .thenApplyAsync(allDestinations -> {
                    try {
                        return ok(sanitizeJson(Json.toJson(allDestinations)));
                    } catch (IOException e) {
                        return internalServerError(Json.toJson(SANITIZATION_ERROR));
                    }
                });
        }
    }

    /**
     * Gets a destination with a given id. Returns a json with destination object.
     *
     * @param getId ID of wanted destination
     * @return OK with a destination, notFound if destination does not exist
     */
    public CompletableFuture<Result> getDestination(long getId) {
        return destinationRepository.getDestination(getId).thenApplyAsync(destination -> {
            if (destination == null) {
                return notFound(Json.toJson(getId));
            } else {
                try {
                    return ok(sanitizeJson(Json.toJson(destination)));
                } catch (IOException e) {
                    return internalServerError(Json.toJson(SANITIZATION_ERROR));
                }
            }
        });
    }

    /**
     * Gets a paged list of destinations conforming to the amount of destinations requested and the
     * provided order and filters.
     *
     * @param page The current page to display
     * @param pageSize The number of destinations per page
     * @param order The column to order by
     * @param filter The sort order (either asc or desc)
     * @return OK with paged list of destinations
     */
    public CompletableFuture<Result> getPagedDestinations(int page, int pageSize, String order,
        String filter) {
        return destinationRepository.getPagedDestinations(page, pageSize, order, filter)
            .thenApplyAsync(destinations -> ok());
    }

    /**
     * Gets the google api key
     *
     * @return an ok message with the google api key
     */
    @With({Everyone.class, Authenticator.class})
    public CompletionStage<Result> googleMapsHelper() {
        String apiKey = "AIzaSyC9Z1g5p2rQtS0nHgDudzCCXVl2HJl3NPI";
        WSRequest request = ws.url("https://maps.googleapis.com/maps/api/js");
        request.addQueryParameter("key", apiKey);

        return request.execute()
            .thenApplyAsync(response -> ok(response.getBody()).as("text/javascript"));
    }

    /**
     * Lists routes to put in JS router for use from frontend.
     *
     * @return JSRouter Play result
     */
    public Result destinationRoutes(Http.Request request) {
        return ok(
            JavaScriptReverseRouter.create("destinationRouter", "jQuery.ajax", request.host(),
                controllers.backend.routes.javascript.DestinationController.getAllDestinations(),
                controllers.backend.routes.javascript.DestinationController
                    .getAllPublicDestinations(),
                controllers.backend.routes.javascript.DestinationController.getDestination(),
                controllers.backend.routes.javascript.DestinationController.deleteDestination(),
                controllers.frontend.routes.javascript.DestinationController
                    .detailedDestinationIndex(),
                controllers.backend.routes.javascript.DestinationController.editDestination(),
                controllers.backend.routes.javascript.DestinationController.makeDestinationPublic(),
                controllers.backend.routes.javascript.DestinationController
                    .toggleDestinationTravellerType(),
                controllers.backend.routes.javascript.DestinationController
                    .toggleRejectTravellerType(),
                controllers.backend.routes.javascript.DestinationController.addNewDestination(),
                controllers.backend.routes.javascript.DestinationController.changeDestinationPrimaryPhoto()
            )
        ).as(Http.MimeTypes.JAVASCRIPT);
    }
}
