package models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.ebean.Finder;
import io.ebean.Model;
import play.data.validation.Constraints;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.io.Serializable;
import java.security.Timestamp;
import java.util.Objects;

/**
 * A class that models the tripData database table
 */
@Table(name="TripData")
@Entity
public class TripData extends Model {

    public long guid;

    @ManyToOne
    @JsonBackReference
    public Trip trip;

    @Id
    @Column(name="trip_id")
    @Constraints.Required
    public Long tripId;

    @Constraints.Required
    public Long position;

    @Constraints.Required
    public Long destinationId;

    @OneToOne
    @JoinTable(
            name = "Destination",
            joinColumns=@JoinColumn(name="destination_id", referencedColumnName="id"))
    public Destination destination;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    public Timestamp arrivalTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    public Timestamp departureTime;
}