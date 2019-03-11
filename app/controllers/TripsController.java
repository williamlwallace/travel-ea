package controllers;

import models.frontend.Destination;
import models.frontend.Trip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.mvc.*;
import views.html.*;

import java.util.ArrayList;
import java.util.Arrays;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static play.libs.Scala.asScala;

/**
 */
public class TripsController extends Controller {

    public static MessagesApi messagesApi;
    public static List<Trip> tripList;
    private final ArrayList<Destination> destList = new ArrayList<Destination>();

    @Inject
    public void TripController( MessagesApi messagesApi) {
        this.messagesApi = messagesApi;
        //Test data for accounts. should be replaced with account data from database.
        Destination newDest = new Destination("UC", "Place", "Canterbury", 43.5235, 172.5839, "New Zealand");
        this.destList.add(newDest);
        Trip newTrip = new Trip(this.destList);
        this.tripList = com.google.common.collect.Lists.newArrayList(newTrip);
    }
    /**
     * Displays the trips page. Called with the /trips URL and uses a GET request.
     * Checks that a user is logged in. Takes them to the trips page if they are,
     * otherwise they are taken to the start page.
     *
     * @return displays the trips or start page.
     */
    public Result index(Http.Request request) {
        return request.session()
                .getOptional("connected")
                .map(user -> ok(trips.render(user, tripList, request, messagesApi.preferred(request))))
                .orElseGet(() -> redirect(routes.StartController.index()));
    }

}
