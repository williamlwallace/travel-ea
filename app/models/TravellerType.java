package models;

import io.ebean.Model;
import play.data.validation.Constraints;
import javax.persistence.Column;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * A class that stores information regarding users and which country passports they hold
 */
@Entity
public class TravellerType extends Model {

    @Id
    public Long guid;

    @Constraints.Required
    public Long userId;

    @Constraints.Required
    public Long countryId;
}