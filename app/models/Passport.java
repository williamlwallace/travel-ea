package models;

import io.ebean.Model;
import play.data.validation.Constraints;
import javax.persistence.Column;

import javax.persistence.Entity;

/**
 * A class that stores information regarding users and which country passports they hold
 */
@Entity
public class Passport extends Model {

    @Constraints.Required
    public Long uid;

    @Column(name="countryId")
    @Constraints.Required
    public Long countryId;
}