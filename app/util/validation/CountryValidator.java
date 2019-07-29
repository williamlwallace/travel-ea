package util.validation;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * A class which validates country information
 */
public class CountryValidator extends Validator {

    public CountryValidator(JsonNode form) {
        super(form, new ErrorResponse());
    }

    /**
     * Validates a country has all the required fields
     *
     * @return an ErrorResponse object with relevant information about each failed field (if any)
     */
    public ErrorResponse validateCountry() {
        this.required("id", "Country Code (id)");
        this.required("name", "Country Name");
        this.isInt("id");
        this.isText("name");

        return this.getErrorResponse();
    }

}
