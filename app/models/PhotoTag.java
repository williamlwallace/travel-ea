package models;

import io.ebean.Model;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import play.data.validation.Constraints;

/**
 * A class that stores information regarding tags and what photos are tagged with them.
 *
 * This class is here for clarity, it can be deleted due to our bridging method
 */
@Entity
@Table(name = "PhotoTag")
public class PhotoTag extends Model {

    @Id
    public Long guid;

    @Constraints.Required
    @Column(name = "photo_id")
    public Long photoId;

    @ManyToOne
    @Constraints.Required
    public Tag tag;
}
