package controllers;

import models.Destination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.mvc.*;
import views.html.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static play.libs.Scala.asScala;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's destinations page.
 */
@Singleton
public class DestinationsController extends Controller {

    public static Form<DestData> form;
    public static MessagesApi messagesApi;
    public static List<Destination> destList;

    private final Logger logger = LoggerFactory.getLogger(getClass()) ;

    @Inject
    public void DestController(FormFactory formFactory, MessagesApi messagesApi) {
        this.form = formFactory.form(DestData.class);
        this.messagesApi = messagesApi;
        this.destList = com.google.common.collect.Lists.newArrayList(
                new Destination("UC", "Place", "Canterbury", 43.5235, 172.5839, "New Zealand"),
                new Destination("Grove Street", "Place", "Los Santos", 98.2335, 45.5120, "United States of America")
        );
    }

    /**
     * Displays the destinations page. Called with the /destinations URL and uses a GET request.
     * Checks that a user is logged in. Takes them to the destinations page if they are,
     * otherwise they are taken to the start page.
     *
     * @return displays the destinations or start page.
     */
    public Result index(Http.Request request) {
        return request.session()
                .getOptional("connected")
                .map(user -> ok(destinations.render(asScala(destList), form, user, request, messagesApi.preferred(request))))
                .orElseGet(() -> redirect(routes.StartController.index()));
    }

    public Result createDestination(Http.Request request) {
        final Form<DestData> boundForm = form.bindFromRequest(request);

        if (boundForm.hasErrors()) {
            logger.error("errors = {}", boundForm.errors());
            return index(request);
        } else {
            DestData data = boundForm.get();
            Destination newDest = new Destination(data.getName(), data.getType(), data.getDistrict(), data.getLatitude(), data.getLongitude(), data.getCountry());
            destList.add(newDest);
            return index(request).flashing("info", "Destination added!");
        }
    }

    public Result deleteDestination(Http.Request request) {
        destList.remove(destList.size() - 1);
        return  index(request);

    }

}

