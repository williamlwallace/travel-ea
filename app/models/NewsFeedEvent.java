package models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import io.ebean.Model;
import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import play.data.validation.Constraints;


/**
 * Event entity managed by Ebean.
 */
@Entity
@Table(name = "NewsFeedEvent")
public class NewsFeedEvent extends Model {

    @Id
    public Long guid;

    @Constraints.Required
    public String eventType;

    public Long userId;

    public Long destId;

    public Long refId;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime created;
}