package models;

import io.ebean.Model;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.persistence.*;

import play.data.validation.Constraints;

/**
 * A class that represents a profile and hold information that is received from the database
 */
@Entity
@Table(name = "Profile")
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

    public LocalDateTime creationDate;

    public String gender;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name="TravellerType",
            joinColumns=@JoinColumn(name="user_id", referencedColumnName="user_id"),
            inverseJoinColumns=@JoinColumn(name="traveller_type_id", referencedColumnName="id"))
    public List<TravellerTypeDefinition> travellerTypes;

    @ManyToMany(mappedBy = "profiles")
    @JoinTable(
            name = "Nationality",
            joinColumns=@JoinColumn(name="user_id", referencedColumnName="user_id"),
            inverseJoinColumns=@JoinColumn(name="country_id", referencedColumnName="id"))
    public List<CountryDefinition> nationalities;

    @ManyToMany(mappedBy = "profiles")
    @JoinTable(
            name = "Passport",
            joinColumns=@JoinColumn(name="user_id", referencedColumnName="user_id"),
            inverseJoinColumns=@JoinColumn(name="country_id", referencedColumnName="id"))
    public List<CountryDefinition> passports;

    public int calculateAge() {
        LocalDate birthDate = LocalDate
            .parse(dateOfBirth, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        return age;
    }

    public List<CountryDefinition> getNationalities() {
        return nationalities;
    }
}