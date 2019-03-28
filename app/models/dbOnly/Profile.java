package models.dbOnly;

import io.ebean.Model;
import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * A class that represents a profile and hold information that is received from the database
 */
@Entity
@Table(name="Profile")
public class Profile extends Model {

    /**
     * Constructor to create a db object version of profile, from a main object profile
     * @param profile Main object version of profile
     */
    public Profile(models.Profile profile) {
        this.userId = profile.userId;
        this.firstName = profile.firstName;
        this.lastName = profile.lastName;
        this.middleName = profile.middleName;
        this.dateOfBirth = profile.dateOfBirth;
        this.gender = profile.gender;
    }

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

}
