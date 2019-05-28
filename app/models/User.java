package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.ebean.Model;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import play.data.validation.Constraints;

/**
 * User entity managed by Ebean.
 */
@Entity
@Table(name = "User")
public class User extends Model {

    @Id
    public Long id;

    @Constraints.Required
    @Constraints.Email
    @Column(unique = true)
    public String username;

    @Constraints.Required
    @JsonIgnore
    public String password;

    @JsonIgnore
    public String salt;

    //    @JsonFormat(pattern = "YYYY-MM-dd HH:mm")
    @JsonIgnore
    public LocalDateTime creationDate;

    public Boolean admin = false;
}