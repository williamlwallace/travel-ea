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

public class DestinationController extends Controller {
    private final FormFactory formFactory;
    private final HttpExecutionContext httpExecutionContext;
    private final MessagesApi messagesApi;

    @Inject
    public DestinationController(
            FormFactory formFactory,
            HttpExecutionContext httpExecutionContext,
            MessagesApi messagesApi) {
        this.formFactory = formFactory;
        this.httpExecutionContext = httpExecutionContext;
        this.messagesApi = messagesApi;
    }

    public Result addDestinationForm() {
        return ok(views.html.addDestinationForm.render());
    }

    public Result viewAllDestinations() {return ok(views.html.viewAllDestinations.render()); }
}
