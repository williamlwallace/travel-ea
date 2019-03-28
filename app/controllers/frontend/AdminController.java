package controllers.frontend;

import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.admin;

import javax.inject.Singleton;


/**
 * This controller contains an action to handle HTTP requests to the
 * application's admin page.
 */
@Singleton
public class AdminController extends Controller {

    /**
     * Displays the admin page. Called with the /admin URL and uses a
     * GET request. Checks that a user is logged in and an admin. Takes them to the admin
     * page if they are, otherwise they are taken to the home/start page.
     *
     * @return displays the admin, home or start page.
     */
    public Result index(Http.Request request) {
        //will remove session checks when middle ware done in story 9
        return request.session().getOptional("connected")
                .map(user -> ok(admin.render(user)))
                .orElseGet(() -> redirect(routes.UserController.index()));
    }


}
