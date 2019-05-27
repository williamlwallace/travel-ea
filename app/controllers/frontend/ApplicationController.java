package controllers.frontend;

import actions.ActionState;
import actions.Authenticator;
import actions.roles.Everyone;
import javax.inject.Inject;
import javax.inject.Singleton;
import models.User;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import views.html.home;
import views.html.start;


/**
 * This controller contains actions to handle HTTP requests on the start page.
 */
@Singleton
public class ApplicationController extends TEAFrontController {

    @Inject
    public ApplicationController(HttpExecutionContext httpExecutionContext) {
        super(httpExecutionContext);
    }

    /**
     * Displays the home page. Called with the /home URL and uses a GET request. Checks that a user
     * is logged in. Takes them to the home page if they are, otherwise they are taken to the start
     * page.
     *
     * @return displays the home or start page.
     */
    @With({Everyone.class, Authenticator.class})
    public Result home(Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        return ok(home.render(user));

    }

    /**
     * Displays the start page. Called with the / URL and uses a GET request. Takes the user to the
     * home page if they are already logged in.
     *
     * @return displays the start page.
     */
    @With(Authenticator.class)
    public Result cover(Http.Request request) {
        return ok(start.render());
    }
}
