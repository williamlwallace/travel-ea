package models.dbOnly;

import io.ebean.Model;
import play.data.validation.Constraints;

import javax.persistence.*;

/**
 * A class that stores information regarding users and which country passports they hold
 */
@Entity
@Table(name="TravellerType")
public class TravellerType extends Model {

    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long guid;

    @Constraints.Required
    @Column(name="user_id")
    public Long userId;

    @Constraints.Required
    @Column(name="traveller_type_id")
    public Long travellerTypeId;

}