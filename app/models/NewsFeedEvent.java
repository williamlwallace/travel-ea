package models;

import java.time.LocalDateTime;
import models.enums.NewsFeedEventType;

public class NewsFeedEvent {

    public String type;

    public Long guid;

    public Long userId;

    public Long destId;

    public Long refId;

    public LocalDateTime time;

}
