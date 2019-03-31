package controllers.frontend;

import actions.ActionState;
import actions.Authenticator;
import actions.roles.Admin;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;
import models.User;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSBodyReadables;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import views.html.admin;


/**
 * This controller contains an action to handle HTTP requests to the application's admin page.
 */
@Singleton
public class AdminController extends Controller {

    private WSClient ws;
    private HttpExecutionContext httpExecutionContext;

    @Inject
    public void DestController(WSClient ws, HttpExecutionContext httpExecutionContext) {
        this.ws = ws;
        this.httpExecutionContext = httpExecutionContext;
    }

    /**
     * Displays the admin page. Called with the /admin URL and uses a GET request. Checks that a
     * user is logged in and an admin. Takes them to the admin page if they are, otherwise they are
     * taken to the home/start page.
     *
     * @return displays the admin, home or start page.
     */
    @With({Admin.class, Authenticator.class})
    public CompletableFuture<Result> index(Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        return getUsers(Authenticator.getTokenFromCookie(request)).thenApplyAsync((users ->
            ok(admin.render(user, users))
        ), httpExecutionContext.current());
    }

    /**
     * Gets Users from api endpoint via get request
     *
     * @return List of users wrapped in completable future
     */
    public CompletableFuture<List<User>> getUsers(String token) {
        CompletableFuture<WSResponse> res = ws
            .url("http://localhost:9000/api/user/search")
            .addHeader("Cookie", String.format("JWT-Auth=%s;", token))
            .get()
            .toCompletableFuture();
        return res.thenApply(r -> {
            //System.out.println(r.getBody());
            JsonNode json = r.getBody(WSBodyReadables.instance.json());
            try {
                return new ObjectMapper().readValue(new ObjectMapper().treeAsTokens(json),
                    new TypeReference<List<User>>() {
                    });
            } catch (Exception e) {
                return new ArrayList<User>();
            }
        });

    }

}
