package models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import play.data.validation.Constraints;


/**
 * Event entity managed by Ebean.
 */
@Entity
@Table(name = "NewsFeedEvent")
public class NewsFeedEvent extends BaseModel {

    @Id
    public Long guid;

    @Constraints.Required
    public String type;

    public Long userId;

    public Long destId;

    @Constraints.Required
    public Long refId;

    @Constraints.Required
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime time;
}