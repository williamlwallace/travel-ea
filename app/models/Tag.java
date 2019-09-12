package models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.ebean.Model;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import play.data.validation.Constraints;

/**
 * A class that models the Tag table from the database.
 */
@Entity
@Table(name = "Tag")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tag extends Model {

    @Id
    @JsonIgnore
    public Long id;

    @Constraints.Required
    public String name;

    @JsonBackReference("DestinationTagReference")
    @ManyToMany(mappedBy = "tags") //The tags variable/list in the Destination Model
    @JoinTable(
        name = "DestinationTag",
        joinColumns = @JoinColumn(name = "tag_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "destination_id", referencedColumnName = "id"))
    public Set<Destination> destinations;

    @ManyToMany(mappedBy = "tags")
    @JsonBackReference("PhotoTagReference")
    @JoinTable(
        name = "PhotoTag",
        joinColumns = @JoinColumn(name = "tag_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "photo_id", referencedColumnName = "guid"))
    public Set<Photo> photos;

    @JsonBackReference("TripsTagReference")
    @ManyToMany(mappedBy = "tags")
    @JoinTable(
        name = "TripTag",
        joinColumns = @JoinColumn(name = "tag_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "trip_id", referencedColumnName = "id"))
    public Set<Trip> trips;

    @OneToMany(mappedBy = "tag")
    @JsonIgnore
    public Set<UsedTag> usedTags;

    /**
     * Constructor, this is mainly for ease and conciseness of testing.
     *
     * I have made it so you must instantiate a Tag with a name, this is because the hashCode method
     * hashes based only on name
     */
    public Tag(String name) {
        this.name = name;
    }

    /**
     * Constructor, this is mainly for ease and conciseness of testing.
     *
     * I have made it so you must instantiate a Tag with a name, this is because the hashCode method
     * hashes based only on name
     */
    public Tag(String name, Long id) {
        this.name = name;
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Tag)) {
            return false;
        }

        Tag tag = (Tag) o;
        if (name == null || tag.name == null) {
            return Objects.equals(id, tag.id);
        } else {
            return Objects.equals(name, tag.name);
        }
    }

    @Override
    public final int hashCode() {
        return Objects.hash(name);
    }
}
