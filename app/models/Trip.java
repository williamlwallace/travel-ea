package models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.ebean.Model;
import java.util.List;
import javax.persistence.*;

import play.data.validation.Constraints;

/**
 * A class that models the trip database table.
 */
@Entity
@Table(name = "Trip")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Trip extends Model implements Comparable<Trip> {

    @Id
    public Long id;

    @Constraints.Required
    public Long userId;

    @Constraints.Required
    @Column(name = "is_public")
    public boolean isPublic;

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
                String arrivalYear = tripData.arrivalTime.toString().substring(0, 4);
                String arrivalMonth = tripData.arrivalTime.toString().substring(5, 7);
                String arrivalDay = tripData.arrivalTime.toString().substring(8, 10);
                return arrivalDay + "-" + arrivalMonth + "-" + arrivalYear;
            } else if (tripData.departureTime != null) {
                String departYear = tripData.departureTime.toString().substring(0, 4);
                String departMonth = tripData.departureTime.toString().substring(5, 7);
                String departDay = tripData.departureTime.toString().substring(8, 10);
                return departDay + "-" + departMonth + "-" + departYear;
            }
        }

        return null;
    }

    /**
     * Comparator which allows for trips to be compared and sorted by date. Will sort by recent
     * first, with nulls last
     *
     * @param other Trip to compare against
     * @return Negative or zero integer if this trip should be first, otherwise positive
     */
    @Override
    public int compareTo(Trip other) {
        if (other.findFirstTripDate() == null) {
            return -1;
        } else if (this.findFirstTripDate() == null) {
            return 1;
        } else {
            return other.findFirstTripDate().compareTo(this.findFirstTripDate());
        }
    }
}
