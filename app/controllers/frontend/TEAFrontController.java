package controllers.frontend;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import play.mvc.Controller;
import play.libs.concurrent.HttpExecutionContext;

/**
 * This controller is a super class to all front end controllers
 */
@Singleton
public class TEAFrontController extends Controller {
    protected HttpExecutionContext httpExecutionContext;

    @Inject
    protected TEAFrontController(HttpExecutionContext httpExecutionContext) {
        this.httpExecutionContext = httpExecutionContext;
    }
}