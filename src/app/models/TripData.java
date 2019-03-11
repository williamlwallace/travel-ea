package models;

import io.ebean.Finder;
import io.ebean.Model;
import play.data.validation.Constraints;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import java.sql.Timestamp;

/**
 * A class that models the tripData database table
 */
@Entity
@JsonIgnoreProperties(ignoreUnknown=true)
@Table(name = "tripData")
public class TripData extends Model {

    @EmbeddedId
    public TripDataKey key;

    @Constraints.Required
    @Column(name="destinationId")
    public Long destinationId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    @Column(name="arrivalTime")
    public Timestamp arrivalTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    @Column(name="departureTime")
    public Timestamp departureTime;

    @Embeddable
    public static class TripDataKey implements Serializable {
        @Constraints.Required
        @Column(name="tripId")
        public Long tripId;

        @Constraints.Required
        public Long position;


        //Default constructor -- not strictly necessary
        public TripDataKey() {}

        //This constructor will mainly be used to create keys to search with
        public TripDataKey(Long tripId, Long position) {
            this.tripId = tripId;
            this.position = position;
        }


        //We must include the equals method so that the program knows how to check if two TripDataKeys are the same
        @Override
        public boolean equals(Object object) {
            //If the object checking against is itself
            if (this == object) return true;

            //If the object is not a TripDataKey
            if (!(object instanceof TripDataKey)) return false;

            //Cast the object to TripDataKey, now that we know it is one
            TripDataKey testObject = (TripDataKey) object;
            //Return whether the two component keys equal each other
            return Objects.equals(tripId, testObject.tripId) && Objects.equals(position, testObject.position);
        }

        //We must include the hashCode method so that the key can be hashed
        @Override
        public int hashCode() {
            return Objects.hash(tripId, position);
        }
    }

    //Non-key values, currently used for validation in TripValidator
    @Column(name="tripId")
    public Long tripId;
    public Long position;


    public static final Finder<TripDataKey, TripData> find = new Finder<>(TripData.class);
}