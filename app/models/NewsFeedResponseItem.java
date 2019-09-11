package models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import java.time.LocalDateTime;

public class NewsFeedResponseItem {

    public String message;
    public String name;
    public String thumbnail;
    public Object data;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime created;

    public String eventType;

    /**
     * Constructor to initialize all fields
     * @param message Message to show to user
     * @param data Data involved with the event
     */
    public NewsFeedResponseItem(String message, String name, String thumbnail, Object data) {
        this.message = message;
        this.name = name;
        this.thumbnail = thumbnail;
        this.data = data;
    }
}
