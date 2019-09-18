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
import models.Profile;
import models.User;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSBodyReadables;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import views.html.createProfile;


/**
 * This controller contains an action to handle HTTP requests to the application's profile page.
 */
@Singleton
public class ProfileController extends TEAFrontController {

    private final WSClient ws;

    @Inject
    public ProfileController(WSClient ws, HttpExecutionContext httpExecutionContext) {
        super(httpExecutionContext);
        this.ws = ws;
    }

    /**
     * Displays the create profile page. Called with the /profile/create URL and uses a GET request.
     * Checks that a user is logged in. Takes them to the profile page if they are, otherwise they
     * are taken to the start page.
     *
     * @return displays the create profile or start page.
     */
    @With({Everyone.class, Authenticator.class})
    public Result createProfileIndex(Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        return ok(createProfile.render(user));
    }

    /**
     * Displays the  edit profile page. Called with the /Profile URL and uses a GET request. Checks
     * that a user is logged in. Takes them to the profile page if they are, otherwise they are
     * taken to the start page.
     *
     * @return displays the profile or start page.
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> index(Http.Request request, Long userId) {
        User loggedUser = request.attrs().get(ActionState.USER);
        return this.getProfile(userId, request).thenComposeAsync(
            profile ->
                this.getUser(userId, request).thenApplyAsync(
                    user -> {
                        user.id = userId;
                        boolean canModify = loggedUser.id.equals(userId) || loggedUser.admin;
                        return ok(views.html.profile.render(profile, user, loggedUser, canModify));
                    },
                    httpExecutionContext.current())
            ,
            httpExecutionContext.current());
    }

    /**
     * Gets profile of user to be viewed via get request.
     *
     * @param userId Id of profile to be retrieved
     * @param request Request containing url and authentication information
     * @return CompletableFuture containing profile object
     */
    private CompletableFuture<Profile> getProfile(Long userId, Http.Request request) {
        String url = HTTP + request.host() + controllers.backend.routes.ProfileController
            .getProfile(userId);
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
     * Gets user object to be viewed in profile screen.
     *
     * @param userId Id of user to be retrieved
     * @param request Request containing url and authentication information
     * @return CompletableFuture containing user object
     */
    private CompletableFuture<User> getUser(Long userId, Http.Request request) {
        String url =
            HTTP + request.host() + controllers.backend.routes.UserController.getUser(userId);
        CompletableFuture<WSResponse> res = ws
            .url(url)
            .addHeader("Cookie",
                String.format("JWT-Auth=%s;", Authenticator.getTokenFromCookie(request)))
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

    public Result createProfile() {
        return ok();
    }
}



