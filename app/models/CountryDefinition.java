package models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.ebean.Model;

import javax.persistence.*;

import play.data.validation.Constraints;

import java.util.List;

/**
 * A class that represents a country and holds information received from the database
 */
@Entity
@Table(name = "CountryDefinition")
public class CountryDefinition extends Model {

    @Id
    public Long id;

    @Constraints.Required
    public String name;

    @Override
    public String toString() {
        return name;
//        return "CountryDefinition{" +
//                "id=" + id +
//                ", name='" + name + '\'' +
//                '}';
    }

    @JsonBackReference(value="passports-reference")
    @ManyToMany(mappedBy = "passports")
    @JoinTable(
            name = "Nationality",
            joinColumns=@JoinColumn(name="country_id", referencedColumnName="id"),
            inverseJoinColumns=@JoinColumn(name="user_id", referencedColumnName="user_id"))
    public List<Profile> nationalityProfiles;

    @JsonBackReference(value="nationalities-reference")
    @ManyToMany(mappedBy = "nationalities")
    @JoinTable(
            name = "Passport",
            joinColumns=@JoinColumn(name="country_id", referencedColumnName="id"),
            inverseJoinColumns=@JoinColumn(name="user_id", referencedColumnName="user_id"))
    public List<Profile> passportProfiles;
}
