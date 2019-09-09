package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import play.data.validation.Constraints;


/**
 * FollowedUser entity managed by Ebean.
 */
@Entity
@Table(name = "FollowerUser")
public class FollowerUser extends BaseModel {

    @Id
    public Long guid;

    @Constraints.Required
    @Column(name = "user_id")
    public Long userId;

    @ManyToOne
    @Constraints.Required
    public Long followerId;

}