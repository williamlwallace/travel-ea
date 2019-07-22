package util.validation;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Validator for TreasureHunt controller methods
 */
public class TreasureHuntValidator extends Validator {

    private final JsonNode form;

    public TreasureHuntValidator(JsonNode form) {
        super(form, new ErrorResponse());
        this.form = form;
    }

    /**
     * Validates data for a TreasureHunt object
     *
     * @return Error response containing error information if it has any
     */
    public ErrorResponse validateTreasureHunt() {
        if (this.required("user", "User") && this.form.get("user").get("id").asText("")
            .equals("")) {
            this.required("userId", "UserId");
        }

        if (this.required("destination", "Destination") && this.form.get("destination").get("id")
            .asText("").equals("")) {
            this.required("destinationId", "DestinationId");
        }

        this.required("riddle", "Riddle");
        this.required("startDate", "Start date");
        this.required("endDate", "End date");

        return this.getErrorResponse();
    }
}
