package models.strategies;

import java.util.concurrent.CompletableFuture;
import java.util.List;
import models.NewsFeedResponseItem;

public abstract class NewsFeedStrategy {

    protected List<Long> eventIds;

    /**
     * Constructor to instantiate both required fields
     * @param eventIds list of relevent eventIds
     */
    public NewsFeedStrategy(List<Long> eventIds) {
        this.eventIds = eventIds;
    }

    /**
     * The method that handles executing whatever relevant code for any news feed strategy
     * @return JSON node containing data that will be sent to front end
     */
    public abstract CompletableFuture<NewsFeedResponseItem> execute();

}
