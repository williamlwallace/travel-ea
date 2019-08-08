package repository;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;
import models.Photo;
import models.Profile;
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
        return supplyAsync(() ->
                ebeanServer.find(Profile.class)
                    .where()
                    .eq("user_id", id)
                    .findOneOrEmpty()
                    .orElse(null)
            , executionContext);
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
     * Gets all the profiles in the database except the given user id.
     *
     * @return A list of all profiles
     */
    public CompletableFuture<List<Profile>> getAllProfiles(Long userId) {
        return supplyAsync(() -> {
            ArrayList<Profile> profiles = new ArrayList<>(
                ebeanServer.find(Profile.class)
                    .where()
                    .ne("user_id", userId)
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

    /**
     * Updates the profile photo of some users profile, and returns the id that WAS being used
     *
     * @param userId ID of user to update profile picture of
     * @param newId New id of photo to set as profile
     * @return The id of the photo (possibly null) that was previously used
     */
    public CompletableFuture<Long> updateProfilePictureAndReturnExistingId(Long userId, Long newId) throws NullPointerException {
        return supplyAsync(() -> {
            // Find existing profile
            Profile found = ebeanServer.find(Profile.class)
                .where()
                .eq("user_id", userId)
                .findOne();
            if(found == null) {
                throw new NullPointerException("No such profile");
            }

            Photo foundPhoto = ebeanServer.find(Photo.class)
                .where()
                .eq("guid", newId)
                .findOne();
            if(foundPhoto != null) {
                // Update object and return
                foundPhoto.usedForProfile = true;
                foundPhoto.isPublic = true;
                ebeanServer.update(foundPhoto);
            }

            // Keep track of existing ID, and update profile photo to new id
            Long returnId = (found.profilePhoto != null) ? found.profilePhoto.guid : null;
            found.profilePhoto = new Photo();
            found.profilePhoto.guid = newId;
            ebeanServer.update(found);
            return returnId;
        });
    }

    /**
     * Updates the profile cover photo of some user's profile, and returns the id that WAS being used
     *
     * @param userId ID of user to update cover photo of
     * @param newId New id of photo to set as cover photo
     * @return The id of the photo (possibly null) that was previously used
     */
    public CompletableFuture<Long> updateCoverPhotoAndReturnExistingId(Long userId, Long newId) throws NullPointerException {
        return supplyAsync(() -> {
            // Find existing profile
            Profile found = ebeanServer.find(Profile.class)
                .where()
                .eq("user_id", userId)
                .findOne();
            if(found == null) {
                throw new NullPointerException("No such profile");
            }

            Photo foundPhoto = ebeanServer.find(Photo.class)
                .where()
                .eq("guid", newId)
                .findOne();
            if(foundPhoto != null) {
                // Update object and return
                foundPhoto.isPublic = true;
                ebeanServer.update(foundPhoto);
            }

            // Keep track of existing ID, and update profile photo to new id
            Long returnId = (found.coverPhoto != null) ? found.coverPhoto.guid : null;
            found.coverPhoto = new Photo();
            found.coverPhoto.guid = newId;
            ebeanServer.update(found);
            return returnId;
        });
    }
}