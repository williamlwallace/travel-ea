package controllers.frontend;

import actions.*;
import actions.roles.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Destination;
import models.TripData;
import models.Trip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSBodyReadables;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.*;
import views.html.*;

import java.util.ArrayList;
import java.util.Arrays;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static play.libs.Scala.asScala;

/**
 */
public class TripController extends Controller {

    private MessagesApi messagesApi;
    private HttpExecutionContext httpExecutionContext;
    private WSClient ws;
    private DestinationController destinationController;

    @Inject
    public void TripController(
            MessagesApi messagesApi,
            HttpExecutionContext httpExecutionContext,
            WSClient ws,
            DestinationController destinationController) {

        this.messagesApi = messagesApi;
        this.httpExecutionContext = httpExecutionContext;
        this.ws = ws;
        this.destinationController = destinationController;
    }

    /**
     * Displays the trips page. Called with the /trips URL and uses a GET request.
     * Checks that a user is logged in. Takes them to the trips page if they are,
     * otherwise they are taken to the start page.
     *
     * @return displays the trips or start page.
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> tripIndex(Http.Request request) {
        String username = request.attrs().get(ActionState.USER).username;
        return this.getUserTrips(Authenticator.getTokenFromCookie(request)).thenApplyAsync(
                tripList -> {
                    return ok(trips.render(username, asScala(tripList), request, messagesApi.preferred(request)));
                },
                httpExecutionContext.current());
    }

    /**
     * Displays the create trip page. Called with the /trips/create URL and uses a GET request.
     * Checks that a user is logged in. Takes them to the create trip page if they are,
     * otherwise they are taken to the start page.
     *
     * @return displays the create trip or start page.
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> createTripIndex(Http.Request request) {
        String username = request.attrs().get(ActionState.USER).username;
        return destinationController.getDestinations().thenApplyAsync(
                destList -> {
                    return (destList.size() != 0) ? ok(createTrip.render(username, asScala(destList), new Trip(), request, messagesApi.preferred(request))) : internalServerError();
                },
                httpExecutionContext.current());
    }

    /**
     * Displays the create trip page re-formatted for editing the selected trip. Called with the /trips/edit URL and uses a GET request.
     * Checks that a user is logged in. Takes them to the edit trip page if they are,
     * otherwise they are taken to the start page.
     *
     * @return displays the create trip or start page.
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> editTripIndex(Http.Request request, Long tripId) {
        String username = request.attrs().get(ActionState.USER).username;
        return destinationController.getDestinations().thenApplyAsync(
                destList -> {
                    return (destList.size() != 0) ? ok(createTrip.render(username, asScala(destList), new Trip(), request, messagesApi.preferred(request))) : internalServerError();
                },
                httpExecutionContext.current());
    }

    /**
     * Gets trips from api endpoint via get request
     *
     * @return List of trips wrapped in completable future
     */
    public CompletableFuture<List<Trip>> getUserTrips(String token) {
        CompletableFuture<WSResponse> res = ws
                                            .url("http://localhost:9000/api/trip/getAll/")
                                            .addHeader("Cookie", String.format("JWT-Auth=%s;", token))
                                            .get()
                                            .toCompletableFuture();
        return res.thenApply(r -> {
            JsonNode json = r.getBody(WSBodyReadables.instance.json());
            try {
                return new ObjectMapper().readValue(new ObjectMapper().treeAsTokens(json),
                        new TypeReference<List<Trip>>() {});
            } catch (Exception e) {
                return new ArrayList<>();
            }
        });
    }
}
