package models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import io.ebean.Model;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.joda.time.DateTime;

/**
 * A class that models the Photo database table
 */
@Table(name = "Photo")
@Entity
public class Photo extends Model {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long guid;

    public Long userId;

    public String filename;

    public String thumbnailFilename;

    public Boolean isPublic;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public DateTime uploaded;

    public Boolean isProfile;
}
