package controllers.frontend;

import actions.ActionState;
import actions.Authenticator;
import actions.roles.Everyone;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;
import models.Destination;
import models.User;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSBodyReadables;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import views.html.destinations;
import play.libs.Json;

/**
 * This controller contains an action to handle HTTP requests to the application's destinations
 * page.
 */
@Singleton
public class DestinationController extends TEAFrontController {

    private final WSClient ws;

    @Inject
    public DestinationController(WSClient ws, HttpExecutionContext httpExecutionContext) {
        super(httpExecutionContext);
        this.ws = ws;
    }

    /**
     * Displays the destinations page. Called with the /destinations URL and uses a GET request.
     * Checks that a user is logged in. Takes them to the destinations page if they are, otherwise
     * they are taken to the start page.
     *
     * @param request Http request containing authentication information
     * @return displays the destinations or start page.
     */
    @With({Everyone.class, Authenticator.class})
    public Result index(Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        return ok(destinations.render(user));
    }

    /**
     * Gets a Destination of selected id from api endpoint via get request.
     *
     * @param request the http request.
     * @param destinationId the id of the destination to retrieve
     * @return List of destinations wrapped in completable future
     */
    private CompletableFuture<Destination> getDestination(Http.Request request,
        Long destinationId) {
        String url = "http://" + request.host() + controllers.backend.routes.DestinationController
            .getDestination(destinationId);
        CompletableFuture<WSResponse> res = ws.url(url).get().toCompletableFuture();
        return res.thenApply(r -> {
            JsonNode json = r.getBody(WSBodyReadables.instance.json());
            try {
                return new ObjectMapper().readValue(new ObjectMapper().treeAsTokens(json),
                    new TypeReference<Destination>() {
                    });
            } catch (Exception e) {
                return new Destination();
            }
        });
    }

    /**
     * Displays a selected destinations details. Checks if the logged user is the destination owner
     * or an admin and sets permissions accordingly.
     *
     * @param request the http request
     * @param destinationId the id of the destination to view the details of
     * @return displays the detailed destination page for the selected destination.
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> detailedDestinationIndex(Http.Request request,
        Long destinationId) {
        User loggedUser = request.attrs().get(ActionState.USER);
        return this.getDestination(request, destinationId).thenApplyAsync(destination -> {
            if (destination.user == null) {
                return notFound();
            } else {
                boolean canModify = loggedUser.id.equals(destination.user.id) || loggedUser.admin;
                return ok(
                    views.html.detailedDestination.render(destinationId, loggedUser, canModify));
            }
        }, httpExecutionContext.current());
    }

}
