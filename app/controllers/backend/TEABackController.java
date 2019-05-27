package controllers.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.inject.Singleton;
import play.mvc.Controller;


/**
 * This controller is the super class to all backend controllers.
 */
@Singleton
class TEABackController extends Controller {

    static final String SANITIZATION_ERROR = "Sanitization Failed";

    static final long MASTER_ADMIN_ID = 1L;

    /**
     * Sanitizes json input of all html and js.
     *
     * @param body JsonNode input
     * @return JsonNode that has been sanitized
     */
    JsonNode sanitizeJson(JsonNode body) throws IOException {
        String bodyString = body.toString();
        bodyString = bodyString.replace(">", "&gt;").replace("<", "&lt;");
        //map new body back to a json object
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(bodyString);
    }
}