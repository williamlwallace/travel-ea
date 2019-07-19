package util.validation;

import com.fasterxml.jackson.databind.JsonNode;

public class CountryValidator extends Validator {

    public CountryValidator(JsonNode form) {
        super(form, new ErrorResponse());
    }

    public ErrorResponse validateCountry() {
        this.required("id", "Country Code (id)");
        this.required("name", "Country Name");
        this.isInt("id");
        this.isText("name");

        return this.getErrorResponse();
    }

}
