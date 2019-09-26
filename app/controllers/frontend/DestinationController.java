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
import views.html.destinations;

/**
 * This controller contains an action to handle HTTP requests to the application's destinations
 * page.
 */
@Singleton
public class DestinationController extends TEAFrontController {

    @Inject
    public DestinationController(HttpExecutionContext httpExecutionContext) {
        super(httpExecutionContext);
    }

    /**
     * Displays the destinations page. Called with the /destinations URL and uses a GET request.
     * Checks that a user is logged in. Takes them to the destinations page if they are, otherwise
     * they are taken to the start page.
     *
     * @param request Http request containing authentication information
     * @return displays the destinations or start page.
     */
    @With({Everyone.class, Authenticator.class})
    public Result index(Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        return ok(destinations.render(user));
    }

    /**
     * Displays a selected destinations details. Checks if the logged user is the destination owner
     * or an admin and sets permissions accordingly.
     *
     * @param request the http request
     * @param destinationId the id of the destination to view the details of
     * @return displays the detailed destination page for the selected destination.
     */
    @With({Everyone.class, Authenticator.class})
    public Result detailedDestinationIndex(Http.Request request,
        Long destinationId) {
        User loggedUser = request.attrs().get(ActionState.USER);
        return ok(
            views.html.detailedDestination.render(destinationId, loggedUser));
    }
}