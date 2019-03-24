package controllers;

import actions.*;
import actions.roles.Everyone;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import views.html.*;
import models.User;
import javax.inject.Inject;


/**
 * This controller contains actions to handle HTTP requests on the start page.
 */
public class ApplicationController extends Controller {


    @Inject
    public ApplicationController(FormFactory formFactory, MessagesApi messagesApi) {

    }

    /**
     * Displays the home page. Called with the /home URL and uses a GET request.
     * Checks that a user is logged in. Takes them to the home page if they are,
     * otherwise they are taken to the start page.
     *
     * @return displays the home or start page.
     */
    @With({Everyone.class, Authenticator.class})
    public Result index(Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        return ok(home.render(user.username));
                
    }

    /**
     * Removes the current user from the session and redirects them to the start page.
     *
     * @return redirects user to the start page with a new session.
     */
    public Result logout() {
        return redirect(controllers.frontend.routes.UserController.index()).withNewSession();
    }

}
