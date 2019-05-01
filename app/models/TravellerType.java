package models;

import io.ebean.Model;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import play.data.validation.Constraints;

/**
 * A class that stores information regarding users and which country passports they hold.
 */
@Entity
@Table(name = "TravellerType")
public class TravellerType extends Model {

    @Id
    public Long guid;

    @Constraints.Required
    @Column(name = "user_id")
    public Long userId;

    @ManyToOne
    @Constraints.Required
    public TravellerTypeDefinition travellerTypeDefinition;

}