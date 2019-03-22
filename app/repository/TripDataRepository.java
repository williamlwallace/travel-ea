package repository;

import io.ebean.*;
import models.Trip;
import models.TripData;
import play.db.ebean.EbeanConfig;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static play.mvc.Results.ok;
import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * A repository that executes database operations on the TripData table
 * in a different execution context.
 */
public class TripDataRepository {

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public TripDataRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    /**
     * Adds a new trip location and position to the database
     * @param newTripData tripData object to be stored
     * @return Ok on success
     */
    public CompletableFuture<Result> insertTripData(TripData newTripData) {
        return supplyAsync(() -> {
            ebeanServer.insert(newTripData);
            return ok();
        }, executionContext);
    }

    /**
     * Adds multiple trip data values simultaneously
     * @param tripDataList
     * @return
     */
    public CompletableFuture<Long> insertTripDataList(List<TripData> tripDataList, Long tripId) {
        return supplyAsync(() -> {
            ebeanServer.insertAll(tripDataList);
            return tripId;
        }, executionContext);
    }

    /**
     * Deletes a trip location and position from the database
     * @param tripData TripData object to be deleted
     * @return True if TripData was deleted, false if object not found
     */
    public CompletableFuture<Boolean> deleteTripData(TripData tripData) {
        return supplyAsync(() ->
                        ebeanServer.delete(tripData),
                executionContext);
    }

    public CompletableFuture<Integer> deleteAllTripData(Long tripId) {
        return supplyAsync(() ->
                        ebeanServer.find(TripData.class)
                                .where()
                                .eq("tripId", tripId)
                                .delete()
                , executionContext);
    }

    /**
     * Finds all entries in TripData table related to given tripID
     * @param tripID Trip ID of objects to be returned
     * @return List of TripData objects with specified trip id
     */
    public CompletableFuture<List<TripData>> getAllTripData(Long tripID) {
        return supplyAsync(() ->
                        ebeanServer.find(TripData.class)
                                .where()
                                .eq("tripId", tripID)
                                .findList()

                , executionContext);
    }

    /**
     * Updates a profile on the database, primary key must be the same
     * @param tripData TripData object with updated fields
     * @return Ok on success
     */
    public CompletableFuture<Result> updateTripData(TripData tripData) {
        return supplyAsync(() -> {
            ebeanServer.update(tripData);
            return ok();
        }, executionContext);
    }

    /**
     * Updates a list of trip data points
     * @param tripDataList
     * @return
     */
    public CompletableFuture<Result> updateTripDataList(Collection<TripData> tripDataList) {
        return supplyAsync(() -> {
            ebeanServer.updateAll(tripDataList);
            return ok();
        }, executionContext);
    }
}