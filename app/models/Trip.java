package models;

import io.ebean.Model;

import java.util.Comparator;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import play.data.validation.Constraints;

/**
 * A class that models the trip database table.
 */
@Entity
@Table(name = "Trip")
public class Trip extends Model {

    @Id
    public Long id;

    @Constraints.Required
    public Long userId;

    @Constraints.Required
    public Long privacy;

    @OneToMany(cascade = CascadeType.ALL)
    public List<TripData> tripDataList;

    /**
     * Finds the first date in a trip, if there is one.
     *
     * @return the date found  as a string or null if no date was found
     */
    public String findFirstTripDate() {
        for (TripData tripData : tripDataList) {
            if (tripData.arrivalTime != null) {
                return tripData.arrivalTime.toString().substring(0, 10);
            } else if (tripData.departureTime != null) {
                return tripData.departureTime.toString().substring(0, 10);
            }
        }

        return null;
    }

    /**
     * Comparator which allows for trips to be compared and sorted by date
     * Will sort by recent first, with nulls last
     */
    public Comparator<Trip> compareByDate1 = new Comparator<Trip>() {
        public int compare(Trip trip1, Trip trip2) {
            if (trip2.findFirstTripDate() == null) {
                return -1;
            } else if (trip1.findFirstTripDate() == null) {
                return 1;
            } else {
                return trip2.findFirstTripDate().compareTo(trip1.findFirstTripDate());
            }
        }
    };

    /**
     * Comparator which allows for trips to be compared and sorted by date
     * Will sort by recent first, with nulls last
     */
    public Comparator<Trip> compareByDate = (Trip trip1, Trip trip2) -> {
        if (trip2.findFirstTripDate() == null) {
            return -1;
        } else if (trip1.findFirstTripDate() == null) {
            return 1;
        } else {
            return trip2.findFirstTripDate().compareTo(trip1.findFirstTripDate());
        }
    };
}
