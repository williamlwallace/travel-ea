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
@Table(name = "DestinationPhoto")
public class DestinationPhoto extends Model {

    @Id
    public Long guid;

    @Constraints.Required
    @Column(name = "photo_id")
    public Long photoId;

    @ManyToOne
    @Constraints.Required
    public Destination destination;

}