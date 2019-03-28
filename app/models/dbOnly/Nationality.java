package models.dbOnly;

import io.ebean.Model;
import play.data.validation.Constraints;

import javax.persistence.*;

/**
 * A class that represents a user's nationalities
 */
@Entity
@Table(name="Nationality")
public class Nationality extends Model {

    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long guid;

    @Constraints.Required
    public Long countryId;

    @Constraints.Required
    public Long userId;

}