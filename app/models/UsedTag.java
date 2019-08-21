package models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import io.ebean.Model;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * A class that stores information regarding tags and when a user last used them.
 */
@Entity
@Table(name = "UsedTag")
public class UsedTag extends Model {

    @Id
    public Long guid;

    @JsonBackReference("UsedTagUserReference")
    @ManyToOne
    @JoinColumn(name = "user_id")
    public User user;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @Column(name = "time_used")
    public LocalDateTime timeUsed;

    @JsonBackReference("UsedTagTagReference")
    @ManyToOne
    @JoinColumn(name = "tag_id")
    public Tag tag;
}
