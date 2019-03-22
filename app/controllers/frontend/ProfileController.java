package controllers.frontend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.Profile;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSBodyReadables;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static play.libs.Scala.asScala;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's profile page.
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
     * Displays the profile page. Called with the /profile URL and uses a GET request.
     * Checks that a user is logged in. Takes them to the profile page if they are,
     * otherwise they are taken to the start page.
     *
     * @return displays the profile or start page.
     */
    public Result index(Http.Request request) {
        return request.session()
                .getOptional("connected")
                .map(user -> ok(profile.render(user)))
                .orElseGet(() -> redirect(controllers.frontend.routes.UserController.index()));
    }

    public CompletableFuture<Result> editindex(Http.Request request) {
        return request.session().getOptional("connected").map(user -> {
            return this.getProfile().thenApplyAsync(
                    profile -> {
                        return ok(editProfile.render(profile, user));
                    },
                    httpExecutionContext.current());
        }).orElseGet(() -> CompletableFuture.supplyAsync(() -> redirect(controllers.frontend.routes.UserController.index())));
    }

    public Result createProfile(Http.Request request) {
        return ok();
    }

    /**
     * Gets Profile from api endpoint via get request
     *
     * @return List of destinations wrapped in completable future
     */
    private CompletableFuture<Profile> getProfile() {
        // This url will need to be updated to use the actual logged in user
        CompletableFuture<WSResponse> res = ws.url("http://localhost:9000/api/profile/1").get().toCompletableFuture();
        return res.thenApply(r -> {
            //System.out.println(r.getBody());
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

}



