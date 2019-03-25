package controllers.backend;


import com.fasterxml.jackson.databind.JsonNode;
import models.Destination;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import repository.CountryDefinitionRepository;
import repository.DestinationRepository;
import util.validation.DestinationValidator;
import util.validation.ErrorResponse;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

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
     * @param request Request containing destination json object as body
     * @return Ok with id of destination on success, badRequest otherwise
     */
    public CompletableFuture<Result> addNewDestination(Http.Request request) {
        JsonNode data = request.body().asJson();
        //Sends the received data to the validator for checking
        ErrorResponse validatorResult = new DestinationValidator(data).addNewDestination();
        if (validatorResult.error()) {
            return CompletableFuture.supplyAsync(() -> badRequest(validatorResult.toJson()));
        } else {
            //Else, no errors found, continue with adding to the database
            Destination newDestination = Json.fromJson(data, Destination.class);
            return destinationRepository.addDestination(newDestination).thenApplyAsync(id -> ok(Json.toJson(id)));
        }
    }

    /**
     * Deletes a destination with given id. Return a result with a json int which represents the number
     * of rows that were deleted. So if the return value is 0, no destination was found to delete
     * @param id ID of destination to delete
     * @return OK with number of rows deleted, badrequest if none deleted
     */
    public CompletableFuture<Result> deleteDestination(Long id) {
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
     * Gets all destinations. Returns a json list of all destinations.
     * @return OK with list of destinations
     */
    public CompletableFuture<Result> getAllDestinations() {
        return destinationRepository.getAllDestinations()
            .thenApplyAsync(allDestinations -> ok(Json.toJson(allDestinations)));
    }

    /**
     * Gets all countries. Returns a json list of all countries.
     * @return OK with list of countries
     */
    public CompletableFuture<Result> getAllCountries() {
        return countryDefinitionRepository.getAllCountries()
                .thenApplyAsync(allCountries -> ok(Json.toJson(allCountries)));
    }

    /**
     * Gets a destination with a given id. Returns a json with destination object.
     * @param getId ID of wanted destination
     * @return OK with a destination, notFound if destination does not exist
     */
    public CompletableFuture<Result> getDestination(long getId) {
        return destinationRepository.getDestination(getId).thenApplyAsync(destination -> {
            if (destination.id == null) {
                return notFound(Json.toJson(getId));
            } else {
                return ok(Json.toJson(destination));
            }
        });
    }

    /**
     * Gets a paged list of destinations conforming to the amount of destinations requested and the provided order and
     * filters.
     * @param page      The current page to display
     * @param pageSize  The number of destinations per page
     * @param order     The column to order by
     * @param filter    The sort order (either asc or desc)
     * @return OK with paged list of destinations
     */
    public CompletableFuture<Result> getPagedDestinations(int page, int pageSize, String order, String filter) {
        return destinationRepository.getPagedDestinations(page, pageSize, order, filter)
            .thenApplyAsync(destinations -> ok());
    }
}
