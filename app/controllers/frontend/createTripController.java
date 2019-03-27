package controllers.frontend;

import actions.*;
import actions.roles.*;
import play.mvc.With;
import play.i18n.MessagesApi;
import play.mvc.*;

import views.html.*;
import models.frontend.Trip;
import models.frontend.Destination;

import java.util.ArrayList;
import javax.inject.Inject;

/**
 * The front end controller for the Create Trip page
 */
public class createTripController extends Controller {

    private MessagesApi messagesApi;
    private final ArrayList<Trip> tripList;
    private final ArrayList<Destination> destList = new ArrayList<Destination>();

    @Inject
    public createTripController(MessagesApi messagesApi) {
        this.messagesApi = messagesApi;
        //Test data for accounts. should be replaced with account data from database.
        Destination newDest = new Destination("UC", "Place", "Canterbury", 43.5235, 172.5839, "New Zealand");
        this.destList.add(newDest);
        Trip newTrip = new Trip(this.destList);
        this.tripList = com.google.common.collect.Lists.newArrayList(newTrip);
    }


    /**
     * Displays the create trip page. Called with the /trips/create URL and uses a GET request.
     * Checks that a user is logged in. Takes them to the create trip page if they are,
     * otherwise they are taken to the start page.
     *
     * @return displays the create trip or start page.
     */
    @With({Everyone.class, Authenticator.class})
    public Result index(Http.Request request) {
        String username = request.attrs().get(ActionState.USER).username;
       return ok(createTrip.render(username, tripList, request, messagesApi.preferred(request)));
    }



}
