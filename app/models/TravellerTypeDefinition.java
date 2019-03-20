package models;

import io.ebean.Finder;
import io.ebean.Model;
import play.data.validation.Constraints;
import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.ManyToMany;
import java.util.List;

/**
 * A class that represents a traveller type and holds information received from database
 */
@Entity
@Table(name="TravellerTypeDefinition")
public class TravellerTypeDefinition extends Model {

    @Id
    public Long id;

    @Constraints.Required
    public String description;

    @ManyToMany(mappedBy = "travellerTypes")
    @JsonBackReference
    public List<Profile> profiles;
}
