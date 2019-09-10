package models.strategies;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.concurrent.CompletableFuture;

public abstract class NewsFeedStrategy {

    /**
     * The method that handles executing whatever relevant code for any news feed strategy
     * @return JSON node containing data that will be sent to front end
     */
    public abstract CompletableFuture<JsonNode> execute();

}
