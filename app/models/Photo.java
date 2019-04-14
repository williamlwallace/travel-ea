package models;

import io.ebean.Model;
import play.data.validation.Constraints;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * A class that models the photo database table
 */
@Entity
@Table(name = "Photo")
public class Photo extends Model {

    @Id
    public Long photoId;

    @Constraints.Required
    @Column(name = "user_id")
    public Long userId;

    @Constraints.Required
    public String fileName;

    public Long publicPhoto;

    public Long profilePhoto;
}
