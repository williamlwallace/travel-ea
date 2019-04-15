package util.validation;

import com.fasterxml.jackson.databind.JsonNode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

/**
 * Provides validation for json form data
 */
public class Validator {

    private final JsonNode form; //json form data
    private ErrorResponse errorResponse;

    public Validator(JsonNode form, ErrorResponse errorResponse) {
        this.form = form;
        this.errorResponse = errorResponse;
    }

    /**
     * Checks field is not empty
     *
     * @param field json field name
     * @return Boolean whether validation succeeds
     */
    protected Boolean required(String field) {
        if (this.form.has(field)) {
            if ((this.form.get(field) != null && !this.form.get(field).asText("").equals("")) ||
                this.form.get(field).isObject()) {
                return true;
            } else if (this.form.get(field).isArray() && this.form.get(field).size() > 0) {
                return true;
            }
        }
        this.errorResponse.map(String.format("%s field must be present", field), field);
        return false;
    }

    /**
     * Checks field is longer then given length
     *
     * @param field json field name
     * @param min Min field length
     * @return Boolean whether validation succeeds
     */
    protected Boolean minTextLength(String field, int min) {
        if (this.form.get(field).asText("").length() < min) {
            this.errorResponse
                .map(String.format("%s has a minTextLength length of %d", field, min), field);
            return false;
        }
        return true;
    }

    /**
     * Checks field is shorter then given length
     *
     * @param field json field name
     * @param max maxTextLength field length
     * @return Boolean whether validation succeeds
     */
    protected Boolean maxTextLength(String field, int max) {
        if (this.form.get(field).asText("").length() > max) {
            this.errorResponse
                .map(String.format("%s has a maxTextLength length of %d", field, max), field);
            return false;
        }
        return true;
    }

    /**
     * Checks if value of field is integer
     *
     * @param field json field name
     * @return Boolean whether condition is met
     */
    protected Boolean isText(String field) {
        if (!this.form.get(field).isTextual()) {
            this.errorResponse.map(String.format("%s must be text", field), field);
            return false;
        }
        return true;
    }

    /**
     * Checks if value of field is integer
     *
     * @param field json field name
     * @return Boolean whether condition is met
     */
    protected Boolean isInt(String field) {
        if (!this.form.get(field).isInt()) {
            this.errorResponse.map(String.format("%s must be of type integer", field), field);
            return false;
        }
        return true;
    }

    /**
     * Checks if value of field is long
     *
     * @param field json field name
     * @return Boolean whether condition is met
     */
    protected Boolean isLong(String field) {
        if (!this.form.get(field).isLong()) {
            this.errorResponse.map(String.format("%s must be of type long", field), field);
            return false;
        }
        return true;
    }

    /**
     * Checks if value of field is double or int
     *
     * @param field json field name
     * @return Boolean whether condition is met
     */
    protected Boolean isDoubleOrInt(String field) {
        if (!this.form.get(field).isDouble() && !this.form.get(field).isInt()) {
            this.errorResponse.map(String.format("%s must be of type double", field), field);
            return false;
        }
        return true;
    }

    /**
     * Checks integer value of field, must already be confirmed to be integer
     *
     * @param field json field name
     * @param max maxTextLength value of integer in field
     * @return Boolean whether condition is met
     */
    protected Boolean maxDoubleValue(String field, double max) {
        if (this.form.get(field).asDouble() > max) {
            this.errorResponse
                .map(String.format("%s must be not be more than %f", field, max), field);
            return false;
        }
        return true;
    }

    /**
     * Checks integer value of field, must already be confirmed to be integer
     *
     * @param field json field name
     * @param min minTextLength value of integer in field
     * @return Boolean whether condition is met
     */
    protected Boolean minDoubleValue(String field, double min) {
        if (this.form.get(field).asDouble() < min) {
            this.errorResponse.map(String.format("%s must be at least %f", field, min), field);
            return false;
        }
        return true;
    }

    /**
     * Checks integer value of field, must already be confirmed to be integer
     *
     * @param field json field name
     * @param max maxTextLength value of integer in field
     * @return Boolean whether condition is met
     */
    protected Boolean maxIntValue(String field, int max) {
        if (this.form.get(field).asInt() > max) {
            this.errorResponse
                .map(String.format("%s must be not be more than %d", field, max), field);
            return false;
        }
        return true;
    }

    /**
     * Checks integer value of field, must already be confirmed to be integer
     *
     * @param field json field name
     * @param min minTextLength value of integer in field
     * @return Boolean whether condition is met
     */
    protected Boolean minIntValue(String field, int min) {
        if (this.form.get(field).asInt() < min) {
            this.errorResponse.map(String.format("%s must be at least %d", field, min), field);
            return false;
        }
        return true;
    }

    /**
     * Checks field if valid email
     *
     * @param field json field name
     * @return Boolean whether validation succeeds
     */
    protected Boolean email(String field) {
        String email = this.form.get(field).asText("");
        Pattern pattern = Pattern.compile("^.+@.+\\..+$");
        if (!pattern.matcher(email).matches()) {
            this.errorResponse.map("Invalid email", field);
            return false;
        }
        return true;
    }

    /**
     * Checks field if valid date
     *
     * @param field json field name
     * @return Boolean whether validation succeeds
     */
    protected Boolean date(String field) {
        String date = this.form.get(field).asText("");
        try {
            new SimpleDateFormat("yyyy-MM-dd").parse(date);
        } catch (ParseException e) {
            this.errorResponse.map("Invalid date", field);
            return false;
        }
        return true;
    }

    /**
     * Checks field if valid gender
     *
     * @param field json field name
     * @return Boolean whether validation succeeds
     */
    protected Boolean gender(String field) {
        String gender;
        try {
            gender = this.form.get(field).asText("");
        } catch (NullPointerException e) {
            this.errorResponse.map("Invalid gender", field);
            return false;
        }
        if (gender.equals("Select")) {
            return false;
        }
        if (gender.equals("Male") || gender.equals("Female") || gender.equals("Other")) {
            return true;
        }
        this.errorResponse.map("Invalid gender", field);
        return false;
    }

    /**
     * errorResponse getter
     *
     * @return ErrorResponse
     */
    protected ErrorResponse getErrorResponse() {
        return this.errorResponse;
    }
}