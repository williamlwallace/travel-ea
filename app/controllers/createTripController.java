package controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.mvc.*;

import views.html.*;
import models.Trip;
import models.Destination;

import java.util.ArrayList;
import java.util.Arrays;
import javax.inject.Inject;
import java.util.List;

import static play.libs.Scala.asScala;

/**
 */
public class createTripController extends Controller {

    private MessagesApi messagesApi;
    private final List<Trip> tripList;
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
    public Result index(Http.Request request) {
        return request.session()
                .getOptional("connected")
                .map(user -> ok(createTrip.render(user, tripList, request, messagesApi.preferred(request))))
                .orElseGet(() -> redirect(routes.StartController.index()));
    }



}
