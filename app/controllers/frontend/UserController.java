package controllers.frontend;

import models.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import play.mvc.*;

import views.html.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;

import static play.libs.Scala.asScala;

/**
 * This controller contains actions to handle HTTP requests on the start page.
 */
@Singleton
public class UserController extends Controller {
    private WSClient ws;
    private HttpExecutionContext httpExecutionContext;


    /**
     * Used to initialise the account form. messagesApi and also creates a list of example
     * account data to use before a database is established.
     */
    @Inject
    public void StartController(WSClient ws, HttpExecutionContext httpExecutionContext) {
        this.ws = ws;
        this.httpExecutionContext = httpExecutionContext;
    }

    /**
     * Displays the start page. Called with the / URL and uses a GET request.
     * Takes the user to the home page if they are already logged in.
     *
     * @param request
     * @return displays the start page.
     */
    public Result index(Http.Request request) {
        return request.session()
                .getOptional("connected")
                .map(user -> redirect(controllers.routes.ApplicationController.index()))
            .orElseGet(() -> {
                return ok(start.render());
            }
                );
    }

    /**
     * Uses a POST request at /login to validate logging in to an account.
     * Will display error messages if email/password are incorrect.
     * If the account details are in the database. The user will be logged in and taken to
     * the home page.
     *
     * @param request
     * @return a http result; a redirect if the user credentials are correct, and a bad request in other cases.
     */
    public Result login(Http.Request request) {
        return redirect(controllers.routes.ApplicationController.index()).addingToSession(request, "connected", "dave@gmail.com");
    }

    public Result signUp(Http.Request request) {
        return redirect(controllers.frontend.routes.ProfileController.index()).addingToSession(request, "connected", "dave@gmail.com");
    }
}
