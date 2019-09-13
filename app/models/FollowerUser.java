package models;

import javax.persistence.Entity;
import javax.persistence.Id;
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
    public Long userId;

    @Constraints.Required
    public Long followerId;

}