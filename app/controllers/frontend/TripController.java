package controllers.frontend;

import static play.libs.Scala.asScala;

import actions.ActionState;
import actions.Authenticator;
import actions.roles.Everyone;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;
import models.Trip;
import models.User;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSBodyReadables;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import views.html.createTrip;
import views.html.trips;

public class TripController extends TEAFrontController {

    private WSClient ws;

    @Inject
    public TripController(
        HttpExecutionContext httpExecutionContext,
        WSClient ws) {
        super(httpExecutionContext);
        this.ws = ws;
    }

    /**    private HttpExecutionContext httpExecutionContext;
     * Displays the trips page. Called with the /trips URL and uses a GET request. Checks that a
     * user is logged in. Takes them to the trips page if they are, otherwise they are taken to the
     * start page.
     *
     * @return displays the trips or start page.
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> tripIndex(Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        return this.getUserTrips(request).thenApplyAsync(
                tripList -> ok(trips.render(user, asScala(tripList)))
                , httpExecutionContext.current());
    }

    /**
     * Displays the create trip page. Called with the /trips/create URL and uses a GET request.
     * Checks that a user is logged in. Takes them to the create trip page if they are, otherwise
     * they are taken to the start page.
     *
     * @return displays the create trip or start page.
     */
    @With({Everyone.class, Authenticator.class})
    public Result createTripIndex(Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        return ok(createTrip.render(user, new Trip()));
    }

    /**
     * Displays the create trip page re-formatted for editing the selected trip. Called with the
     * /trips/edit URL and uses a GET request. Checks that a user is logged in. Takes them to the
     * edit trip page if they are, otherwise they are taken to the start page.
     *
     * @return displays the create trip or start page.
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> editTripIndex(Http.Request request, Long tripId) {
        User user = request.attrs().get(ActionState.USER);
        return this.getTrip(Authenticator.getTokenFromCookie(request), tripId, request)
        .thenApplyAsync(trip -> ok(createTrip.render(user, trip))
                                , httpExecutionContext.current());
    }

    /**
     * Gets trips from api endpoint via get request.
     *
     * @return List of trips wrapped in completable future
     */
    public CompletableFuture<List<Trip>> getUserTrips(Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        String url = "http://" + request.host() + controllers.backend.routes.TripController.getAllUserTrips(user.id);
        CompletableFuture<WSResponse> res = ws
            .url(url)
            .addHeader("Cookie", String.format("JWT-Auth=%s;", Authenticator.getTokenFromCookie(request)))
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

    /**
     * Gets trip by tripId from api endpoint via get request.
     *
     * @return Trip object wrapped in completable future
     */
    public CompletableFuture<Trip> getTrip(String token, Long tripId, Http.Request request) {
        String url =
            "http://" + request.host() + controllers.backend.routes.TripController.getTrip(tripId);
        CompletableFuture<WSResponse> res = ws
            .url(url)
            .addHeader("Cookie", String.format("JWT-Auth=%s;", token))
            .get()
            .toCompletableFuture();
        return res.thenApply(r -> {
            JsonNode json = r.getBody(WSBodyReadables.instance.json());
            try {
                return new ObjectMapper().readValue(new ObjectMapper().treeAsTokens(json),
                    new TypeReference<Trip>() {
                    });
            } catch (Exception e) {
                return new Trip();
            }
        });
    }
}
