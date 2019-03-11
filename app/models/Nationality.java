package models;

import io.ebean.Model;
import play.data.validation.Constraints;
import javax.persistence.Column;

import javax.persistence.Entity;

/**
 * A class that represents a user's nationalities
 */
@Entity
public class Nationality extends Model {

    @Constraints.Required
    public Long uid;

    @Column(name="countryId")
    @Constraints.Required
    public Long countryId;
}