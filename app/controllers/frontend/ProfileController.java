package controllers.frontend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.ProfileData;
import controllers.routes;
import models.Destination;
import models.frontend.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSBodyReadables;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.profile;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
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
    public CompletableFuture<Result> index(Http.Request request) {
        return request.session()
                .getOptional("connected")
                .map(user -> {
                    return this.getUser().thenApplyAsync(
                            ok(profile.render(user)),
                            httpExecutionContext.current());
                }).orElseGet(() -> CompletableFuture.supplyAsync(() -> redirect(controllers.frontend.routes.UserController.index())));
    }

    /**
     * Uses a POST request at /profile to validate creating a profile.
     * If the account details are in the database. The user will be logged in and taken to the home page
     *
     * @param request
     * @return a http result; a redirect if the user credentials are correct, and a bad request in other cases.
     */
    public Result createProfile(Http.Request request) {
        return redirect(controllers.routes.ApplicationController.index()).addingToSession(request, "connected", "dave@gmail.com");
    }

    private CompletableFuture<String> getUser() {
        CompletableFuture<WSResponse> res = ws.url("http://localhost:9000/api/profile").get().toCompletableFuture();
        return res.thenApply(r -> {
            JsonNode json = r.getBody(WSBodyReadables.instance.json());
            try {
                return new ObjectMapper().readValue(new ObjectMapper().treeAsTokens(json),
                        new TypeReference<String>() {
                        });
            } catch (Exception e) {
                return null;
            }
        });
    }
}
