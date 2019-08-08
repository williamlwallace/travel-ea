package models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
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

//    @ManyToMany(mappedBy = "users")
//    @JoinTable(
//        name = "UsedTag",
//        joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
//        inverseJoinColumns = @JoinColumn(name = "tag_id", referencedColumnName = "id"))
//    public List<Tag> usedTags;

    @OneToMany(mappedBy = "user")
    public List<UsedTag> usedTags;



    /**
     * Updates this users tags, updating the date of the tag or inserting a new tag. Compares the
     * original object to the new object to find newly added tags.
     *
     * Use this method signature when the user is updating the object.
     *
     * @param oldObject The original tagged object before this user's changes
     * @param newObject The new tagged object after this user's changes
     */
    public void updateUserTags(Taggable oldObject, Taggable newObject) {
        Set<Long> oldTagIds = oldObject.getTagsList().stream().map(tag -> tag.id)
            .collect(Collectors.toSet());
        Set<Long> newTagIds = newObject.getTagsList().stream().map(tag -> tag.id)
            .collect(Collectors.toSet());

        newTagIds.removeAll(oldTagIds);

//        for (Tag tag : usedTags) {
//            if (newTagIds.contains(tag.id)) {
//
//            }
//        }


    }

    /**
     * Updates this users tags, updating the date of the tag or inserting a new tag.
     *
     * Use this method signature when a user is creating the object.
     *
     * @param object The tagged object
     */
    public void updateUserTags(Taggable object) {

    }
}