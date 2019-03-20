package models;

import io.ebean.Finder;
import io.ebean.Model;
import play.data.validation.Constraints;
import play.data.format.*;
import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.util.Date;
import java.util.List;

/**
 * A class that represents a profile and hold information that is received from the database
 */
@Entity
@Table(name="Profile")
public class Profile extends Model {

    @Id
    @Constraints.Required
    @Column(name="user_id")
    public Long userId; //Unique user id

    @Constraints.Required
    public String firstName;

    @Constraints.Required
    public String lastName;

    public String middleName;

    public String dateOfBirth;

    public String gender;

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(
        name="TravellerType",
        joinColumns=@JoinColumn(name="user_id", referencedColumnName="user_id"),
            inverseJoinColumns=@JoinColumn(name="traveller_type_id", referencedColumnName="id"))
    @JsonManagedReference
    public List<TravellerTypeDefinition> travellerTypes;
}