package models;

import io.ebean.Finder;
import io.ebean.Model;
import play.data.validation.Constraints;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.io.Serializable;
import java.security.Timestamp;
import java.sql.Time;
import java.util.Date;
import java.util.Objects;

/**
 * A class that models the tripData database table
 */
@Table(name="TripData")
@Entity
public class TripData extends Model {

    @Id
    public Long guid;

    @Constraints.Required
    public Long tripId;

    @Constraints.Required
    public Long position;

    @Constraints.Required
    public Long destinationId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    public Timestamp arrivalTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    public Timestamp departureTime;
}