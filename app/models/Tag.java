package models;

import io.ebean.Model;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import play.data.validation.Constraints;

@Entity
@Table(name = "Tag")
public class Tag extends Model {

    @Id
    public Long id;

    @Constraints.Required
    public String name;

    @ManyToMany(mappedBy = "tags") //The tags variable/list in the Destination Model
    public List<Destination> destinations;

    @ManyToMany(mappedBy = "tags")
    public List<Photo> photos;

    @ManyToMany(mappedBy = "tags")
    public List<Trip> trips;

    @ManyToMany(mappedBy = "usedTags")
    public List<User> users;

}
