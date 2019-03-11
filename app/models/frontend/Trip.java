package models.frontend;

import java.util.List;
import java.util.Date;
import models.frontend.Destination;

public class Trip {
//    public Date departDate;
//    public Date arrivalDate;
    public String tripName;
    public List<Destination> tripList; // Trip is an array of destinations in order of travel

    public Trip(){}

    public Trip(List<Destination> tripList) {
//        this.departDate = departDate;
//        this.arrivalDate = arrivalDate;
        this.tripName = tripName;
        this.tripList = tripList;
    }


}
