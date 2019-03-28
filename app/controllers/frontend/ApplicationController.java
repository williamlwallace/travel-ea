package controllers.frontend;

import actions.*;
import actions.roles.Everyone;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import views.html.*;
import models.User;


/**
 * This controller contains actions to handle HTTP requests on the start page.
 */
public class ApplicationController extends Controller {

    /**
     * Displays the home page. Called with the /home URL and uses a GET request.
     * Checks that a user is logged in. Takes them to the home page if they are,
     * otherwise they are taken to the start page.
     *
     * @return displays the home or start page.
     */
    @With({Everyone.class, Authenticator.class})
    public Result home(Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        return ok(home.render(user));
                
    }

    /**
     * Displays the start page. Called with the / URL and uses a GET request.
     * Takes the user to the home page if they are already logged in.
     *
     * @param request
     * @return displays the start page.
     */
    @With(Authenticator.class)
    public Result cover(Http.Request request) {
        return ok(start.render());
    }
}
