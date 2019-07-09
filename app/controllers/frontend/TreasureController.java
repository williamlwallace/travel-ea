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
import views.html.treasureHunt;


/**
 * This controller contains an action to handle HTTP requests to the application's treasure hunt page.
 */
@Singleton
public class TreasureController extends TEAFrontController {

    @Inject
    public TreasureController(HttpExecutionContext httpExecutionContext) {
        super(httpExecutionContext);
    }

    /**
     * Displays the trips page. Called with the /trips URL and uses a GET request. Checks that a
     * user is logged in. Takes them to the trips page if they are, otherwise they are taken to the
     * start page.
     *
     * @return displays the trips or start page.
     */
    @With({Everyone.class, Authenticator.class})
    public Result index(Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        return ok(treasureHunt.render(user));
    }
}
