package repository;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityNotFoundException;
import models.Trip;
import models.TripData;
import play.db.ebean.EbeanConfig;

/**
 * A repository which executes operations of the Trip database table.
 */
@Singleton
public class TripRepository {

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;
    private final TagRepository tagRepository;


    @Inject
    public TripRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext,
        TagRepository tagRepository) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
        this.tagRepository = tagRepository;
    }

    /**
     * Inserts new trip into database.
     *
     * @param newTrip Trip object to be added
     * @return the id of the inserted trip
     */
    public CompletableFuture<Long> insertTrip(Trip newTrip) {
        return tagRepository.addTags(newTrip.tags).thenApplyAsync(addedTags -> {
            newTrip.deleted = false;
            ebeanServer.insert(newTrip);
            return newTrip.id;
        }, executionContext);
    }

    /**
     * Updates a trip.
     *
     * @param trip the updated trip
     * @return true on successful update or false if the trip is not found in the database
     */
    public CompletableFuture<Boolean> updateTrip(Trip trip) {
        return tagRepository.addTags(trip.tags).thenApplyAsync(addedTags -> {
            try {
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
     * Finds all trips in database related to the given user ID or public.
     *
     * @param userID User to find all trips for
     * @return List of Trip objects with the specified user ID
     */
    public CompletableFuture<List<Trip>> getAllPublicTrips(long userID) {
        return supplyAsync(() ->
                ebeanServer.find(Trip.class)
                    .where()
                    .or()
                    .eq("is_public", 1)
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
}