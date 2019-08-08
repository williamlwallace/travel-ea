package models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.ebean.Model;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import play.data.validation.Constraints;

/**
 * A class that models the Tag table from the database
 */
@Entity
@Table(name = "Tag")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tag extends Model {

    @Id
    public Long id;

    @Constraints.Required
    public String name;

    @JsonBackReference("DestinationTagReference")
    @ManyToMany(mappedBy = "tags") //The tags variable/list in the Destination Model
    @JoinTable(
        name = "DestinationTag",
        joinColumns = @JoinColumn(name = "tag_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "destination_id", referencedColumnName = "id"))
    public List<Destination> destinations;

    @ManyToMany(mappedBy = "tags")
    @JsonBackReference("PhotoTagReference")
    @JoinTable(
        name = "PhotoTag",
        joinColumns = @JoinColumn(name = "tag_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "photo_id", referencedColumnName = "guid"))
    public List<Photo> photos;

    @JsonBackReference("TripsTagReference")
    @ManyToMany(mappedBy = "tags")
    @JoinTable(
        name = "TripTag",
        joinColumns = @JoinColumn(name = "tag_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "trip_id", referencedColumnName = "id"))
    public List<Trip> trips;

//    @ManyToMany(mappedBy = "usedTags")
//    @JsonBackReference("UsedTagReference")
//    @JoinTable(
//        name = "UsedTag",
//        joinColumns = @JoinColumn(name = "tag_id", referencedColumnName = "id"),
//        inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
//    public List<User> users;

    @OneToMany(mappedBy = "tag")
    public List<UsedTag> usedTags;

}
