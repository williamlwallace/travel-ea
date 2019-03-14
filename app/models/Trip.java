package models;

import io.ebean.Model;
import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * A class that models the trip database table
 */
@Entity
public class Trip extends Model {

    @Id
    public Long id;

    @Constraints.Required
    public Long userId;
}
