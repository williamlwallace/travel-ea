package models;

import cucumber.api.java.it.Ma;
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

    @ManyToOne
    @Constraints.Required
    @Column(name = "user_id")
    public Long userId;

    @ManyToOne
    @Constraints.Required
    @Column(name = "follower_id")
    public Long followerId;

}