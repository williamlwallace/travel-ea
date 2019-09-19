package models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * Event entity managed by Ebean.
 */
@Entity
@Table(name = "NewsFeedEvent")
public class NewsFeedEvent extends BaseNewsFeedEvent {

    @Id
    public Long guid;

    public Long refId;

}