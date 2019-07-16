package repository;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityNotFoundException;
import models.TreasureHunt;
import play.db.ebean.EbeanConfig;

/**
 * A repository that executes database operations on the TreasureHunt database table.
 */
@Singleton
public class TreasureHuntRepository {

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public TreasureHuntRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    /**
     * Inserts a TreasureHunt object into the database.
     *
     * @param treasureHunt Object to be inserted
     * @return ID of inserted object
     */
    public CompletableFuture<Long> addTreasureHunt(TreasureHunt treasureHunt) {
        return supplyAsync(() -> {
            ebeanServer.insert(treasureHunt);
            return treasureHunt.id;
        }, executionContext);
    }

    /**
     * Updates a TreasureHunt object in the database.
     *
     * @param treasureHunt Object to updated table with
     * @return True if object was updated successfully, false if object was not found
     */
    public CompletableFuture<Boolean> updateTreasureHunt(TreasureHunt treasureHunt) {
        return supplyAsync(() -> {
            try {
                ebeanServer.update(treasureHunt);
                return true;
            } catch (EntityNotFoundException ex) {
                return false;
            }
        }, executionContext);
    }

    /**
     * Deletes the TreasureHunt object with the specified ID from the database.
     *
     * @param id ID of object to delete
     * @return Number of objects deleted
     */
    public CompletableFuture<Integer> deleteTreasureHunt(Long id) {
        return supplyAsync(() -> ebeanServer.find(TreasureHunt.class)
                .where()
                .eq("id", id)
                .delete(),
            executionContext);
    }

    /**
     * Retrieves all TreasureHunt objects from the database.
     *
     * @return List of TreasureHunt objects
     */
    public CompletableFuture<List<TreasureHunt>> getAllTreasureHunts() {
        return supplyAsync(() ->
                ebeanServer.find(TreasureHunt.class)
                    .findList()
            , executionContext);
    }

    /**
     * Retrieves all TreasureHunt objects related to a given userId.
     *
     * @param userID User to find all hunts for
     * @return List if TreasureHunt objects with the specified user ID
     */
    public CompletableFuture<List<TreasureHunt>> getAllUserTreasureHunts(long userID) {
        return supplyAsync(() ->
            ebeanServer.find(TreasureHunt.class)
            .where()
            .eq("user_id", userID)
            .findList()
        , executionContext);
    }
}
