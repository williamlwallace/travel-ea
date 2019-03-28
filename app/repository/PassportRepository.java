package repository;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import models.dbOnly.Passport;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class PassportRepository {

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public PassportRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    /**
     * Insert a new passport of a user
     * @param passport Passport to insert
     * @return True on success
     */
    public CompletableFuture<Boolean> insertPassport(Passport passport) {
        return supplyAsync(() -> {
            ebeanServer.insert(passport);
            return true;
        }, executionContext);
    }

    /**
     * Deletes a passport for a user
     * @param passport Passport to delete
     * @return True if a passport was found and deleted, false if no such passport found
     */
    public CompletableFuture<Boolean> deletePassport(Passport passport) {
        return supplyAsync(() ->
            ebeanServer.delete(passport)
            , executionContext);
    }

    /**
     * Gets all passports belonging to a user
     * @param userID User to get passports of
     * @return List (possibly empty) of passports of user
     */
    public CompletableFuture<List<Passport>> getAllPassportsOfUser(long userID) {
        return supplyAsync(() ->
            ebeanServer.find(Passport.class)
                    .where()
                    .eq("user_id", userID)
                    .findList()
            , executionContext);
    }
}
