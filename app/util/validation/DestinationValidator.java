package util.validation;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * A class which validates Destination information
 */
public class DestinationValidator extends Validator{
    public DestinationValidator(JsonNode form) {
        super(form, new ErrorResponse());
    }

    /**
     * Validates data when creating a new destination
     * @return an ErrorResponse object with relevant information about each failed field
     */
    public ErrorResponse addNewDestination () {
        //Don't want to check for id as it should be generated by the database
        //Check that the destination name is present
        this.required("name");
        //Check that the destination type is present
        this.required("_type");
        //Check that the destination district is present
        this.required("district");
        //Check that the destination's country id is present
        this.required("countryId");

        //Check that the latitude is present and within bounds
        if (this.required("latitude")) {
            this.max("latitude", 90);
            this.min("latitude", -90);
        }

        //Check that the longitude is present and within bounds
        if (this.required("longitude")) {
            this.max("longitude", 180);
            this.min("longitude", -180);
        }

        return this.getErrorResponse();
    }
}
