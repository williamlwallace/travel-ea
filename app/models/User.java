package models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    @OneToMany(mappedBy = "user")
    public Set<UsedTag> usedTags;

    @ManyToMany(mappedBy = "following")
    @JoinTable(
        name = "FollowerUser",
        joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "follower_id", referencedColumnName = "id"))
    public List<User> followers;

    @ManyToMany(mappedBy = "followers")
    @JoinTable(
        name = "FollowerUser",
        joinColumns = @JoinColumn(name = "follower_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
    public List<User> following;

    @ManyToMany(mappedBy = "followersDestination")
    @JoinTable(
        name = "FollowerDestination",
        joinColumns = @JoinColumn(name = "follower_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "destination_id", referencedColumnName = "id"))
    public List<Destination> followingDestinations;

    /**
     * Updates this users tags, updating the date of the tag or inserting a new tag. Compares the
     * original object to the new object to find newly added tags.
     *
     * Use this method signature when the user is updating the object.
     *
     * @param oldTaggable The original tagged object before this user's changes
     * @param newTaggable The new tagged object after this user's changes
     */
    public void updateUserTags(Taggable oldTaggable, Taggable newTaggable) {
        Set<Tag> oldTags = oldTaggable.getTagsList();
        Set<Tag> newTags = new HashSet<>(newTaggable.getTagsList());

        newTags.removeAll(oldTags);

        for (UsedTag usedTag : usedTags) {
            if (newTags.contains(usedTag.tag)) {
                usedTag.timeUsed = LocalDateTime.now();
                newTags.remove(usedTag.tag);
            }
        }

        for (Tag tag : newTags) {
            UsedTag newUsedTag = new UsedTag();
            newUsedTag.tag = tag;
            newUsedTag.user = this;

            usedTags.add(newUsedTag);
        }
    }

    /**
     * Updates this users tags, updating the date of the tag or inserting a new tag.
     *
     * Use this method signature when a user is creating the object.
     *
     * @param taggable The tagged object
     */
    public void updateUserTags(Taggable taggable) {
        Set<Tag> tags = taggable.getTagsList();

        for (UsedTag usedTag : usedTags) {
            if (tags.contains(usedTag.tag)) {
                usedTag.timeUsed = LocalDateTime.now();
                tags.remove(usedTag.tag);
            }
        }

        for (Tag tag : tags) {
            UsedTag newUsedTag = new UsedTag();
            newUsedTag.tag = tag;
            newUsedTag.user = this;

            usedTags.add(newUsedTag);
        }
    }
}