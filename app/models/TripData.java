package models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.annotation.JsonBackReference;
import io.ebean.Finder;
import io.ebean.Model;
import org.springframework.format.annotation.DateTimeFormat;
import play.data.validation.Constraints;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.io.Serializable;
import java.security.Timestamp;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

/**
 * A class that models the tripData database table
 */
@Table(name="TripData")
@Entity
public class TripData extends Model {

    @GeneratedValue(strategy = GenerationType.AUTO)
    public long guid;

    @ManyToOne
    @JsonBackReference
    public Trip trip;

    @Id
    @Column(name="trip_id")
    public Long tripId;

    @Constraints.Required
    public Long position;

    //@Constraints.Required
    //public Long destinationId;

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