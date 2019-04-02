package models;

import io.ebean.Model;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import play.data.validation.Constraints;

/**
 * A class that represents a user's nationalities
 */
@Entity
@Table(name = "Nationality")
public class Nationality extends Model {

    @Id
    public Long guid;

    @Constraints.Required
    public Long countryId;

    @Constraints.Required
    public Long userId;

}