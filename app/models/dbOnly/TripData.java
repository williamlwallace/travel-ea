package models.dbOnly;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import io.ebean.Model;
import models.Destination;
import models.Trip;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * A class that models the tripData database table
 */
@Table(name="TripData")
@Entity
public class TripData extends Model {

    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long guid;

    @Id
    public Long tripId;

    @Constraints.Required
    public Long position;

    @Constraints.Required
    public Long destinationId;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime arrivalTime;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime departureTime;
}