package repository;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;

import models.Profile;
import models.User;
import play.db.ebean.EbeanConfig;

@Singleton
public class ProfileRepository {

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public ProfileRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    /**
     * Adds a new profile to the database.
     *
     * This method should be simpler, but at the moment it can't be. Because play is brain-dead, and
     * is broken. We have working bridging code, that play fails on, except when we run app as
     * production.
     *
     * @param profile Profile to add
     * @return Ok on success
     */
    public CompletableFuture<Long> addProfile(Profile profile) {
        return supplyAsync(() -> {
            ebeanServer.insert(profile);
            return profile.userId;
        }, executionContext);
    }

    /**
     * Gets the profile with some id from the database, or null if no such profile exists.
     *
     * @param id Unique ID of profile (owning user's id) to retrieve
     * @return Profile object with given ID, or null if none found
     */
    public CompletableFuture<Profile> findID(Long id) {
        return supplyAsync(() -> {
            List<Profile> profiles = ebeanServer.find(Profile.class).findList();
                for (Profile profile : profiles) {
                    System.out.println(profile.userId);
                    System.out.println(profile.firstName);
                }

                return ebeanServer.find(Profile.class)
                    .where()
                    .eq("user_id", id)
                    .findOneOrEmpty()
                    .orElse(null); },
            executionContext);
    }

    /**
     * Updates a profile on the database, ID must not have been changed though.
     *
     * @param profile New profile object
     * @return OK on success
     */
    public CompletableFuture<Long> updateProfile(Profile profile) {
        return supplyAsync(() -> {
            ebeanServer.update(profile);
            return profile.userId;
        }, executionContext);
    }

    /**
     * Gets all the profiles in the database.
     *
     * @return A list of all profiles
     */
    public CompletableFuture<List<Profile>> getAllProfiles() {
        return supplyAsync(() -> {
            ArrayList<Profile> profiles = new ArrayList<>(
                ebeanServer.find(Profile.class).findList());
            // Manually change bean lists to array lists, as this was causing an issue on front end
            for (Profile profile : profiles) {
                profile.travellerTypes = new ArrayList<>(profile.travellerTypes);
                profile.nationalities = new ArrayList<>(profile.nationalities);
                profile.passports = new ArrayList<>(profile.passports);
            }
            return profiles;
        });
    }

    /**
     * Gets all the profiles in the database except the given user id
     *
     * @return A list of all profiles
     */
    public CompletableFuture<List<Profile>> getAllProfiles(Long userId) {
        return supplyAsync(() -> {
            ArrayList<Profile> profiles = new ArrayList<>(
                ebeanServer.find(Profile.class).where()
                    .ne("user_id", String.valueOf(userId))
                    .findList());
            // Manually change bean lists to array lists, as this was causing an issue on front end
            for (Profile profile : profiles) {
                profile.travellerTypes = new ArrayList<>(profile.travellerTypes);
                profile.nationalities = new ArrayList<>(profile.nationalities);
                profile.passports = new ArrayList<>(profile.passports);
            }
            return profiles;
        });
    }
}