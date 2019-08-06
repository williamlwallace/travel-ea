package models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.ebean.Model;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
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
    public List<Destination> destinations;

    @JsonBackReference("PhotoTagReference")
    @ManyToMany(mappedBy = "tags")
    public List<Photo> photos;

    @JsonBackReference("TripsTagReference")
    @ManyToMany(mappedBy = "tags")
    public List<Trip> trips;

    @JsonBackReference("UsedTagReference")
    @ManyToMany(mappedBy = "usedTags")
    public List<User> users;

}
