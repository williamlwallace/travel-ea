package models;

import java.util.List;

/**
 * A class that models the trip database table
 */
public class Trip {

    public Long id;

    public Long userId;

    public List<TripData> tripDataList;

    public String findFirstTripDate() {
        for (TripData tripData : tripDataList) {
            if (tripData.arrivalTime != null) {
                return tripData.arrivalTime.toString().substring(0, 10);
            }
            else if (tripData.departureTime != null) {
                return tripData.departureTime.toString().substring(0, 10);
            }
        }

        return null;
    }
}
