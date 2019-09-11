package models;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.ebean.annotation.Aggregation;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
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

    @JsonInclude()
    @Aggregation("count(*)")
    public Long followingUsersCount;

    @JsonInclude()
    @Aggregation("count(*)")
    public Long followerUsersCount;

}