package util.validation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.Trip;
import models.TripData;
import models.TripData.TripDataKey;
import play.libs.Json;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;

public class TripValidator {
    private final JsonNode form;
    private ErrorResponse response;

    public TripValidator(JsonNode form) {
        this.form = form;
        response = new ErrorResponse();
    }

    /**
     * Validates addNewTrip data
     * @return ErrorResponse object
     */
    public ErrorResponse validateTrip(boolean isUpdating) throws IOException {
        // Check if uid has a value
        //Validation for trip as a whole
        this.required("uid");

        if(isUpdating) {
            this.required("id");
        }

        //Validation for TripData objects
        // Now deserialize it to a list of trip data objects, and check each of these
        ObjectMapper mapper = new ObjectMapper();
        ArrayList tripDataCollection = mapper.readValue(mapper.treeAsTokens(this.form.get("tripDataCollection")), new TypeReference<ArrayList<TripData>>(){});

        Long lastDestinationID = 0L;
        Date mostRecentDate = null;

        for (Object obj : tripDataCollection) {
            TripData trip = (TripData) obj;
            trip.key = new TripDataKey(trip.tripId, trip.position);
            String errorString = "";
            if(trip.key.position == null) {
                errorString += "position is null, ";
                trip.key.position = -1L;
            }
            if(trip.destinationId == null) errorString += "destinationId is null, ";
            if(trip.destinationId == lastDestinationID) errorString += "cannot attend same destination twice in a row, ";
            if(trip.arrivalTime != null && trip.departureTime != null && trip.arrivalTime.after(trip.departureTime)) errorString += "departure must be after arrival, ";
            if(trip.arrivalTime != null || trip.departureTime != null) {
                //todo: fix this one
                //if(!(mostRecentDate == null) && (trip.arrivalTime.before(mostRecentDate) || trip.departureTime.before(mostRecentDate))) {
                //    errorString += "this stage cannot occur before a previous stage, ";
                //}
                // Set most recent time stamp to be latest value that is not null
                mostRecentDate = (trip.departureTime == null) ? trip.arrivalTime : trip.departureTime;
            }

            // Update most recent destination id
            lastDestinationID = trip.destinationId;

            // If any errors were added to string, add this to error response map
            if(!errorString.equals("")) {
                this.response.map(errorString, trip.key.position.toString());
            }

        }
        return this.response;
    }

    /**
     * Checks field is not empty
     * @param field json field name
     * @return Boolean whether validation succeeds
     */
    protected Boolean required(String field) {
        if (this.form.get(field).asText("") == "") {
            this.response.map(String.format("%s required", field), field);
            return false;
        }
        return true;
    }

    public ErrorResponse getAllTripsByUser() {
        this.required("uid");

        return this.response;
    }
}
