package models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.ebean.Model;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import play.data.validation.Constraints;

import java.util.List;

/**
 * A class that represents a traveller type and holds information received from database
 */
@Entity
@Table(name = "TravellerTypeDefinition")
public class TravellerTypeDefinition extends Model {

    @Id
    public Long id;

    @Constraints.Required
    public String description;

    @Override
    public String toString() {
        String toReturn = description.substring(0, 1).toUpperCase();

        //Captialises the traveller type properly
        for (int i = 1; i < description.length(); i++) {
            char c = description.charAt(i - 1);
            if (c == '/' || c == '\\' || c == ' ') {
                toReturn += description.substring(i, i + 1).toUpperCase();
            } else {
                toReturn += description.substring(i, i + 1);
            }
        }
        return toReturn;
//        return description.substring(0, 1).toUpperCase() + description.substring(1);
    }

    @ManyToMany(mappedBy = "travellerTypes")
    @JsonBackReference
    public List<Profile> travellerTypes;
}
