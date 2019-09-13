package models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import io.ebean.Model;
import java.time.LocalDateTime;
import javax.persistence.MappedSuperclass;
import play.data.validation.Constraints;

@MappedSuperclass
public abstract class BaseNewsFeedEvent extends Model {

    @Constraints.Required
    public String eventType;

    public Long userId;

    public Long destId;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime created;
}
