package repository;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Expr;
import io.ebean.Expression;
import io.ebean.PagedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityNotFoundException;
import models.Trip;
import models.TripData;
import models.TripTag;
import play.db.ebean.EbeanConfig;

/**
 * A repository which executes operations of the Trip database table.
 */
@Singleton
public class TripRepository {

    private final Expression SQL_FALSE = Expr.raw("false");
    private final Expression SQL_TRUE = Expr.raw("true");

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;


    @Inject
    public TripRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    /**
     * Inserts new trip into database.
     *
     * @param trip Trip object to be added
     * @return the id of the inserted trip
     */
    public CompletableFuture<Long> insertTrip(Trip trip) {
        return supplyAsync(() -> {
            trip.deleted = false;
            ebeanServer.insert(trip);
            return trip.id;
        }, executionContext);
    }

    /**
     * Updates a trip.
     *
     * @param trip the updated trip
     * @return true on successful update or false if the trip is not found in the database
     */
    public CompletableFuture<Boolean> updateTrip(Trip trip) {
        return supplyAsync(() -> {
            try {
                if (trip.tags.isEmpty()) {
                    ebeanServer.find(TripTag.class).where().eq("trip_id", trip.id).delete();
                }
                ebeanServer.update(trip);
                return true;
            } catch (EntityNotFoundException ex) {
                return false;
            }
        }, executionContext);
    }

    /**
     * Deletes trip from database by id.
     *
     * @param id ID of trip object to be deleted
     * @return the number of deleted rows
     */
    public CompletableFuture<Integer> deleteTrip(Long id) {
        return supplyAsync(() -> {
            ebeanServer.find(TripData.class)
                .where()
                .eq("trip_id", id)
                .delete();
            return ebeanServer.find(Trip.class)
                .where()
                .idEq(id)
                .delete();
        }, executionContext);
    }

    /**
     * Finds all trips in database related to the given user ID.
     *
     * @param userID User to find all trips for
     * @return List of Trip objects with the specified user ID
     */
    public CompletableFuture<List<Trip>> getAllUserTrips(long userID) {
        return supplyAsync(() ->
                ebeanServer.find(Trip.class)
                    .where()
                    .eq("user_id", userID)
                    .findList()
            , executionContext);
    }

    /**
     * Finds all trips in database with given paramters
     *
     * @return PagedList of Trip objects with the specified user ID
     */
    public CompletableFuture<PagedList<Trip>> searchTrips(Long userId,
        Long loggedInUserId,
        String searchQuery,
        Boolean ascending,
        Integer pageNum,
        Integer pageSize,
        Boolean getPrivate,
        Boolean getAll) {

        final String cleanedSearchQuery = (searchQuery == null ? "" : searchQuery)
            .replaceAll(" ", "").toLowerCase();

        return supplyAsync(() ->
            ebeanServer.find(Trip.class)
                .fetch("tripDataList.destination")
                .where()
                // Search where name fits search query
                .or(
                    Expr.eq("t0.user_id", userId),
                    getAll ? SQL_TRUE : SQL_FALSE
                ).endOr()
                // Only get public results, unless getPrivate is true, or we are getting all in which case return the logged in users private trips as well
                .or(
                    Expr.eq("t0.is_public", true),
                    Expr.or(
                        getPrivate ? SQL_TRUE : SQL_FALSE,
                        getAll ? Expr.eq("t0.user_id", loggedInUserId) : SQL_FALSE
                    )
                ).endOr()
                .ilike("tripDataList.destination.name", "%" + cleanedSearchQuery + "%")
                // Order by specified column and asc/desc if given, otherwise default to most recently created profiles first
                .orderBy("creation_date " + (ascending ? "asc" : "desc") + ", t0.id " + (ascending
                    ? "asc" : "desc"))
                .setFirstRow((pageNum - 1) * pageSize)
                .setMaxRows(pageSize)
                .findPagedList()
        );
    }

    /**
     * Finds all trips in database related to the given user ID which are public.
     *
     * @param userID User to find all trips for
     * @return List of Trip objects with the specified user ID
     */
    public CompletableFuture<List<Trip>> getAllPublicUserTrips(long userID) {
        return supplyAsync(() ->
                ebeanServer.find(Trip.class)
                    .where()
                    .eq("user_id", userID)
                    .eq("is_public", 1)
                    .findList()
            , executionContext);
    }

    /**
     * Returns a single trip as specified by its ID.
     *
     * @param tripId ID of trip to return
     * @return Trip having given id, null if no such trip found
     */
    public CompletableFuture<Trip> getTripById(long tripId) {
        return supplyAsync(() ->
                ebeanServer.find(Trip.class)
                    .where()
                    .eq("id", tripId)
                    .findOneOrEmpty()
                    .orElse(null)
            , executionContext);
    }

    /**
     * Returns a single trip as specified by its ID including delete trips
     *
     * @param tripId ID of trip to return
     * @return Trip having given id, null if no such trip found
     */
    public CompletableFuture<Trip> getDeletedTrip(Long tripId) {
        return supplyAsync(() -> ebeanServer.find(Trip.class)
            .setIncludeSoftDeletes()
            .where()
            .idEq(tripId)
            .findOneOrEmpty()
            .orElse(null), executionContext);
    }

    /**
     * Copies a trip and changes the user who owns the new trip
     *
     * @param trip The trip to copy
     * @param newUserId The user to own the copied trip
     * @return The id of the trip
     */
    public CompletableFuture<Long> copyTrip(Trip trip, Long newUserId) {
        trip.userId = newUserId;
        trip.id = null;

        for (TripData tripData : trip.tripDataList) {
            tripData.guid = null;
        }

        return insertTrip(trip);
    }
}