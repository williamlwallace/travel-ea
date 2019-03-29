package models;

import io.ebean.Model;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.util.List;

/**
 * A class that models the trip database table
 */
@Entity
@Table(name="Trip")
public class Trip extends Model {

    @Id
    public Long id;

    @Constraints.Required
    public Long userId;

    @OneToMany(cascade = CascadeType.ALL)
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
