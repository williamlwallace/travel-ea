package util.validation;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Validator subclass to provide validation for the user controller.
 */
public class UserValidator extends Validator {

    public UserValidator(JsonNode form) {
        super(form, new ErrorResponse());
    }

    /**
     * Validates login data
     *
     * @return ErrorResponse object
     */
    public ErrorResponse login() {
        if (this.required("username", "Username")) {
            this.email("username");
        }
        if (this.required("password", "Password")) {
            this.minTextLength("password", 3);
        }
        return this.getErrorResponse();
    }

    /**
     * Validates addNewProfile data NB passports are not required.
     *
     * @return ErrorResponse object
     */
    public ErrorResponse profile() {
        this.required("firstName", "First Name");
        this.required("lastName", "Last Name");
        if (this.required("dateOfBirth", "Date of Birth")) {
            this.date("dateOfBirth");
        }
        this.gender("gender");
        this.required("nationalities", "Nationality");
        this.required("travellerTypes", "Traveller Type");
        return this.getErrorResponse();
    }
}