package controllers.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.inject.Singleton;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import play.mvc.Controller;

/**
 * This controller is the super class to all backend controllers
 */
@Singleton
class TEABackController extends Controller {

    /**
     * Sanitizes json input of all html and js
     *
     * @param body JsonNode input
     * @return JsonNode that has been sanitized
     */
    JsonNode sanitizeJson(JsonNode body) throws IOException {
         String bodyString = body.toString();
        //Create a sanitizer
        PolicyFactory policy = new HtmlPolicyBuilder().toFactory();
        bodyString = policy.sanitize(bodyString).replace("&#34;", "\"").replace("&#64;", "@");
        //map new body back to a json object
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(bodyString);
    }
}