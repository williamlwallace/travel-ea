package controllers.frontend;

import actions.ActionState;
import actions.Authenticator;
import actions.roles.Everyone;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Profile;
import models.Trip;
import models.User;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSBodyReadables;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import views.html.createProfile;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static play.libs.Scala.asScala;


/**
 * This controller contains an action to handle HTTP requests to the application's profile page.
 */
@Singleton
public class ProfileController extends Controller {

    private WSClient ws;
    private HttpExecutionContext httpExecutionContext;

    @Inject
    public ProfileController(WSClient ws, HttpExecutionContext httpExecutionContext) {
        this.ws = ws;
        this.httpExecutionContext = httpExecutionContext;
    }

    /**
     * Displays the create profile page. Called with the /profile/create URL and uses a GET request. Checks that a
     * user is logged in. Takes them to the profile page if they are, otherwise they are taken to
     * the start page.
     *
     * @return displays the create profile or start page.
     */
    @With({Everyone.class, Authenticator.class})
    public Result createProfileIndex(Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        return ok(createProfile.render(user, user.id));
    }

    /**
     * Displays the  edit profile page. Called with the /Profile URL and uses a GET request.
     * Checks that a user is logged in. Takes them to the profile page if they are, otherwise
     * they are taken to the start page.
     *
     * @return displays the profile or start page.
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> index(Http.Request request, Long userId) {
        User loggedUser = request.attrs().get(ActionState.USER);
        return this.getProfile(userId).thenComposeAsync(
                profile -> {
                    return this.getUser(userId).thenComposeAsync(
                            username -> {
                                User user = new User();
                                user.id = userId;
                                user.username = username;
                                return this.getUserTrips(Authenticator.getTokenFromCookie(request), request).thenApplyAsync(
                                        tripList -> {
                                            if (loggedUser.id.equals(userId) || loggedUser.admin) {
                                                return ok(views.html.profile.render(profile, user, loggedUser, asScala(tripList), true));
                                            }
                                            else {
                                                return ok(views.html.profile.render(profile, user, loggedUser, asScala(tripList), false));
                                            }
                                        },
                                        httpExecutionContext.current()
                                );
                            },
                            httpExecutionContext.current());
                },
                httpExecutionContext.current());
    }

    /**
     * Gets trips from api endpoint via get request.
     *
     * @return List of trips wrapped in completable future
     */
    public CompletableFuture<List<Trip>> getUserTrips(String token, Http.Request request) {
        String url = "http://" + request.host() + controllers.backend.routes.TripController.getAllUserTrips();
        CompletableFuture<WSResponse> res = ws
                .url(url)
                .addHeader("Cookie", String.format("JWT-Auth=%s;", token))
                .get()
                .toCompletableFuture();
        return res.thenApply(r -> {
            JsonNode json = r.getBody(WSBodyReadables.instance.json());
            try {
                return new ObjectMapper().readValue(new ObjectMapper().treeAsTokens(json),
                        new TypeReference<List<Trip>>() {
                        });
            } catch (Exception e) {
                return new ArrayList<>();
            }
        });
    }

    // TODO: Change javadoc
    /**
     * Gets Destinations from api endpoint via get request.
     *
     * @return List of destinations wrapped in completable future
     */
    private CompletableFuture<Profile> getProfile(Long userId) {
        CompletableFuture<WSResponse> res = ws.url("http://localhost:9000/api/profile/" + userId)
            .get().toCompletableFuture();
        return res.thenApply(r -> {
            JsonNode json = r.getBody(WSBodyReadables.instance.json());
            try {
                return new ObjectMapper().readValue(new ObjectMapper().treeAsTokens(json),
                    new TypeReference<Profile>() {
                    });
            } catch (Exception e) {
                return new Profile();
            }
        });
    }

    // TODO: Javadoc and set up check admin or matching user
    private CompletableFuture<String> getUser(Long userId) {
        CompletableFuture<WSResponse> res = ws.url("http://localhost:9000/api/user/name/" + userId)
                .get().toCompletableFuture();
        return res.thenApply(r -> {
            JsonNode json = r.getBody(WSBodyReadables.instance.json());
            //try {
            //    System.out.println();
                return json.toString();
            //} catch (Exception e) {
            //    return "";
            //}
        });
    }

    // TODO: Whats this for
    public Result createProfile() {
        return ok();
    }
}



