package models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.ebean.Model;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import play.data.validation.Constraints;

/**
 * A class that represents a traveller type and holds information received from database.
 */
@Entity
@Table(name = "TravellerTypeDefinition")
public class TravellerTypeDefinition extends Model {

    @Id
    public Long id;

    @Constraints.Required
    public String description;

    @ManyToMany(mappedBy = "travellerTypes")
    @JsonBackReference("tt-reference")
    @JoinTable(
        name = "TravellerType",
        joinColumns = @JoinColumn(name = "traveller_type_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "user_id"))

    public List<Profile> travellerTypes;

    @ManyToMany(mappedBy = "travellerTypes")
    @JsonBackReference("desttt-reference")
    @JoinTable(
        name = "DestinationTravellerType",
        joinColumns = @JoinColumn(name = "traveller_type_definition_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "dest_id", referencedColumnName = "id"))

    public List<Destination> destTravellerTypes;

    @ManyToMany(mappedBy = "travellerTypesPending")
    @JsonBackReference("desttt-pending-reference")
    @JoinTable(
        name = "DestinationTravellerTypePending",
        joinColumns = @JoinColumn(name = "traveller_type_definition_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "dest_id", referencedColumnName = "id"))

    public List<Destination> destTravellerTypesPending;

    @Override
    public String toString() {
        StringBuilder toReturn = new StringBuilder();

        toReturn.append(description.substring(0, 1).toUpperCase());

        //Captialises the traveller type properly
        for (int i = 1; i < description.length(); i++) {
            char c = description.charAt(i - 1);
            if (c == '/' || c == '\\' || c == ' ') {
                toReturn.append(description.substring(i, i + 1).toUpperCase());
            } else {
                toReturn.append(description.substring(i, i + 1));
            }
        }
        return toReturn.toString();
    }
}
