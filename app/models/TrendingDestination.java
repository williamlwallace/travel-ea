package models;

import io.ebean.Model;
import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import play.data.validation.Constraints;
import javax.persistence.Table;


/**
 * TrendingDestination entity managed by Ebean.
 */
@Entity
@Table(name = "FollowerDestination")
public class TrendingDestination extends Model {

    @ManyToOne
    @JoinColumn(name = "destination_id")
    public Destination destination;

}