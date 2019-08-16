package models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import io.ebean.Model;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import play.data.validation.Constraints;

/**
 * A class that models the tripData database table.
 */
@Table(name = "TripData")
@Entity
public class TripData extends Model {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long guid;

    @ManyToOne
    @JsonBackReference
    public Trip trip;

    @Constraints.Required
    public Long position;

    @OneToOne
    @JoinTable(
        name = "Destination",
        joinColumns = @JoinColumn(name = "destination_id", referencedColumnName = "id"))
    public Destination destination;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime arrivalTime;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime departureTime;

    @ManyToOne
    public Photo primaryPhotoId;

    /**
     * Transforms the arrival time to an ISO string an returns it.
     *
     * @return ISO string format of the arrival time or null
     */
    public String getArrivalTime() {
        if (arrivalTime != null) {
            return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(arrivalTime);
        } else {
            return null;
        }
    }

    /**
     * Transforms the departure time to an ISO string an returns it.
     *
     * @return ISO string format of the departure time or null
     */
    public String getDepartureTime() {
        if (departureTime != null) {
            return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(departureTime);
        } else {
            return null;
        }
    }
}