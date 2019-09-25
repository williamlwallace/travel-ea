package util.validation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import models.Destination;
import models.Trip;
import models.TripData;

/**
 * A class which validates Trip information.
 */
public class TripValidator extends Validator {

    private final JsonNode form;

    public TripValidator(JsonNode form) {
        super(form, new ErrorResponse());
        this.form = form;
    }

    /**
     * Validates addNewTrip data.
     *
     * @return ErrorResponse object
     */
    public ErrorResponse validateTrip(boolean isUpdating) {

        // Validation for trip as a whole
        if (isUpdating) {
            this.required("id", "Trip Id");
        }

        this.required("userId", "User Id");
        this.required("isPublic", "Privacy");
        this.requiredTags("tags", "Tags");

        // Validation for TripData objects
        // Now deserialize it to a list of trip data objects, and check each of these
        ObjectMapper mapper = new ObjectMapper();

        try {
            ArrayList tripDataCollection = mapper
                .readValue(mapper.treeAsTokens(this.form.get("tripDataList")),
                    new TypeReference<ArrayList<TripData>>() {
                    });

            if (tripDataCollection.size() < 2) {
                this.getErrorResponse().map("A trip must contain at least 2 destinations.", "trip");
            }

            Long lastDestinationID = 0L;
            LocalDateTime mostRecentDateTime = null;

            for (Object obj : tripDataCollection) {
                TripData trip = (TripData) obj;

                // Checks for destination errors
                String errorString = checkDestinationData(trip, lastDestinationID);

                // Checks for date/time errors
                if (errorString.equals("")) {
                    errorString = checkArrivalDepartureData(trip, mostRecentDateTime);
                }

                // Checks for position errors
                if (errorString.equals("") && trip.position == null) {
                    errorString = "Position of destination not found.";
                    trip.position = -1L;
                }

                // Sets most recent date time value
                if (trip.arrivalTime != null && (mostRecentDateTime == null || trip.arrivalTime
                    .isAfter(mostRecentDateTime))) {
                    mostRecentDateTime = trip.arrivalTime;
                }

                if (trip.departureTime != null && (mostRecentDateTime == null || trip.departureTime
                    .isAfter(mostRecentDateTime))) {
                    mostRecentDateTime = trip.departureTime;
                }

                // Sets last destination ID value
                if (trip.destination != null && trip.destination.id != null) {
                    lastDestinationID = trip.destination.id;
                }

                // If any errors were added to string, add this to error response map
                if (!errorString.equals("")) {
                    this.getErrorResponse().map(errorString, trip.position.toString());
                }
            }
        } catch (IOException ex) {
            this.getErrorResponse().map("Trip data could not be deserialized", "trip");
        }

        return this.getErrorResponse();
    }

    /**
     * Validates trip privacy update data.
     *
     * @return Error response object containing error messages
     */
    public ErrorResponse validateTripPrivacyUpdate() {
        // Validation for trip as a whole
        this.required("id", "Id");

        // Validation for trip privacy
        this.required("isPublic", "Privacy");

        return this.getErrorResponse();
    }

    /**
     * Validates a trip is not public with private destinations
     *
     * @param trip Trip object with destination information
     * @return Error response containing error messages
     */
    public ErrorResponse validateDestinationPrivacy(Trip trip) {
        if (trip.isPublic) {
            for (TripData tripData : trip.tripDataList) {
                if (!tripData.destination.isPublic) {
                    this.getErrorResponse().map("A public trip cannot contain a private destination",
                        tripData.position.toString());
                }
            }
        }

        return this.getErrorResponse();
    }

    /**
     * Checks the destination data for the current destination and returns error an message.
     *
     * @param trip TripData object to be analysed
     * @param lastDestinationID Destination ID of card previous to current card
     * @return Error message if error found or empty string
     */
    private String checkDestinationData(TripData trip, Long lastDestinationID) {
        if (trip.destination != null && trip.destination.id == null) {
            return "Invalid destination ID.";
        } else if (trip.destination != null && trip.destination.id.equals(lastDestinationID)) {
            return "You cannot have the same destination twice in a row.";
        } else if (trip.destination == null) {
            return "Destination not found.";
        }

        return "";
    }

    /**
     * Checks the arrival and departure times for the destination and returns error an message.
     *
     * @param trip TripData object to be analysed
     * @param mostRecentDateTime Most recent recorded point of trip up to this destination card
     * @return Error message if error found or empty string
     */
    private String checkArrivalDepartureData(TripData trip, LocalDateTime mostRecentDateTime) {
        if (trip.arrivalTime != null && trip.departureTime != null && trip.arrivalTime
            .isAfter(trip.departureTime)) {
            return "The arrival time must be before the departure time.";
        } else if (trip.arrivalTime != null && mostRecentDateTime != null && trip.arrivalTime
            .isBefore(mostRecentDateTime)) {
            return "The arrival time for this destination cannot be before a previous destination.";
        } else if (trip.departureTime != null && mostRecentDateTime != null && trip.departureTime
            .isBefore(mostRecentDateTime)) {
            return "The departure time for this destination"
                + " cannot be before a previous destination.";
        }

        return "";
    }
}
