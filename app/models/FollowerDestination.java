package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import play.data.validation.Constraints;


/**
 * FollowerDestination entity managed by Ebean.
 */
@Entity
@Table(name = "FollowerDestination")
public class FollowerDestination extends BaseModel {

    @Id
    public Long guid;

    @Constraints.Required
    @Column(name = "destination_id")
    public Long destinationId;

    @ManyToOne
    @Constraints.Required
    public Long followerId;

}