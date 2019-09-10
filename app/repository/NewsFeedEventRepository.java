package repository;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static play.mvc.Http.Status.OK;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Expr;
import io.ebean.Expression;
import io.ebean.PagedList;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;

import models.*;
import org.apache.commons.text.similarity.LevenshteinDistance;
import play.db.ebean.EbeanConfig;
import play.mvc.Http;
import play.mvc.Result;

/**
 * A repository that executes database operations for the News feed events table.
 */
@Singleton
public class NewsFeedEventRepository {

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public NewsFeedEventRepository(EbeanConfig ebeanConfig,
        DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    /**
     * Adds a new News feed event to the database.
     *
     * @param newsFeedEvent the new newsFeedEvent to add
     * @return A CompletableFuture with the new event's id
     */
    public CompletableFuture<Long> addNewsFeedEvent(NewsFeedEvent newsFeedEvent) {
        return supplyAsync(() -> {
            ebeanServer.insert(newsFeedEvent);
            return newsFeedEvent.guid;
        }, executionContext);
    }

    /**
     * Gets a single event given the event ID.
     *
     * @param id Unique event ID of the requested event
     * @return A single event with the requested ID, or null if none was found
     */
    public CompletableFuture<NewsFeedEvent> getEvent(Long id) {
        return supplyAsync(() -> ebeanServer.find(NewsFeedEvent.class)
            .where()
            .idEq(id)
            .findOneOrEmpty()
            .orElse(null), executionContext);
    }
}

