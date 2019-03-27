package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.ebean.Model;
import play.data.validation.Constraints;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that represents a profile and hold information that is received from the database
 */
@Entity
@Table(name="Profile")
public class Profile extends Model {

    @Id
    @Constraints.Required
    public Long userId; //Unique user id

    @Constraints.Required
    public String firstName;

    @Constraints.Required
    public String lastName;

    public String middleName;

    public String dateOfBirth;

    public String gender;

//    @ManyToMany(cascade = CascadeType.ALL)
//    @JoinTable(
//            name="TravellerType",
//            joinColumns=@JoinColumn(name="user_id", referencedColumnName="user_id"),
//            inverseJoinColumns=@JoinColumn(name="traveller_type_id", referencedColumnName="id"))
    public List<TravellerTypeDefinition> travellerTypes;

//    @ManyToMany(cascade = CascadeType.ALL)
//    @JoinTable(
//            name = "Nationality",
//            joinColumns=@JoinColumn(name="user_id", referencedColumnName="user_id"),
//            inverseJoinColumns=@JoinColumn(name="country_id", referencedColumnName="id"))
    public List<CountryDefinition> nationalities;

//    @ManyToMany
//    @JoinTable(
//            name = "Passport",
//            joinColumns=@JoinColumn(name="user_id", referencedColumnName="user_id"),
//            inverseJoinColumns=@JoinColumn(name="country_id", referencedColumnName="id"))
    public List<CountryDefinition> passports;


    public int calculateAge() {
        LocalDate birthDate = LocalDate.parse(dateOfBirth, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        return age;
    }

    public List<CountryDefinition> getNationalities() {
        return nationalities;
    }
}