package models;

import io.ebean.Model;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.util.List;

/**
 * A class that stores information regarding users and which country passports they hold
 */
@Entity
@Table(name="Passport")
public class Passport extends Model {
    @Id
    public Long guid;

    @Constraints.Required
    public Long userId;

    @Constraints.Required
    public Long countryId;

//    @ManyToMany(mappedBy = "passports")
//    public List<Profile> passports;
}