package repository;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import models.Nationality;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class NationalityRepository {

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public NationalityRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    /**
     * Insert a new nationality for some user to database
     * @param nationality Nationality to add
     * @return True if successfully added
     */
    public CompletableFuture<Boolean> insertNationality(Nationality nationality) {
        return supplyAsync(() -> {
            ebeanServer.insert(nationality);
            return true;
        }, executionContext);
    }

    /**
     * Deletes a specific nationality from the table. Both uid and country id must be filled in
     * @param nationality Nationality to delete
     * @return True if the nationality was deleted, false if it was not found
     */
    public CompletableFuture<Boolean> deleteNationality(Nationality nationality) {
        return supplyAsync(() ->
            ebeanServer.delete(nationality)
        , executionContext);
    }

    /**
     * Gets all nationalities belonging to a user
     * @param userID ID of user to get nationalities for
     * @return List of nationalities of user
     */
    public CompletableFuture<List<Nationality>> getAllNationalitiesOfUser(long userID) {
        return supplyAsync(() ->
            ebeanServer.find(Nationality.class)
                .where()
                .eq("user_id", userID)
                .findList()
        , executionContext);
    }
}
