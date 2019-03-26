package util.validation;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Validator subclass to provide validation for the user controller
 */
public class UserValidator extends Validator {

    public UserValidator(JsonNode form) {
        super(form, new ErrorResponse());
    }

    /**
     * Validates login data
     * @return ErrorResponse object
     */
    public ErrorResponse login() {
        if (this.required("username")) {
            this.email("username");
        }
        if (this.required("password")) {
            this.minTextLength("password", 3);
        }
        return this.getErrorResponse();
    }

    /**
     * Validates addNewProfile data
     * @return ErrorResponse object
     */
    public ErrorResponse profile() {
        this.required("firstName");
        this.required("lastName");
        if (this.required("dateOfBirth")) {
            this.date("dateOfBirth");
        }
        this.gender("gender");
        this.required("nationalities");
        this.required("passports");
        this.required("travellerTypes");
        return this.getErrorResponse();
    }
}