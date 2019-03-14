package models;

import io.ebean.Model;
import play.data.validation.Constraints;
import javax.persistence.Column;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * A class that represents a user's nationalities
 */
@Entity
public class Nationality extends Model {

    @Id
    public Long guid;

    @Constraints.Required
    public Long userId;

    @Constraints.Required
    public Long countryId;
}