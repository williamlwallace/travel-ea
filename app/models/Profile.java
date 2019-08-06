package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.ebean.Model;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.CascadeType;
import play.data.validation.Constraints;

/**
 * A class that represents a profile and hold information that is received from the database.
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

    @JsonIgnore
    public LocalDateTime creationDate;

    public String gender;

    @ManyToMany(mappedBy = "travellerTypes")
    @JoinTable(
        name = "TravellerType",
        joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "traveller_type_id", referencedColumnName = "id"))
    public List<TravellerTypeDefinition> travellerTypes;

    @ManyToMany(mappedBy = "nationalityProfiles")
    @JoinTable(
        name = "Nationality",
        joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "country_id", referencedColumnName = "id"))
    public List<CountryDefinition> nationalities;

    @ManyToMany(mappedBy = "passportProfiles")
    @JoinTable(
        name = "Passport",
        joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "country_id", referencedColumnName = "id"))
    public List<CountryDefinition> passports;

    @ManyToOne
    public Photo profilePhoto;

    @ManyToOne(cascade = CascadeType.ALL)
    public Photo coverPhoto;

    /**
     * Calculates age based on the birth date.
     *
     * @return age
     */
    public int calculateAge() {
        LocalDate birthDate = LocalDate
            .parse(dateOfBirth, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    /**
     * Creates a copy not refrenced
     *
     * @return copy of class Profile
     */
    public Profile copy() {
        Profile copy = new Profile();
        copy.userId = this.userId;
        copy.firstName = this.firstName;
        copy.lastName = this.lastName;
        copy.middleName = this.middleName;
        copy.dateOfBirth = this.dateOfBirth;
        copy.creationDate = this.creationDate;
        copy.gender = this.gender;
        copy.travellerTypes = new ArrayList<TravellerTypeDefinition>(this.travellerTypes);
        copy.nationalities = new ArrayList<CountryDefinition>(this.nationalities);
        copy.passports = new ArrayList<CountryDefinition>(this.passports);
        return copy;
    }
}