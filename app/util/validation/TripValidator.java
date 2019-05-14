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

        // Validation for trip privacy
        this.required("privacy");

        //Validation for TripData objects
        // Now deserialize it to a list of trip data objects, and check each of these
        ObjectMapper mapper = new ObjectMapper();
        ArrayList tripDataCollection = mapper
            .readValue(mapper.treeAsTokens(this.form.get("tripDataList")),
                new TypeReference<ArrayList<TripData>>() {
                });

        if (tripDataCollection.size() < 2) {
            this.response.map("A trip must contain at least 2 destinations.", "trip");
        }

        Long lastDestinationID = 0L;
        LocalDateTime mostRecentDateTime = null;

        for (Object obj : tripDataCollection) {
            TripData trip = (TripData) obj;
            String errorString = "";

            // Validates card information and creates error string
            if (trip.destination != null && trip.destination.id == null) {
                errorString = "Invalid destination ID.";
            }
            else if (trip.destination != null && trip.destination.id.equals(lastDestinationID)) {
                errorString = "You cannot have the same destination twice in a row.";
            }
            else if (trip.destination == null) {
                errorString = "Destination not found.";
            }
            else if (trip.position == null) {
                errorString = "Position of destination not found.";
                trip.position = -1L;
            }
            else if (trip.arrivalTime != null && trip.departureTime != null && trip.arrivalTime.isAfter(trip.departureTime)) {
                errorString = "The arrival time must be before the departure time.";
            }
            else if (trip.arrivalTime != null && mostRecentDateTime != null && trip.arrivalTime.isBefore(mostRecentDateTime)) {
                errorString = "The arrival time for this destination cannot be before a previous destination.";
                mostRecentDateTime = ((trip.departureTime != null) ? trip.departureTime : trip.arrivalTime);
            }
            else if (trip.departureTime != null && mostRecentDateTime != null && trip.departureTime.isBefore(mostRecentDateTime)) {
                errorString = "The departure time for this destination cannot be before a previous destination.";
                mostRecentDateTime = ((trip.departureTime != null) ? trip.departureTime : trip.arrivalTime);
            }

            // Sets most recent time and last destination values
            if (trip.arrivalTime != null || trip.departureTime != null) {
                mostRecentDateTime = ((trip.departureTime != null) ? trip.departureTime : trip.arrivalTime);
            }

            if (trip.destination != null && trip.destination.id != null) {
                lastDestinationID = trip.destination.id;
            }

            // If any errors were added to string, add this to error response map
            if (!errorString.equals("")) {
                this.response.map(errorString, trip.position.toString());
            }
        }

        return this.response;
    }

    /**
     * Validates trip privacy update data
     *
     * @return Error response object containing error messages
     */
    public ErrorResponse validateTripPrivacyUpdate() {
        // Validation for trip as a whole
        this.required("id");

        // Validation for trip privacy
        this.required("privacy");

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
