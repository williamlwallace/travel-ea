package controllers.frontend;

import javax.inject.Inject;
import javax.inject.Singleton;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;

/**
 * This controller is a super class to all front end controllers.
 */
@Singleton
public class TEAFrontController extends Controller {

    final HttpExecutionContext httpExecutionContext;
    static final String HTTP = "http://";

    @Inject
    protected TEAFrontController(HttpExecutionContext httpExecutionContext) {
        this.httpExecutionContext = httpExecutionContext;
    }
}