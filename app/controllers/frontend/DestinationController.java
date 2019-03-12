package controllers.frontend;

import models.Destination;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import play.mvc.*;
import views.html.*;
import play.libs.ws.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import play.libs.concurrent.HttpExecutionContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static play.libs.Scala.asScala;

/**
 * This controller contains an action to handle HTTP requests to the
 * application's destinations page.
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
     * Displays the destinations page. Called with the /destinations URL and uses a
     * GET request. Checks that a user is logged in. Takes them to the destinations
     * page if they are, otherwise they are taken to the start page.
     *
     * @return displays the destinations or start page.
     */
    public CompletableFuture<Result> index(Http.Request request) {
        //will remove session checks when middle ware done in story 9
        return request.session().getOptional("connected").map(user -> {
            return this.getDestinations().thenApplyAsync(
                    destList -> {
                        return (destList.size() != 0) ? ok(destinations.render(asScala(destList), user)):internalServerError();
                    },
                    httpExecutionContext.current());
        }).orElseGet(() -> CompletableFuture.supplyAsync(() -> redirect(controllers.routes.StartController.index())));
    }

    public Result createDestination(Http.Request request) {
        return ok();
    }

    public Result deleteDestination(Http.Request request) {
        return ok();
    }

    /**
     * Gets Destinations from api endpoint via get request
     * 
     * @return List of destinations wrapped in completable future
     */
    private CompletableFuture<List<Destination>> getDestinations() {
        CompletableFuture<WSResponse> res = ws.url("http://localhost:9000/api/destination").get().toCompletableFuture();
        return res.thenApply(r -> {
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
