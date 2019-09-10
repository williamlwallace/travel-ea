package repository;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static play.mvc.Http.Status.OK;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Expr;
import io.ebean.Expression;
import io.ebean.ExpressionList;
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

    private final Expression SQL_FALSE = Expr.raw("false");
    private final Expression SQL_TRUE = Expr.raw("true");

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

    /**
     * Gets all the profiles in the database except the given user id.
     *
     * @return A list of all profiles
     */
    public CompletableFuture<PagedList<NewsFeedEvent>> getPagedEvents(List<Long> userIds, // Possibly null
        List<Long> destIds,
        Integer pageNum,
        Integer pageSize) {

        // Below we make items for each value that we know aren't null, so that EBean won't throw NullPointer,
        // but we must check against the original parameter when checking if the variable was null initially
        List<Long> userIdsNotNull = userIds == null ? new ArrayList<>() : userIds;
        List<Long> destIdsNotNull = destIds == null ? new ArrayList<>() : destIds;

        return supplyAsync(() -> {
            ExpressionList<NewsFeedEvent> eventsExprList =
                ebeanServer.find(NewsFeedEvent.class)
                    .where()
                    .or(
                        Expr.in("t0.user_id", userIdsNotNull),
                        (userIds == null) ? SQL_TRUE : SQL_FALSE
                    ).endOr()
                    // Filter nationalities by given traveller type ids, only if some were given
                    .or(
                        Expr.in("t0.dest_id", destIdsNotNull),
                        (destIds == null) ? SQL_TRUE : SQL_FALSE
                    ).endOr();

            // Order by specified column and asc/desc if given, otherwise default to most recently created profiles first
            PagedList<NewsFeedEvent> events = eventsExprList.orderBy("time desc")
            .setFirstRow((pageNum - 1) * pageSize)
            .setMaxRows(pageSize)
            .findPagedList();

            return events;
        });
    }

    
}

