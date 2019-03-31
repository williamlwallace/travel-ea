package models;

import io.ebean.Finder;
import io.ebean.Model;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import play.data.validation.Constraints;

/**
 * User entity managed by Ebean
 */
@Entity
@Table(name = "User")
public class User extends Model {

    public static final Finder<Long, User> find = new Finder<>(User.class);
    @Id
    public Long id;
    @Constraints.Required
    @Constraints.Email
    @Column(unique = true)
    public String username;
    @Constraints.Required
    public String password;
    public String salt;

    // public String authToken;
    public Boolean admin = false;
}