package repository;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import models.Profile;
import play.db.ebean.EbeanConfig;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static play.mvc.Results.ok;

public class ProfileRepository {

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public ProfileRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    /**
     * Adds a new profile to the database
     * @param profile Profile to add
     * @return Ok on success
     */
    public CompletableFuture<Result> addProfile(Profile profile) {
        return supplyAsync(() -> {
            ebeanServer.insert(profile);
            return ok();
        }, executionContext);
    }

    /**
     * Gets the profile with some id from the database, or null if no such profile exists
     * @param id Unique ID of profile (owning user's id) to retrieve
     * @return Profile object with given ID, or null if none found
     */
    public CompletableFuture<Profile> findID(Long id){
        return supplyAsync(() -> 
            ebeanServer.find(Profile.class)
                .where()
                .idEq(id)
                .findOneOrEmpty()
                .orElse(null),
            executionContext);
    }

    /**
     * Deletes the profile having some ID if it exists, returns false if no profile with that id was found
     * @param id ID of profile to delete
     * @return True if a profile was found with the ID and then deleted
     */
    public CompletableFuture<Boolean> deleteProfile(Long id) {
        return supplyAsync(() -> {
            int rows = ebeanServer.find(Profile.class)
                                    .where()
                                    .eq("uid",id)
                                    .delete();
            return rows > 0;
        }, executionContext);
    }

    /**
     * Updates a profile on the database, ID must not have been changed though
     * @param profile New profile object
     * @return OK on success
     */
    public CompletableFuture<Result> updateProfile(Profile profile) {
        return supplyAsync(() -> {
           ebeanServer.update(profile);
           return ok();
        }, executionContext);
    }
}