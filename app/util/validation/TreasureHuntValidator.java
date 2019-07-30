package util.validation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDate;
import models.TreasureHunt;

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
    public ErrorResponse validateTreasureHunt(boolean isUpdating) throws IOException {
        if((!isUpdating) && (this.required("user", "User") && this.form.get("user").get("id").asText("")
            .equals(""))) {
            this.required("userId", "User ID");
        }

        if (this.required("destination", "Destination") && this.form.get("destination").get("id")
            .asText("").equals("")) {
            this.required("destinationId", "DestinationId");
        }

        this.required("riddle", "Riddle");

        boolean validStartDate = this.required("startDate", "Start date");
        boolean validEndDate = this.required("endDate", "End date");

        if (validEndDate && validStartDate) {
            ObjectMapper mapper = new ObjectMapper();
            TreasureHunt treasureHunt = mapper
                .readValue(mapper.treeAsTokens(this.form), new TypeReference<TreasureHunt>() {
                });

            if (LocalDate.now().isAfter(treasureHunt.startDate)) {
                this.getErrorResponse().map("The start date cannot be before today's date.", "startDate");
            }

            if (treasureHunt.startDate.isAfter(treasureHunt.endDate)) {
                this.getErrorResponse().map("The end date cannot be before the start date.", "endDate");
            }
        }

        return this.getErrorResponse();
    }
}
