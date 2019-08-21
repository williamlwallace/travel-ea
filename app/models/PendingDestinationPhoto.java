package models;

import io.ebean.Model;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import play.data.validation.Constraints;

/**
 * A class that stores information regarding pending requests for a destination primary photo.
 */
@Entity
@Table(name = "PendingDestinationPhoto")
public class PendingDestinationPhoto extends Model {

    @Id
    public Long id;

    @Constraints.Required
    @Column(name = "dest_id")
    public Long destId;

    @ManyToOne
    @Constraints.Required
    public Photo photo;
}