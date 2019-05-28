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
@Table(name = "DestinationTravellerType")
public class DestinationTravellerType extends Model {

    @Id
    public Long guid;

    @Constraints.Required
    @Column(name = "dest_id")
    public Long destId;

    @ManyToOne
    @Constraints.Required
    public TravellerTypeDefinition travellerTypeDefinition;
}