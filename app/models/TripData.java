package models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.annotation.JsonBackReference;
import io.ebean.Model;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * A class that models the tripData database table
 */
@Table(name="TripData")
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
            joinColumns=@JoinColumn(name="destination_id", referencedColumnName="id"))
    public Destination destination;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime arrivalTime;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime departureTime;
}