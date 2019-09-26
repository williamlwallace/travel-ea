package repository;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Expr;
import io.ebean.Expression;
import io.ebean.ExpressionList;
import io.ebean.PagedList;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import models.Destination;
import models.FollowerDestination;
import models.FollowerUser;
import models.Likes;
import models.NewsFeedEvent;
import models.Photo;
import models.Profile;
import models.TrendingDestination;
import models.TrendingUser;
import models.Trip;
import models.enums.NewsFeedEventType;
import play.db.ebean.EbeanConfig;

/**
 * A repository that executes database operations for the News feed events table.
 */
@Singleton
public class NewsFeedEventRepository {

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    private final Expression SQL_FALSE = Expr.raw("false");
    private final Expression SQL_TRUE = Expr.raw("true");

    private final Double TRENDING_TIME_TUNING = 3000.0; // The bigger this gets, the longer it takes for a post to fall off
    private final Double TRENDING_LIKE_TUNING = 0.5; // The bigger this gets, the less influence the amount of likes has
    private final Double E = 2.718281828459045;

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
     * For some photo, removes any event that were associated with it
     *
     * @param photo The photo to clean up events from
     * @return Number of events cleaned up (removed)
     */
    public CompletableFuture<Integer> cleanUpPhotoEvents(Photo photo) {
        Collection<String> relevantTypes = Arrays.asList(
            NewsFeedEventType.LINK_DESTINATION_PHOTO.name(),
            NewsFeedEventType.NEW_PROFILE_COVER_PHOTO.name(),
            NewsFeedEventType.NEW_PROFILE_PHOTO.name(),
            NewsFeedEventType.UPLOADED_USER_PHOTO.name(),
            NewsFeedEventType.NEW_PRIMARY_DESTINATION_PHOTO.name()
        );

        return supplyAsync(() ->
            ebeanServer.find(NewsFeedEvent.class)
                .where()
                .eq("ref_id", photo.guid)
                .in("event_type", relevantTypes)
                .delete()
        );
    }

    /**
     * Cleans up all the news feed events related to some destination
     *
     * @param destination The destination that has been deleted
     * @return Number of events cleaned up (removed)
     */
    public CompletableFuture<Integer> cleanUpDestinationEvents(Destination destination) {
        Collection<String> relevantTypes = Arrays.asList(
            NewsFeedEventType.LINK_DESTINATION_PHOTO.name(),
            NewsFeedEventType.MULTIPLE_DESTINATION_PHOTO_LINKS.name(),
            NewsFeedEventType.NEW_PRIMARY_DESTINATION_PHOTO.name(),
            NewsFeedEventType.CREATED_NEW_DESTINATION.name(),
            NewsFeedEventType.UPDATED_EXISTING_DESTINATION.name()
        );

        return supplyAsync(() -> {
            //this is not pretty but ebean queries to annoying to do in one.
            Integer rows = ebeanServer.find(NewsFeedEvent.class)
                .where()
                .eq("dest_id", destination.id)
                .in("event_type", relevantTypes)
                .delete();

            rows += ebeanServer.find(NewsFeedEvent.class)
                .where()
                .eq("ref_id", destination.id)
                .eq("event_type", NewsFeedEventType.CREATED_NEW_DESTINATION.name())
                .delete();
            return rows;
        });
    }

    /**
     * For some trip, removes any event that were associated with it
     *
     * @param trip The trip to clean up events from
     * @return Number of events cleaned up (removed)
     */
    public CompletableFuture<Integer> cleanUpTripEvents(Trip trip) {
        Collection<String> relevantTypes = Arrays.asList(
            NewsFeedEventType.CREATED_NEW_TRIP.name(),
            NewsFeedEventType.UPDATED_EXISTING_TRIP.name(),
            NewsFeedEventType.GROUPED_TRIP_UPDATES.name()
        );

        return supplyAsync(() ->
            ebeanServer.find(NewsFeedEvent.class)
                .where()
                .eq("ref_id", trip.id)
                .in("event_type", relevantTypes)
                .delete()
        );
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
    public CompletableFuture<PagedList<NewsFeedEvent>> getPagedEvents(List<Long> userIds,
        // Possibly null
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
     * Gets all the events in a pagaed fashion. TRENDING
     *
     * @param pageNum page number
     * @param pageSize length of page
     * @return List of Events
     */
    public CompletableFuture<PagedList<NewsFeedEvent>> getPagedTrendingEvents(
        Integer pageNum,
        Integer pageSize) {

        return supplyAsync(() -> {
            // ### Explore algorithm ###
            // (1 / (1+e^({changeInTime}/{time_tuner} -3 ))) * ({likes}/{likesTuner} + 1)

            String changeInTime = " (UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(created)) "; // Integer value of seconds
            String numberOfLikes = " (SELECT COUNT(*) FROM Likes L WHERE L.event_id = E.guid AND deleted = 0) "; // Integer value of likes

            String sql = "SELECT "
                + "*, "
                + "((1 / (1 + POWER(:E, ((" + changeInTime
                + " / CAST(:TRENDING_TIME_TUNING AS DECIMAL(12,8))) - 3)))) * ((" + numberOfLikes
                + " / CAST(:TRENDING_LIKE_TUNING AS DECIMAL(12,8)) ) + 1) ) AS weight "
                + "FROM NewsFeedEvent E "
                + "ORDER BY weight DESC";
            // Order by specified column and asc/desc if given, otherwise default to most recently created profiles first
            PagedList<NewsFeedEvent> events = ebeanServer.findNative(NewsFeedEvent.class, sql)
                .setParameter("E", this.E)
                .setParameter("TRENDING_TIME_TUNING", this.TRENDING_TIME_TUNING)
                .setParameter("TRENDING_LIKE_TUNING", this.TRENDING_LIKE_TUNING)
                .setFirstRow((pageNum - 1) * pageSize)
                .setMaxRows(pageSize)
                .findPagedList();

            ebeanServer.createSqlQuery(sql)
                .setParameter("E", this.E)
                .setParameter("TRENDING_TIME_TUNING", this.TRENDING_TIME_TUNING)
                .setParameter("TRENDING_LIKE_TUNING", this.TRENDING_LIKE_TUNING)
                .findEachRow(((resultSet, rowNum) -> {
                }));

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

    /**
     * Retrieves trending users based on the followers they have recieved in the last week
     *
     * @return A list of 5 trending users
     */
    public CompletableFuture<List<Profile>> getTrendingUsers() {
        return supplyAsync(() -> {
            String sql = "select user_id, COUNT(follower_id) as followerCount from FollowerUser where follow_time >= :time group by user_id order by followerCount desc, user_id desc limit 5";
            List<Profile> trending = ebeanServer.findNative(TrendingUser.class, sql)
                .setParameter("time", LocalDateTime.now().minusDays(7))
                .findList()
                .stream()
                .map(tu -> {
                    tu.user.followerUsersCount = Long.valueOf(ebeanServer.find(FollowerUser.class)
                        .where().eq("user_id", tu.user.userId)
                        .findCount());
                    return tu.user;
                })
                .collect(Collectors.toList());
            return trending;
        });
    }

    /**
     * Retrieves trending destinations based on the followers they have recieved in the last week
     *
     * @return A list of 5 trending destinations
     */
    public CompletableFuture<List<Destination>> getTrendingDestinations() {
        return supplyAsync(() -> {
            String sql = "select destination_id, COUNT(follower_id) as followerCount from FollowerDestination where follow_time >= :time group by destination_id order by followerCount desc, destination_id desc limit 5";
            List<Destination> trending = ebeanServer.findNative(TrendingDestination.class, sql)
                .setParameter("time", LocalDateTime.now().minusDays(7))
                .findList()
                .stream()
                .map(td -> {
                    td.destination.followerCount = Long
                        .valueOf(ebeanServer.find(FollowerDestination.class)
                            .where().eq("destination_id", td.destination.id)
                            .findCount());
                    return td.destination;
                })
                .collect(Collectors.toList());
            return trending;
        });
    }
}

