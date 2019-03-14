package models;

import io.ebean.Finder;
import io.ebean.Model;
import play.data.validation.Constraints;
import play.data.format.*;
import javax.persistence.Column;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * A class that represents a profile and hold information that is received from the database
 */
@Entity
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

}