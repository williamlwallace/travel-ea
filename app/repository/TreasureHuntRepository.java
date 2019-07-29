package repository;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import java.util.List;
import java.util.Optional;
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
            treasureHunt.deleted = false;
            ebeanServer.insert(treasureHunt);
            return treasureHunt.id;
        }, executionContext);
    }

    /**
     * Returns an optional treasure hunt, i.e if one is found with given id it is returned,
     * otherwise optional will remain as none.
     * @param id ID of treasure hunt to search for
     * @return Optional treasureHunt, that is non-null if a treasure hunt with corresponding ID was found
     */
    public CompletableFuture<TreasureHunt> getTreasureHuntById(Long id) {
        return supplyAsync(() ->
            ebeanServer.find(TreasureHunt.class)
            .where()
            .eq("id", id)
            .findOneOrEmpty()
            .orElse(null)
            , executionContext);
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
    public CompletableFuture<List<TreasureHunt>> getAllUserTreasureHunts(Long userID) {
        return supplyAsync(() ->
            ebeanServer.find(TreasureHunt.class)
            .where()
            .eq("user_id", userID)
            .findList()
        , executionContext);
    }

    /**
     * Retrieves a treasure hunt object from the database even if it is soft deleted
     *
     * @param id ID of treasure hunt to retrieve
     * @return Treasure hunt object found or null
     */
    public CompletableFuture<TreasureHunt> getDeletedTreasureHunt(Long id) {
        return supplyAsync(() -> ebeanServer.find(TreasureHunt.class)
            .setIncludeSoftDeletes()
            .where()
            .idEq(id)
            .findOneOrEmpty()
            .orElse(null), executionContext);
    }
}
