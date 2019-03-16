package models;

import io.ebean.Model;
import play.data.validation.Constraints;
import javax.persistence.Column;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
}