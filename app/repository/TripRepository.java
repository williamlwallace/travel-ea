package repository;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import models.Trip;
import models.TripData;
import play.db.ebean.EbeanConfig;

/**
 * A repository which executes operations of the Trip database table.
 */
public class TripRepository {

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
     * @param newTrip Trip object to be added
     * @return Ok on success
     */
    public CompletableFuture<Long> insertTrip(Trip newTrip) {
        return supplyAsync(() -> {
            ebeanServer.insert(newTrip);
            return newTrip.id;
        }, executionContext);
    }

    /**
     * Updates a trip.
     *
     * @param trip the updated trip
     * @return true on successful update or false on fail
     */
    public CompletableFuture<Boolean> updateTrip(Trip trip) {
        return supplyAsync(() -> {
            try {
                ebeanServer.update(trip);
                return true;
            } catch (EntityNotFoundException ex) {
                return false;
            }
        }, executionContext);
    }

    /**
     * Deletes trip from database.
     *
     * @param trip Trip object to be deleted
     * @return True if trip object deleted, false if object not found
     */
    public CompletableFuture<Boolean> deleteTrip(Trip trip) {
        return supplyAsync(() ->
                ebeanServer.delete(trip),
            executionContext);
    }

    /**
     * Deletes trip from database by id.
     *
     * @param id ID of trip object to be deleted
     * @return True if trip object deleted, false if object not found
     */
    public CompletableFuture<Integer> deleteTrip(Long id) {
        return supplyAsync(() -> {
            ebeanServer.find(TripData.class)
                .where()
                .eq("trip_id", id)
                .delete();
            return ebeanServer.find(Trip.class)
                .where()
                .eq("id", id)
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
     * Finds all trips in database.
     *
     * @return List of all Trip objects
     */
    public CompletableFuture<List<Trip>> getAllTrips() {
        return supplyAsync(() ->
                ebeanServer.find(Trip.class)
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
}