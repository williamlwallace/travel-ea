package controllers.backend;

import actions.Authenticator;
import actions.roles.Everyone;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import models.Destination;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import repository.CountryDefinitionRepository;
import repository.DestinationRepository;
import util.validation.DestinationValidator;
import util.validation.ErrorResponse;

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
            return destinationRepository.addDestination(newDestination)
                .thenApplyAsync(id -> ok(Json.toJson(id)));
        }
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
        return destinationRepository.deleteDestination(id).thenApplyAsync(rowsDeleted ->
            (rowsDeleted == 0) ? badRequest(Json.toJson("No such destination"))
                : ok(Json.toJson(rowsDeleted)));
    }

    public CompletableFuture<Result> getAllDestinations() {
        return destinationRepository.getAllDestinations()
            .thenApplyAsync(allDestinations -> ok(Json.toJson(allDestinations)));
    }

    public CompletableFuture<Result> getAllCountries() {
        return countryDefinitionRepository.getAllCountries()
            .thenApplyAsync(allDestinations -> ok(Json.toJson(allDestinations)));
    }

    public CompletableFuture<Result> getDestination(long getId) {
        return destinationRepository.getDestination(getId).thenApplyAsync(destination -> {
            if (destination.id == null) {
                return notFound(Json.toJson(getId));
            } else {
                return ok(Json.toJson(destination));
            }
        });
    }

    public CompletableFuture<Result> getPagedDestinations(int page, int pageSize, String order,
        String filter) {
        return destinationRepository.getPagedDestinations(page, pageSize, order, filter)
            .thenApplyAsync(destinations -> ok());
    }
}
