package models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import play.data.validation.Constraints;


/**
 * User entity managed by Ebean.
 */
@Entity
@Table(name = "User")
public class User extends BaseModel {

    @Id
    public Long id;

    @Constraints.Required
    @Constraints.Email
    @Column(unique = true)
    public String username;

    @Constraints.Required
    @JsonBackReference("password-reference")
    public String password;

    @JsonBackReference("salt-reference")
    public String salt;

    //    @JsonFormat(pattern = "YYYY-MM-dd HH:mm")
    @JsonIgnore
    public LocalDateTime creationDate;

    public Boolean admin = false;

    @ManyToMany
    @JoinTable(
        name = "UsedTag",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id"))
    public List<Tag> usedTags;

}