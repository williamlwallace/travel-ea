package controllers.frontend;

import actions.ActionState;
import actions.Authenticator;
import actions.roles.Admin;
import javax.inject.Inject;
import javax.inject.Singleton;
import models.User;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import views.html.admin;


/**
 * This controller contains an action to handle HTTP requests to the application's admin page.
 */
@Singleton
public class AdminController extends TEAFrontController {

    @Inject
    public AdminController(HttpExecutionContext httpExecutionContext) {
        super(httpExecutionContext);
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

}
