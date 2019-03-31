package util.validation;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.Map;
import play.libs.Json;

/**
 * Class to track errors and be sent back to client
 */
public class ErrorResponse {

    private Map<String, String> inputErrors = new HashMap<String, String>();

    /**
     * return whether there has been any errors
     *
     * @return Boolean
     */
    public Boolean error() {
        return (this.inputErrors.size() == 0) ? false : true;
    }

    /**
     * adds an error message to the errors map
     *
     * @param error Error message
     * @param field Field name
     */
    public Void map(String error, String field) {
        this.inputErrors.put(field, error);
        return null;
    }

    /**
     * jsonifies this object
     *
     * @return json object
     */
    public JsonNode toJson() {
        return Json.toJson(this.inputErrors);
    }
}