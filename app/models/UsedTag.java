package models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import java.time.LocalDateTime;

public class UsedTag {

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime used;

}
