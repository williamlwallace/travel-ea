package controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.libs.Scala;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.*;

import views.html.*;
import models.frontend.Trip;
import models.frontend.Destination;
import controllers.frontend.DestinationController;

import java.util.ArrayList;
import java.util.Arrays;
import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static play.libs.Scala.asScala;

/**
 */
public class createTripController extends Controller {

    private MessagesApi messagesApi;
    private HttpExecutionContext httpExecutionContext;
    private DestinationController destinationController;
    private final ArrayList<Trip> tripList;
    private final ArrayList<Destination> destList = new ArrayList<Destination>();

    @Inject
    public createTripController(
            MessagesApi messagesApi,
            HttpExecutionContext httpExecutionContext,
            DestinationController destinationController) {
        this.messagesApi = messagesApi;
        this.httpExecutionContext = httpExecutionContext;
        //Test data for accounts. should be replaced with account data from database.
        Destination newDest = new Destination("UC", "Place", "Canterbury", 43.5235, 172.5839, "New Zealand");
        this.destList.add(newDest);
        Trip newTrip = new Trip(this.destList);
        this.destinationController = destinationController;
        this.tripList = com.google.common.collect.Lists.newArrayList(newTrip);
    }


    /**
     * Displays the create trip page. Called with the /trips/create URL and uses a GET request.
     * Checks that a user is logged in. Takes them to the create trip page if they are,
     * otherwise they are taken to the start page.
     *
     * @return displays the create trip or start page.
     */
//    public Result index2(Http.Request request) {
//        return request.session()
//                .getOptional("connected")
//                .map(user -> ok(createTrip.render(user, destinationController.getDestinations(), tripList, request, messagesApi.preferred(request))))
//                .orElseGet(() -> redirect(routes.StartController.index()));
//    }

    public CompletableFuture<Result> index(Http.Request request) {
        return request.session().getOptional("connected").map(user -> {
            return destinationController.getDestinations().thenApplyAsync(
                    destList -> {
                        return (destList.size() != 0) ? ok(createTrip.render(user, asScala(destList), tripList, request, messagesApi.preferred(request))):internalServerError();
                    },
                    httpExecutionContext.current());
        }).orElseGet(() -> CompletableFuture.supplyAsync(() -> redirect(controllers.routes.StartController.index())));
    }

    /**
     * Displays the destinations page. Called with the /destinations URL and uses a
     * GET request. Checks that a user is logged in. Takes them to the destinations
     * page if they are, otherwise they are taken to the start page.
     *
     * @return displays the destinations or start page.
     */
//    public CompletableFuture<Result> index(Http.Request request) {
//        //will remove session checks when middle ware done in story 9
//        return request.session().getOptional("connected").map(user -> {
//            return destinationController.getDestinations().thenApplyAsync(
//                    destList -> {
//                        return (destList.size() != 0) ? ok(destinations.render(asScala(destList), user)):internalServerError();
//                    },
//                    httpExecutionContext.current());
//        }).orElseGet(() -> CompletableFuture.supplyAsync(() -> redirect(controllers.routes.StartController.index())));
//    }


}
