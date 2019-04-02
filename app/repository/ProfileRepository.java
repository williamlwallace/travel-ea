package repository;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static play.mvc.Results.ok;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Query;
import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;
import io.ebean.SqlUpdate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import models.CountryDefinition;
import models.Nationality;
import models.Passport;
import models.Profile;
import models.TravellerType;
import models.TravellerTypeDefinition;
import play.db.ebean.EbeanConfig;
import play.libs.Json;
import play.mvc.Result;

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
     *
     * This method should be simpler, but at the moment it can't be. Because play is brain-dead, and
     * is broken. We have working bridging code, that play fails on, except when we run app as
     * production.
     *
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
     *
     * @param id Unique ID of profile (owning user's id) to retrieve
     * @return Profile object with given ID, or null if none found
     */
    public CompletableFuture<Profile> findID(Long id) {
        return supplyAsync(() ->
            ebeanServer.find(Profile.class)
                    .where()
                    .eq("user_id", id)
                    .findOneOrEmpty()
                    .orElse(null),
                executionContext);
    }

    /**
     * Deletes the profile having some ID if it exists, returns false if no profile with that id was
     * found
     *
     * @param id ID of profile to delete
     * @return True if a profile was found with the ID and then deleted
     */
    public CompletableFuture<Boolean> deleteProfile(Long id) {
        return supplyAsync(() ->
                ebeanServer.find(Profile.class)
                .where()
                .eq("user_id", id)
                .delete() > 0,
            executionContext);
    }

    /**
     * Updates a profile on the database, ID must not have been changed though
     *
     * @param profile New profile object
     * @return OK on success
     */
    public CompletableFuture<Result> updateProfile(Profile profile) {
        return supplyAsync(() -> {
            ebeanServer.update(profile);
            return ok();
        }, executionContext);
    }

    public CompletableFuture<List<Profile>> getAllProfiles() {
        return supplyAsync(() -> {
            ArrayList<Profile> profiles = new ArrayList<>(ebeanServer.find(Profile.class).findList());
            // Manually change bean lists to array lists, as this was causing an issue on front end
            for(Profile profile : profiles) {
                profile.travellerTypes = new ArrayList<>(profile.travellerTypes);
                profile.nationalities = new ArrayList<>(profile.nationalities);
                profile.passports = new ArrayList<>(profile.passports);
            }
            return profiles;
        });
    }
}