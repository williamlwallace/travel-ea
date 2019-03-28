package models.dbOnly;

import io.ebean.Model;
import play.data.validation.Constraints;

import javax.persistence.*;

/**
 * A class that stores information regarding users and which country passports they hold
 */
@Entity
@Table(name="Passport")
public class Passport extends Model {

    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long guid;

    @Constraints.Required
    public Long userId;

    @Constraints.Required
    public Long countryId;

}