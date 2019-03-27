package models;

import io.ebean.Model;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.util.List;

/**
 * A class that represents a country and holds information received from the database
 */
@Entity
@Table(name="CountryDefinition")
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

    //    @JsonBackReference
//    @ManyToMany(mappedBy = "nationalities")
//    public List<Profile> nationalities;
//
//    @JsonBackReference
//    @ManyToMany(mappedBy = "passports")
//    public List<Profile> passports;
}
