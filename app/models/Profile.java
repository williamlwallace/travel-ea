package models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.ebean.Model;
import play.data.validation.Constraints;
import javax.persistence.*;
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

    @ManyToMany
    @JoinTable(
        name="TravellerType",
        joinColumns=@JoinColumn(name="user_id", referencedColumnName="user_id"),
        inverseJoinColumns=@JoinColumn(name="traveller_type_id", referencedColumnName="id"))
    public List<TravellerTypeDefinition> travellerTypes;

    @ManyToMany
    @JoinTable(
        name = "Nationality",
        joinColumns=@JoinColumn(name="user_id", referencedColumnName="user_id"),
        inverseJoinColumns=@JoinColumn(name="country_id", referencedColumnName="id"))
    public List<CountryDefinition> nationalities;

    @ManyToMany
    @JoinTable(
            name = "Passport",
            joinColumns=@JoinColumn(name="user_id", referencedColumnName="user_id"),
            inverseJoinColumns=@JoinColumn(name="country_id", referencedColumnName="id"))
    public List<CountryDefinition> passports;
}