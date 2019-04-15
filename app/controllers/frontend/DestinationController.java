package controllers.frontend;

import static play.libs.Scala.asScala;

import actions.ActionState;
import actions.Authenticator;
import actions.roles.Everyone;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;
import models.Destination;
import models.User;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSBodyReadables;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import views.html.destinations;

/**
 * This controller contains an action to handle HTTP requests to the application's destinations
 * page.
 */
@Singleton
public class DestinationController extends Controller {

    private WSClient ws;
    private HttpExecutionContext httpExecutionContext;

    @Inject
    public void DestController(WSClient ws, HttpExecutionContext httpExecutionContext) {
        this.ws = ws;
        this.httpExecutionContext = httpExecutionContext;
    }

    /**
     * Displays the destinations page. Called with the /destinations URL and uses a GET request.
     * Checks that a user is logged in. Takes them to the destinations page if they are, otherwise
     * they are taken to the start page.
     *
     * @return displays the destinations or start page.
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> index(Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        return this.getDestinations().thenApplyAsync(
            destList -> {
                return (destList.size() != 0) ? ok(destinations.render(asScala(destList), user))
                    : internalServerError();
            },
            httpExecutionContext.current());
    }

    /**
     * Gets Destinations from api endpoint via get request
     *
     * @return List of destinations wrapped in completable future
     */
    public CompletableFuture<List<Destination>> getDestinations() {
        CompletableFuture<WSResponse> res = ws.url("http://localhost:9000/api/destination").get()
            .toCompletableFuture();
        return res.thenApply(r -> {
            //System.out.println(r.getBody());
            JsonNode json = r.getBody(WSBodyReadables.instance.json());
            try {
                return new ObjectMapper().readValue(new ObjectMapper().treeAsTokens(json),
                    new TypeReference<List<Destination>>() {
                    });
            } catch (Exception e) {
                return new ArrayList<Destination>();
            }
        });

    }
}
