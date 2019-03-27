package controllers.frontend;

import actions.*;
import actions.roles.*;
import models.frontend.Destination;
import models.frontend.Trip;
import play.i18n.MessagesApi;
import play.mvc.*;
import views.html.*;

import java.util.ArrayList;
import javax.inject.Inject;
import java.util.List;

/**
 * The front end controller for the Trips page
 */
public class TripsController extends Controller {

    public static MessagesApi messagesApi;
    public static List<Trip> tripList;
    private final ArrayList<Destination> destList = new ArrayList<Destination>();

    @Inject
    public void TripController( MessagesApi messagesApi) {

        this.messagesApi = messagesApi;

    }
    /**
     * Displays the trips page. Called with the /trips URL and uses a GET request.
     * Checks that a user is logged in. Takes them to the trips page if they are,
     * otherwise they are taken to the start page.
     *
     * @return displays the trips or start page.
     */
    @With({Everyone.class, Authenticator.class})
    public Result index(Http.Request request) {
        String username = request.attrs().get(ActionState.USER).username;
        return ok(trips.render(username, tripList, request, messagesApi.preferred(request)));
    }
}
