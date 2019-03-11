package controllers.frontend;

import models.Destination;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;

import static play.mvc.Results.ok;

public class TripController extends Controller {
    private final FormFactory formFactory;
    private final HttpExecutionContext httpExecutionContext;
    private final MessagesApi messagesApi;

    @Inject
    public TripController(
            FormFactory formFactory,
            HttpExecutionContext httpExecutionContext,
            MessagesApi messagesApi) {
        this.formFactory = formFactory;
        this.httpExecutionContext = httpExecutionContext;
        this.messagesApi = messagesApi;
    }

    public Result addNewTrip() {
        return ok(views.html.addTrip.render());
    }

    public Result viewTrip(Long id) { return ok(views.html.viewTrip.render()); }

    public Result viewAllUserTrips(Long uid) { return ok(views.html.viewAllUserTrips.render()); }
}
