package controllers;

import play.data.validation.Constraints;
import java.util.List;
import models.*;

/**
 * Stores user created Trips
 */
public class TripData {

    @Constraints.Required
    private String tripName;
    private List<Destination> tripList;


    public TripData() { }

    public String getName() {
        return tripName;
    }

    public void setName(String name) {
        this.tripName = tripName;
    }

    public List<Destination> getTripList() {
        return tripList;
    }


}
