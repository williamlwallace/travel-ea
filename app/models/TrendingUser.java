package models;

import io.ebean.Model;
import java.lang.Integer;
import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import play.data.validation.Constraints;
import javax.persistence.Table;


/**
 * TrendingUser entity managed by Ebean.
 */
@Entity
@Table(name = "FollowerUser")
public class TrendingUser extends Model {

    @ManyToOne
    @JoinColumn(name = "user_id")
    public Profile user;
    
}