package repository;

import io.ebean.*;
import models.Trip;
import models.TripData;
import play.db.ebean.EbeanConfig;
import play.libs.Json;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static play.mvc.Results.ok;
import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * A repository which executes operations of the Trip database table
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
     * Inserts new trip into database
     * @param newTrip Trip object to be added
     * @return Ok on success
     */
    public CompletableFuture<Result> insertTrip(Trip newTrip) {
        return supplyAsync(() -> {
            ebeanServer.insert(newTrip);
            for(TripData data : newTrip.tripDataList) {
                data.trip = newTrip;
            }
            ebeanServer.insertAll(newTrip.tripDataList);
            return ok();
        }, executionContext);
    }

    /**
     * Deletes trip from database
     * @param trip Trip object to be deleted
     * @return True if trip object deleted, false if object not found
     */
    public CompletableFuture<Boolean> deleteTrip(Trip trip) {
        return supplyAsync(() ->
            ebeanServer.delete(trip),
            executionContext);
    }

    /**
     * Deletes trip from database by id
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
     * Finds all trips in database related to the given user ID
     * @param userID User to find all trips for
     * @return List of Trip objects with the specified user ID
     */
    public CompletableFuture<List<Trip>> getAllUserTrips(long userID) {
        return supplyAsync(() -> {
            List<Trip> list = ebeanServer.find(Trip.class)
                    .where()
                    .eq("user_id", userID)
                    .findList();

            for(Trip trip : list) {
                trip.tripDataList = ebeanServer.find(TripData.class)
                        .where()
                        .eq("trip_id", trip.id)
                        .findList();
                }
                return list;
            },
            executionContext);
    }

    /**
     * Returns a single trip as specified by its ID
     * @param tripId ID of trip to return
     * @return Trip having given id, null if no such trip found
     */
    public CompletableFuture<Trip> getTripById(long tripId) {
        return supplyAsync(() -> {
            Trip trip = ebeanServer.find(Trip.class)
                    .where()
                    .eq("id", tripId)
                    .findOneOrEmpty()
                    .orElse(null);
            if(trip != null) {
                trip.tripDataList = ebeanServer.find(TripData.class)
                    .where()
                    .eq("trip_id", trip.id)
                    .findList();
            }
            return trip;
        }, executionContext);
    }
}
