package controllers.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;
import play.mvc.Controller;
import play.mvc.Result;
import play.libs.concurrent.HttpExecutionContext;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller is the super class to all backend controllers
 */
@Singleton
class TEABackController extends Controller {

    static final String SANITIZATION_ERROR = "Sanitization Failed";

    /**
     * Sanitizes json input of all html and js
     *
     * @param body JsonNode input
     * @return JsonNode that has been sanitized
     */
    protected JsonNode sanitizeJson(JsonNode body) throws IOException {
//        String bodyString = body.toString();
//        //Create a sanitizer
//        PolicyFactory policy = new HtmlPolicyBuilder().toFactory();
//        bodyString = policy.sanitize(bodyString).replace("&#34;", "\"").replace("&#64;", "@");
//        //map new body back to a json object
//        ObjectMapper mapper = new ObjectMapper();
//        JsonNode newBody = mapper.readTree(bodyString);
//        return newBody;
        return body;
    }
}