package repository;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Expr;
import io.ebean.Expression;
import io.ebean.ExpressionList;
import io.ebean.OrderBy;
import io.ebean.PagedList;
import io.ebean.Query;
import io.ebean.RawSqlBuilder;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;
import models.FollowerDestination;
import models.FollowerUser;
import models.Photo;
import models.Profile;
import play.db.ebean.EbeanConfig;

@Singleton
public class ProfileRepository {

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    private final Expression SQL_FALSE = Expr.raw("false");
    private final Expression SQL_TRUE = Expr.raw("true");

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
     * @return ID of inserted profile
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
     * @param userId Unique ID of profile (owning user's id) to retrieve
     * @return Profile object with given ID, or null if none found
     */
    public CompletableFuture<Profile> findID(Long userId) {
        return supplyAsync(() ->
                ebeanServer.find(Profile.class)
                    .where()
                    .idEq(userId)
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
     * Retrieves a profile even if it is soft deleted
     *
     * @param userId ID of profile to retrieve
     * @return Profile object found or else null
     */
    public CompletableFuture<Profile> getDeletedProfile(Long userId) {
        return supplyAsync(() -> ebeanServer.find(Profile.class)
            .setIncludeSoftDeletes()
            .where()
            .idEq(userId)
            .findOneOrEmpty()
            .orElse(null)
        );
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
    public CompletableFuture<PagedList<Profile>> getAllProfiles(Long userId,
        List<Long> nationalityIds, // Possibly null
        List<Long> travellerTypeIds, // Possibly null
        List<String> genders, // Possibly null
        Integer minAge, // Possibly null
        Integer maxAge, // Possibly null
        String searchQuery, // Possibly null
        String sortBy, // Possibly null
        Boolean ascending,
        Integer pageNum,
        Integer pageSize) {

        // Below we make items for each value that we know aren't null, so that EBean won't throw NullPointer,
        // but we must check against the original parameter when checking if the variable was null initially
        List<Long> nationalityIdsNotNull =
            nationalityIds == null ? new ArrayList<>() : nationalityIds;
        List<Long> travellerTypeIdsNotNull =
            travellerTypeIds == null ? new ArrayList<>() : travellerTypeIds;
        List<String> gendersNotNull = genders == null ? new ArrayList<>() : genders;
        Integer minAgeNotNull = minAge == null ? -1 : minAge;
        Integer maxAgeNotNull = maxAge == null ? -1 : maxAge;
        final String cleanedSearchQuery = (searchQuery == null ? "" : searchQuery)
            .replaceAll(" ", "").toLowerCase();

        return supplyAsync(() -> {
            ExpressionList<Profile> profilesExprList =
                ebeanServer.find(Profile.class)
                    .fetch("travellerTypes")
                    .fetch("nationalities")
                    .where()
                    .ne("t0.user_id",
                        userId) // Where t0 is the name ebeans generates for the table (if broken, it's probably this.)
                    // Filter traveller types by given traveller type ids, only if some were given
                    .or(
                        Expr.in("travellerTypes.id", travellerTypeIdsNotNull),
                        (travellerTypeIds == null) ? SQL_TRUE : SQL_FALSE
                    ).endOr()
                    // Filter nationalities by given traveller type ids, only if some were given
                    .or(
                        Expr.in("nationalities.id", nationalityIdsNotNull),
                        (nationalityIds == null) ? SQL_TRUE : SQL_FALSE
                    ).endOr()
                    // Check that gender is in given list, if one is given
                    .or(
                        Expr.in("gender", gendersNotNull),
                        (genders == null) ? SQL_TRUE : SQL_FALSE
                    ).endOr()
                    // Only return results which are greater than min age, if one was specified
                    .or(
                        Expr.le("date_of_birth", Timestamp
                            .valueOf(LocalDate.now().minusYears(minAgeNotNull).atStartOfDay())),
                        (minAge == null) ? SQL_TRUE : SQL_FALSE
                    ).endOr()
                    // Only return results which are less than max age, if one was specified
                    .or(
                        Expr.ge("date_of_birth", Timestamp
                            .valueOf(LocalDate.now().minusYears(maxAgeNotNull).atStartOfDay())),
                        (maxAge == null) ? SQL_TRUE : SQL_FALSE
                    ).endOr()
                    // Search where name fits search query
                    .or(
                        Expr.raw("LOWER(CONCAT(first_name, middle_name, last_name)) LIKE ?",
                            "%" + cleanedSearchQuery + "%"),
                        Expr.raw("LOWER(CONCAT(first_name, last_name)) LIKE ?",
                            "%" + cleanedSearchQuery + "%")
                    ).endOr();

            Query<Profile> query;
            // Apply sorting
            if (ascending) {
                switch (sortBy) {
                    case "first_name":
                        query = profilesExprList.orderBy("first_name asc, last_name asc");
                        break;
                    case "last_name":
                        query = profilesExprList.orderBy("last_name asc, first_name asc");
                        break;
                    default:
                        query = profilesExprList.orderBy().asc(sortBy);
                        break;
                }
            } else {
                switch (sortBy) {
                    case "first_name":
                        query = profilesExprList.orderBy("first_name desc, last_name desc");
                        break;
                    case "last_name":
                        query = profilesExprList.orderBy("last_name desc, first_name desc");
                        break;
                    default:
                        query = profilesExprList.orderBy().desc(sortBy);
                        break;
                }
            }

            // Order by specified column and asc/desc if given, otherwise default to most recently created profiles first
            PagedList<Profile> profiles = query
                .setFirstRow((pageNum - 1) * pageSize)
                .setMaxRows(pageSize)
                .findPagedList();

            // Manually change bean lists to array lists, as this was causing an issue on front end
            for (Profile profile : profiles.getList()) {
                profile.travellerTypes = new ArrayList<>(profile.travellerTypes);
                profile.nationalities = new ArrayList<>(profile.nationalities);
                profile.passports = new ArrayList<>(profile.passports);
            }
            return profiles;
        });
    }

    /**
     * Updates the profile cover photo of some user's profile, and returns the id that WAS being
     * used
     *
     * @param userId ID of user to update cover photo of
     * @param newId New id of photo to set as cover photo
     * @return The id of the photo (possibly null) that was previously used
     */
    public CompletableFuture<Long> updateCoverPhotoAndReturnExistingId(Long userId, Long newId)
        throws NullPointerException {
        return supplyAsync(() -> {
            // Find existing profile
            Profile found = ebeanServer.find(Profile.class)
                .where()
                .eq("user_id", userId)
                .findOne();
            if (found == null) {
                throw new NullPointerException("No such profile");
            }

            Photo foundPhoto = ebeanServer.find(Photo.class)
                .where()
                .eq("guid", newId)
                .findOne();
            if (foundPhoto != null) {
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

    /**
     * Retrieves the number of profiles the user is following and the number of profiles who are
     * following the user
     *
     * @param userId ID of the user to retrieve follower counts for
     * @return A profile object with only the follower count fields populated
     */
    public CompletableFuture<Profile> getProfileFollowerCounts(Long userId) {
        Profile profile = new Profile();

        return supplyAsync(() -> {
            profile.followerUsersCount = (long) ebeanServer.find(FollowerUser.class)
                .where()
                .eq("user_id", userId)
                .findCount();

            profile.followingUsersCount = (long) ebeanServer.find(FollowerUser.class)
                .where()
                .eq("follower_id", userId)
                .findCount();

            profile.followingDestinationsCount = (long) ebeanServer.find(FollowerDestination.class)
                .where()
                .eq("follower_id", userId)
                .findCount();

            return profile;
        });
    }

    /**
     * Retrieves a paginated list of the users (profiles) that a user is following
     *
     * @param profileId ID of user to get who they are following
     * @param pageNum What page of data to return
     * @param pageSize Number of results per page
     * @return Paged list of profiles found
     */
    public CompletableFuture<PagedList<Profile>> getUserFollowingProfiles(
        Long profileId,
        Integer pageNum,
        Integer pageSize) {

        return supplyAsync(() -> {

            String sql = "SELECT * FROM Profile "
                + "WHERE user_id IN (SELECT user_id FROM FollowerUser WHERE follower_id=" + profileId + ") "
                + "ORDER BY (SELECT COUNT(*) FROM FollowerUser WHERE user_id=Profile.user_id) desc";

            return ebeanServer.findNative(Profile.class, sql)
                .setFirstRow((pageNum - 1) * pageSize)
                .setMaxRows(pageSize)
                .findPagedList();

        });

    }

    /**
     * Retrieves a paginated list of the users (profiles) that follow some user
     *
     * @param profileId ID of user to get who is following them
     * @param pageNum What page of data to return
     * @param pageSize Number of results per page
     * @return Paged list of profiles found
     */
    public CompletableFuture<PagedList<Profile>> getUserFollowerProfiles(
        Long profileId,
        Integer pageNum,
        Integer pageSize) {

        return supplyAsync(() -> {

            String sql = "SELECT * FROM Profile "
                + "WHERE user_id IN (SELECT follower_id FROM FollowerUser WHERE user_id=" + profileId + ") "
                + "ORDER BY (SELECT COUNT(*) FROM FollowerUser WHERE user_id=Profile.user_id) desc";

            return ebeanServer.findNative(Profile.class, sql)
                .setFirstRow((pageNum - 1) * pageSize)
                .setMaxRows(pageSize)
                .findPagedList();

        });

    }
}