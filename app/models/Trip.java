package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import play.data.validation.Constraints;

/**
 * A class that models the trip database table.
 */
@Entity
@Table(name = "Trip")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Trip extends BaseModel implements Comparable<Trip>, Taggable {

    @Id
    public Long id;

    @Constraints.Required
    public Long userId;

    @Constraints.Required
    @Column(name = "is_public")
    public boolean isPublic;

    @OneToMany(cascade = CascadeType.ALL)
    public List<TripData> tripDataList;

    @ManyToMany(mappedBy = "trips")
    @JoinTable(
        name = "TripTag",
        joinColumns = @JoinColumn(name = "trip_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id", referencedColumnName = "id"))
    public Set<Tag> tags;

    /**
     * Returns the list of tags associated with the object
     *
     * @return a list of Tags
     */
    @JsonIgnore
    public Set<Tag> getTagsList() {
        return tags;
    }

    /**
     * Finds the first date in a trip.
     *
     * @return The first date in a trip, or null
     */
    public LocalDateTime findFirstTripDateAsDate() {
        for (TripData tripData : tripDataList) {
            if (tripData.arrivalTime != null) {
                return tripData.arrivalTime;
            } else if (tripData.departureTime != null) {
                return tripData.departureTime;
            }
        }

        return null;
    }

    /**
     * Comparator which allows for trips to be compared and sorted by date. Will sort by recent
     * first, with nulls last.
     *
     * @param other Trip to compare against
     * @return Negative or zero integer if this trip should be first, otherwise positive
     */
    @Override
    public int compareTo(Trip other) {
        if (other.findFirstTripDateAsDate() == null) {
            return -1;
        } else if (this.findFirstTripDateAsDate() == null) {
            return 1;
        } else {
            return other.findFirstTripDateAsDate().compareTo(this.findFirstTripDateAsDate());
        }
    }
}
