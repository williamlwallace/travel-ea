package models.dbOnly;

import io.ebean.Model;
import play.data.validation.Constraints;

import javax.persistence.*;

/**
 * User entity managed by Ebean
 */
@Entity
@Table(name="User")
public class User extends Model {

    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;

    @Constraints.Required
    @Constraints.Email
    @Column(unique = true)
    public String username;

    @Constraints.Required
    public String password;

    public String salt;

}