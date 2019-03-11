package controllers.frontend;

import models.frontend.Destination;
import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.*;
import views.html.*;
import play.mvc.*;
import play.libs.ws.*;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static play.libs.Scala.asScala;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's destinations page.
 */
@Singleton
public class DestinationController extends Controller {
    private FormFactory formFactory;
    private WSClient ws;

    @Inject
    public void DestController(FormFactory formFactory, WSClient ws) {
        this.ws = ws;
        this.formFactory = formFactory;
    }

    /**
     * Displays the destinations page. Called with the /destinations URL and uses a GET request.
     * Checks that a user is logged in. Takes them to the destinations page if they are,
     * otherwise they are taken to the start page.
     *
     * @return displays the destinations or start page.
     */
    public CompletableFuture<Result> index(Http.Request request) {
        return request.session()
                .getOptional("connected")
                .map(user -> {
                    return this.getDestinations().thenApply(json -> ok(json));
                    // ok(destinations.render(asScala(destList), form, user));
                })
                .orElseGet(() -> CompletableFuture.supplyAsync(() -> redirect(controllers.routes.StartController.index())));
    }

    public Result createDestination(Http.Request request) {
        return ok();
    }

    public Result deleteDestination(Http.Request request) {
        return ok();
    }

    private CompletableFuture<JsonNode> getDestinations() {
        CompletableFuture<WSResponse> res = ws.url("http://localhost:9000/api/destination").get().toCompletableFuture();
        return res.thenApply(r -> {
            JsonNode body = r.getBody(WSBodyReadables.instance.json());
            return body;
        });
        
    }

}

