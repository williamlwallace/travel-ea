package models;

import io.ebean.Model;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import play.data.validation.Constraints;

/**
 * A class that stores information regarding tags and when a user last used them.
 */
@Entity
@Table(name = "UsedTag")
public class UsedTag extends Model {

    @Id
    public Long guid;

    @Constraints.Required
    @Column(name = "user_id")
    public Long userId;

    @ManyToOne
    @Constraints.Required
    public Tag tag;
}
