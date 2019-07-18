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
public class TreasureHuntController extends TEAFrontController {

    @Inject
    public TreasureHuntController(HttpExecutionContext httpExecutionContext) {
        super(httpExecutionContext);
    }

    /**
     * Displays the treasure hunt page. Called with the /treasureHunts URL and uses a GET request.
     *
     * @return displays the treasure hunt page
     */
    @With({Everyone.class, Authenticator.class})
    public Result index(Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        return ok(treasureHunt.render(user));
    }
}
