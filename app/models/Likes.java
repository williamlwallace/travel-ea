package models;

import play.data.validation.Constraints;
import javax.persistence.*;

/**
 * Likes entity managed by Ebean.
 */
@Entity
@Table(name = "Likes")
public class Likes extends BaseModel {

    @Id
    public Long guid;

    @Constraints.Required
    @Column(name = "event_id")
    public Long eventId;

    @ManyToOne
    @Constraints.Required
    @Column(name = "user_id")
    public Long userId;
}
