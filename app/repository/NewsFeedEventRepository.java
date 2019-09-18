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
import java.time.LocalDateTime;
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
            newsFeedEvent.created = LocalDateTime.now();
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
     * Gets all the events in a pgaed fashion. Can filter by user or destination ids.
     * 
     * @param userIds list of users to filter by, null for no filtering
     * @param destIds List of destinations to filter by, null for no filtering
     * @param pageNum page number
     * @param pageSize length of page
     * @return List of Events
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
                        (userIds != null && destIds == null) ? SQL_FALSE : SQL_TRUE
                    ).endOr()
                    // Filter nationalities by given traveller type ids, only if some were given
                    .or(
                        Expr.in("t0.dest_id", destIdsNotNull),
                        (destIds != null && userIds == null) ? SQL_FALSE : SQL_TRUE
                    ).endOr()
                    .or()
                        .in("t0.dest_id", destIdsNotNull)
                        .in("t0.user_id", userIdsNotNull)
                        .raw((destIds != null && userIds != null) ? "false" : "true")
                    .endOr();
                    
            // Order by specified column and asc/desc if given, otherwise default to most recently created profiles first
            PagedList<NewsFeedEvent> events = eventsExprList.orderBy("created desc")
            .setFirstRow((pageNum - 1) * pageSize)
            .setMaxRows(pageSize)
            .findPagedList();

            return events;
        });
    }

    /**
     * Gets the Likes object from the database where the two given ids match the relevant columns
     *
     * @param eventId the id of the news feed event
     * @param userId the id of the user likes
     * @return the Likes object if it exists, null otherwise
     */
    public CompletableFuture<Likes> getLikes(Long eventId, Long userId) {
        return supplyAsync(() ->
                ebeanServer.find(Likes.class)
                        .where().and(
                        Expr.eq("event_id", eventId),
                        Expr.eq("user_id", userId))
                        .findOneOrEmpty()
                        .orElse(null));
    }

    /**
     * Inserts a news feed event - user id pair
     *
     * @param like the Likes object to add
     * @return the guid of the inserted Likes object
     */
    public CompletableFuture<Long> insertLike(Likes like) {
        return supplyAsync(() -> {
            ebeanServer.insert(like);
            return like.guid;
        }, executionContext);
    }

    /**
     * Deletes a news feed event - user id pair
     *
     * @param id guid of the Like to delete
     * @return the number of rows that were deleted
     */
    public CompletableFuture<Long> deleteLike(Long id) {
        return supplyAsync(() ->
                        Long.valueOf(ebeanServer.delete(Likes.class, id))
                , executionContext);
    }

    /**
     * Retrieves the number of likes a news feed event has
     *
     * @param eventId ID of the news feed event to retrieve likes count for
     * @return A news feed event object with only the like count field populated
     */
    public CompletableFuture<Long> getEventLikeCounts(Long eventId) {

        return supplyAsync(() -> {
            return (long) ebeanServer.find(Likes.class)
                .where()
                .eq("event_id", eventId)
                .findCount();

        });
    }



}

