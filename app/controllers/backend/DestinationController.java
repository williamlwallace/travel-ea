package controllers.backend;

import actions.ActionState;
import actions.Authenticator;
import actions.roles.Everyone;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;

import models.Destination;
import models.User;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import play.routing.JavaScriptReverseRouter;
import repository.CountryDefinitionRepository;
import repository.DestinationRepository;
import util.validation.DestinationValidator;
import util.validation.ErrorResponse;

/**
 * Manages destinations in the database.
 */
public class DestinationController extends Controller {

    private final DestinationRepository destinationRepository;
    private final CountryDefinitionRepository countryDefinitionRepository;

    @Inject
    public DestinationController(DestinationRepository destinationRepository,
        CountryDefinitionRepository countryDefinitionRepository) {
        this.destinationRepository = destinationRepository;
        this.countryDefinitionRepository = countryDefinitionRepository;
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
        //Sends the received data to the validator for checking
        ErrorResponse validatorResult = new DestinationValidator(data).addNewDestination();
        if (validatorResult.error()) {
            return CompletableFuture.supplyAsync(() -> badRequest(validatorResult.toJson()));
        } else {
            //Else, no errors found, continue with adding to the database
            Destination newDestination = Json.fromJson(data, Destination.class);
            // Add destination owner to be whichever user uploaded it
            newDestination.user = new User();
            newDestination.user.id = request.attrs().get(ActionState.USER).id;
            return destinationRepository.addDestination(newDestination)
                .thenApplyAsync(id -> ok(Json.toJson(id)));
        }
    }

    /**
     * Allows a user to mark one of their destinations as public, this will cause it to become immediately visible to all other users,
     * as well as merging with any sufficiently similar destinations that are currently marked as private in the database
     *
     * @param request Request containing authentication header
     * @param id ID of destination to mark as public
     * @return 200 if successful, 400 if already public, 401 unauthorized, 403 forbidden, 404 no such destination
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> makeDestinationPublic(Http.Request request, Long id) {
        User user = request.attrs().get(ActionState.USER);
        // Try to get the destination, if it is not found throw 404
        return destinationRepository.getDestination(id).thenComposeAsync(destination -> {
            // Check for 404
            if(destination == null) {
                return CompletableFuture.supplyAsync(() -> notFound(Json.toJson("No such destination exists")));
            }
            // Check if user owns the destination (or is an admin)
            if(!destination.user.id.equals(user.id) && !user.admin) {
                return CompletableFuture.supplyAsync(() -> forbidden(Json.toJson("You are not allowed to perform this action")));
            }
            // Otherwise perform the repository call which will return either 200, 400, or 404 as appropriate
            return destinationRepository.makeDestinationPublic(user, destination);
        }).thenApplyAsync(result -> result);
    }

    /**
     * Deletes a destination with given id. Return a result with a json int which represents the
     * number of rows that were deleted. So if the return value is 0, no destination was found to
     * delete
     *
     * @param id ID of destination to delete
     * @return OK with number of rows deleted, badrequest if none deleted
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> deleteDestination(Long id) {
        // TODO: add authentication of user to destination
        return destinationRepository.deleteDestination(id).thenApplyAsync(rowsDeleted -> {
            if (rowsDeleted < 1) {
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.map("Destination not found", "other");
                return badRequest(errorResponse.toJson());
            } else {
                return ok(Json.toJson(rowsDeleted));
            }
        });
    }

    /**
     * Gets all the destinations valid for the requesting user
     *
     * @param request Http request containing authentication information
     * @param userId ID of user to retrieve destinations for
     * @return OK status code with a list of destinations in the body
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> getAllDestinations(Http.Request request, Long userId) {
        User user = request.attrs().get(ActionState.USER);

        if (user.admin || user.id.equals(userId)) {
            return destinationRepository.getAllDestinations(userId)
                    .thenApplyAsync(allDestinations -> ok(Json.toJson(allDestinations)));
        }
        else {
            return destinationRepository.getAllPublicDestinations()
                    .thenApplyAsync(allDestinations -> ok(Json.toJson(allDestinations)));
        }

    }

    /**
     * Gets all countries. Returns a json list of all countries.
     *
     * @return OK with list of countries
     */
    public CompletableFuture<Result> getAllCountries() {
        return countryDefinitionRepository.getAllCountries()
            .thenApplyAsync(allCountries -> ok(Json.toJson(allCountries)));
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
                return ok(Json.toJson(destination));
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
        // TODO: Destinations should be returned here which are not currently, update API spec when modified
        return destinationRepository.getPagedDestinations(page, pageSize, order, filter)
            .thenApplyAsync(destinations -> ok());
    }

    /**
     * Lists routes to put in JS router for use from frontend
     *
     * @return JSRouter Play result
     */
    public Result destinationRoutes(Http.Request request) {
        return ok(
            JavaScriptReverseRouter.create("destinationRouter", "jQuery.ajax", request.host(),
                controllers.backend.routes.javascript.DestinationController.getAllCountries(),
                controllers.backend.routes.javascript.DestinationController.getAllDestinations()
            )
        ).as(Http.MimeTypes.JAVASCRIPT);
    }
}
