package controllers.frontend;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import play.mvc.Controller;
import play.libs.concurrent.HttpExecutionContext;

/**
 * This controller contains actions to handle HTTP requests on the start page.
 */
public class TEAFrontController extends Controller {
    protected HttpExecutionContext httpExecutionContext;

    @Inject
    public TEAFrontController(HttpExecutionContext httpExecutionContext) {
        this.httpExecutionContext = httpExecutionContext;
    }

    public CompletableFuture<Result> sanitizeJson(JsonNode body) throws IOException {
        String bodyString = body.toString();
        //Create a sanitizer
        PolicyFactory policy = new HtmlPolicyBuilder().toFactory();
        body = policy.sanitize(bodyString).replace("&#34;", "\"").replace("&#64;", "@");
        //map new body back to a json object
        ObjectMapper mapper = new ObjectMapper();
        JsonNode newBody = mapper.readTree(body);
        return newBody;
        // } catch (IOException e) {
        //     return CompletableFuture.supplyAsync(() -> internalServerError());
        // }
    }


}