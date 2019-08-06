package models;

import io.ebean.Model;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
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

    @ManyToMany(mappedBy = "destinationTags")
//    @JsonBackReference("test")
    @JoinTable(
        name = "DestinationTag",
        joinColumns = @JoinColumn(name = "tag_id", referencedColumnName = "guid"),
        inverseJoinColumns = @JoinColumn(name = "destination_id", referencedColumnName = "id"))
    public List<Destination> destinations;
}
