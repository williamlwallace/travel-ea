package controllers.frontend;

import actions.ActionState;
import actions.Authenticator;
import actions.roles.Everyone;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.backend.routes;
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
        return this.getProfile(userId, request).thenComposeAsync(
                profile -> {
                    return this.getUser(userId, request).thenComposeAsync(
                            user -> {
                                return this.getUserTrips(request, userId).thenApplyAsync(
                                        tripList -> {
                                            boolean canModify = loggedUser.id.equals(userId) || loggedUser.admin;
                                            return ok(views.html.profile.render(profile, user, loggedUser, asScala(tripList), canModify));
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
    private CompletableFuture<List<Trip>> getUserTrips(Http.Request request, Long userId) {
        String url = "http://" + request.host() + controllers.backend.routes.TripController.getAllUserTrips(userId);
        CompletableFuture<WSResponse> res = ws
                .url(url)
                .addHeader("Cookie", String.format("JWT-Auth=%s;", Authenticator.getTokenFromCookie(request)))
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

    /**
     * Gets profile of user to be viewed via get request
     * @param userId Id of profile to be retrieved
     * @param request Request containing url and authentication information
     * @return CompletableFuture containing profile object
     */
    private CompletableFuture<Profile> getProfile(Long userId, Http.Request request) {
        String url = "http://" + request.host() + controllers.backend.routes.ProfileController.getProfile(userId);
        CompletableFuture<WSResponse> res = ws.url(url).get().toCompletableFuture();
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

    /**
     * Gets user object to be viewed in profile screen
     * @param userId Id of user to be retrieved
     * @param request Request containing url and authentication information
     * @return CompletableFuture containing user object
     */
    private CompletableFuture<User> getUser(Long userId, Http.Request request) {
        String url = "http://" + request.host() + controllers.backend.routes.UserController.getUser(userId);
        CompletableFuture<WSResponse> res = ws
                .url(url)
                .addHeader("Cookie", String.format("JWT-Auth=%s;", Authenticator.getTokenFromCookie(request)))
                .get()
                .toCompletableFuture();
        return res.thenApply(r -> {
            try {
                JsonNode json = r.getBody(WSBodyReadables.instance.json());
                return new ObjectMapper().readValue(new ObjectMapper().treeAsTokens(json),
                        new TypeReference<User>() {
                        });
            } catch (Exception e) {
                return new User();
            }
        });
    }

    // TODO: Whats this for
    public Result createProfile() {
        return ok();
    }
}



