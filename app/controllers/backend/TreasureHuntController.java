package controllers.backend;

import actions.ActionState;
import actions.Authenticator;
import actions.roles.Everyone;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import models.TreasureHunt;
import models.User;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Http.MimeTypes;
import play.mvc.Result;
import play.mvc.With;
import play.routing.JavaScriptReverseRouter;
import repository.TreasureHuntRepository;
import util.validation.ErrorResponse;
import util.validation.TreasureHuntValidator;

/**
 * Manages the database for Treasure Hunt calls
 */
public class TreasureHuntController extends TEABackController {

    private final TreasureHuntRepository treasureHuntRepository;

    @Inject
    public TreasureHuntController(TreasureHuntRepository treasureHuntRepository) {
        this.treasureHuntRepository = treasureHuntRepository;
    }

    /**
     * Ensures treasure hunt data is valid and calls method to insert into database
     *
     * @param request HTTP request containing authentication and request body
     * @return On success, ok with ID of new treasure hunt, on error returns unauthorized, forbidden
     * or bad request
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> insertTreasureHunt(Http.Request request) {
        // Get the data input by the user as a JSON object
        JsonNode data = request.body().asJson();
        User user = request.attrs().get(ActionState.USER);

        // Sends the received data to the validator for checking
        ErrorResponse validatorResult = new TreasureHuntValidator(data).validateTreasureHunt();

        // Checks if the validator found any errors in the data
        if (validatorResult.error()) {
            return CompletableFuture.supplyAsync(() -> badRequest(validatorResult.toJson()));
        }

        // Assemble TreasureHunt
        TreasureHunt treasureHunt = Json.fromJson(data, TreasureHunt.class);

        // Checks if user logged in is not allowed to create treasure hunt for userId
        if (!user.admin && !user.id.equals(treasureHunt.user.id)) {
            return CompletableFuture.supplyAsync(() -> forbidden(Json.toJson(
                "You do not have permission to create a treasure hunt for someone else")));
        }

        return treasureHuntRepository.addTreasureHunt(treasureHunt).thenApplyAsync(treasureHuntId ->
            ok(Json.toJson(treasureHuntId))
        );
    }

    public  Result treasureHuntRoutes(Http.Request request) {
        return ok(
            JavaScriptReverseRouter.create("treasureHuntRouter", "jQuery.ajax", request.host(),
                controllers.backend.routes.javascript.TreasureHuntController.insertTreasureHunt()
            )
        ).as(MimeTypes.JAVASCRIPT);
    }
}
