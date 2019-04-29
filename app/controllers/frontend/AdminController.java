package controllers.frontend;

import actions.ActionState;
import actions.Authenticator;
import actions.roles.Admin;
import javax.inject.Inject;
import javax.inject.Singleton;
import models.User;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
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

    @Inject
    public AdminController(WSClient ws, HttpExecutionContext httpExecutionContext) {
        this.ws = ws;
    }

    /**
     * Displays the admin page. Called with the /admin URL and uses a GET request. Checks that a
     * user is logged in and an admin. Takes them to the admin page if they are, otherwise they are
     * taken to the home/start page.
     *
     * @return displays the admin, home or start page.
     */
    @With({Admin.class, Authenticator.class})
    public Result index(Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        return ok(admin.render(user));
    }

    // /**
    //  * Gets Users from api endpoint via get request
    //  *
    //  * @return List of users wrapped in completable future
    //  */
    // public CompletableFuture<List<User>> getUsers(String token) {
    //     CompletableFuture<WSResponse> res = ws
    //         .url("http://localhost:9000/api/user/search")
    //         .addHeader("Cookie", String.format("JWT-Auth=%s;", token))
    //         .get()
    //         .toCompletableFuture();
    //     return res.thenApply(r -> {
    //         //System.out.println(r.getBody());
    //         JsonNode json = r.getBody(WSBodyReadables.instance.json());
    //         try {
    //             return new ObjectMapper().readValue(new ObjectMapper().treeAsTokens(json),
    //                 new TypeReference<List<User>>() {
    //                 });
    //         } catch (Exception e) {
    //             e.printStackTrace();
    //             return new ArrayList<User>();
    //         }
    //     });

    // }

}
