package models;

import io.ebean.Finder;
import io.ebean.Model;
import play.data.validation.Constraints;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * User entity managed by Ebean
 */
@Entity
@Table(name="User")
public class User extends Model {

    @Id
    public Long id;

    @Constraints.Required
    @Constraints.Email
    @Column(unique = true)
    public String username;

    @Constraints.Required
    public String password;

    public String salt;

    public Boolean admin = false;
}