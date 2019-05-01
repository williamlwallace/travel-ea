package util.validation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import models.TripData;

public class TripValidator {

    private final JsonNode form;
    private ErrorResponse response;

    public TripValidator(JsonNode form) {
        this.form = form;
        response = new ErrorResponse();
    }

    /**
     * Validates addNewTrip data.
     *
     * @return ErrorResponse object
     */
    public ErrorResponse validateTrip(boolean isUpdating) throws IOException {

        //Validation for trip as a whole
        if (isUpdating) {
            this.required("id");
        }

        //Validation for TripData objects
        // Now deserialize it to a list of trip data objects, and check each of these
        ObjectMapper mapper = new ObjectMapper();
        ArrayList tripDataCollection = mapper
            .readValue(mapper.treeAsTokens(this.form.get("tripDataList")),
                new TypeReference<ArrayList<TripData>>() {
                });

        if (tripDataCollection.size() < 2) {
            this.response.map("a trip must contain at least 2 destinations", "trip");
        }

        Long lastDestinationID = 0L;
        LocalDateTime mostRecentDateTime = null;

        for (Object obj : tripDataCollection) {
            TripData trip = (TripData) obj;
            String errorString = "";

            if (trip.position == null) {
                errorString += "position is null, ";
                trip.position = -1L;
            }

            if (trip.destination.id == null) {
                errorString += "destinationId is null, ";
            }
            if (trip.destination.id == lastDestinationID) {
                errorString += "cannot attend same destination twice in a row, ";
            }
            if (trip.arrivalTime != null && trip.departureTime != null && trip.arrivalTime
                .isAfter(trip.departureTime)) {
                errorString += "departure must be after arrival, ";
            }
            if (trip.arrivalTime != null || trip.departureTime != null) {
                //todo: fix this one
                //if(!(mostRecentDate == null) && (trip.arrivalTime.before(mostRecentDate) || trip.departureTime.before(mostRecentDate))) {
                //    errorString += "this stage cannot occur before a previous stage, ";
                //}
                // Set most recent time stamp to be latest value that is not null
                mostRecentDateTime = ((trip.departureTime != null) ? trip.departureTime
                    : trip.arrivalTime);
            }

            lastDestinationID = trip.destination.id;

            // If any errors were added to string, add this to error response map
            if (!errorString.equals("")) {
                this.response.map(errorString, trip.position.toString());
            }

        }
        return this.response;
    }

    /**
     * Checks field is not empty.
     *
     * @param field json field name
     * @return Boolean whether validation succeeds
     */
    protected Boolean required(String field) {
        if (this.form.get(field).asText("").equals("")) {
            this.response.map(String.format("%s required", field), field);
            return false;
        }
        return true;
    }

    public ErrorResponse getAllTripsByUser() {
        this.required("userId");

        return this.response;
    }
}
