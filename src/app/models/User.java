package models;

import io.ebean.Finder;
import io.ebean.Model;
import play.data.validation.Constraints;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * User entity managed by Ebean
 */
@Entity 
public class User extends Model {

    @Id
    public Long uid;

    @Constraints.Required
    @Constraints.Email
    @Column(unique = true)
    public String username;

    @Constraints.Required
    public String password;

    public String salt;

    public static final Finder<Long, User> find = new Finder<>(User.class);
}