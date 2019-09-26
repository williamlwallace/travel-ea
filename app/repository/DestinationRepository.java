package repository;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Expr;
import io.ebean.Expression;
import io.ebean.PagedList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import models.Destination;
import models.DestinationTag;
import models.DestinationTravellerTypePending;
import models.FollowerDestination;
import models.PendingDestinationPhoto;
import models.TripData;
import org.apache.commons.text.similarity.LevenshteinDistance;
import play.db.ebean.EbeanConfig;

/**
 * A repository that executes database operations for the Destination table.
 */
@Singleton
public class DestinationRepository {

    private final Expression SQL_FALSE = Expr.raw("false");
    private final Expression SQL_TRUE = Expr.raw("true");

    // The number of decimal places to check for determining similarity of destinations
    // (2 = 1km, 3 = 100m, 4 = 10m, 5 = 1m, ...)
    private static final int COORD_DECIMAL_PLACES = 3;
    // The maximum Levenshtein distance that two destination names
    // may have and still be considered similar
    // (0 = require exact strings, 1000000000 = every string is a match to every other string)
    private static final int NAME_SIMILARITY_THRESHOLD = 10;
    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public DestinationRepository(EbeanConfig ebeanConfig,
        DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    /**
     * Adds a new destination to the database.
     *
     * @param destination the new destination to add
     * @return A CompletableFuture with the new destination's id
     */
    public CompletableFuture<Long> addDestination(Destination destination) {
        return supplyAsync(() -> {
            destination.deleted = false;
            ebeanServer.insert(destination);
            return destination.id;
        }, executionContext);
    }

    /**
     * Deletes a destination from the database.
     *
     * @param id Unique destination ID of destination to be deleted
     * @return The number of rows deleted
     */
    public CompletableFuture<Integer> deleteDestination(Long id) {
        return supplyAsync(() ->
                ebeanServer.find(Destination.class)
                    .where()
                    .idEq(id)
                    .delete()
            , executionContext);
    }

    /**
     * Updates a destination.
     *
     * @param destination The destination object to update, with the updated parameters
     * @return The updated destination
     */
    public CompletableFuture<Destination> updateDestination(Destination destination) {
        return supplyAsync(() -> {
            if (destination.tags.isEmpty()) {
                ebeanServer.find(DestinationTag.class).where().eq("destination_id", destination.id)
                    .delete();
            }
            ebeanServer.update(destination);
            return destination;
        }, executionContext);
    }

    /**
     * Sets the owner of a destination.
     *
     * @param destinationId id of destination to update
     * @param newUserId id to set user to
     * @return The number of rows that were updated
     */
    public CompletableFuture<Integer> changeDestinationOwner(Long destinationId, Long newUserId) {
        return supplyAsync(() ->
            ebeanServer.createUpdate(Destination.class,
                "UPDATE Destination SET user_id=:newUserId WHERE id=:id")
                .setParameter("newUserId", newUserId)
                .setParameter("id", destinationId)
                .execute()
        );
    }

    /**
     * Changes the database entry for a destination to have a public state of true. Returns not
     * found if no such destination exists, and returns badRequest if the destination was already
     * public
     *
     * @param destinationId ID of destination to make public
     */
    public void setDestinationToPublicInDatabase(Long destinationId) {
        Destination destination = ebeanServer.find(Destination.class)
            .where()
            .eq("id", destinationId)
            .findOneOrEmpty()
            .orElse(null);

        if (destination != null) {
            destination.isPublic = true;
            ebeanServer.update(destination);
        }
    }

    /**
     * Changes any references in TripData of any similar destinations that are going to be merged.
     * to the id of the destination that they have been merged to. I.e if there are 3 Eiffel towers,
     * and one of them is made public, the other two will now point to the new public Eiffel tower.
     *
     * The number of rows changed is also returned, use this to check if there were other
     * destinations in use that have been merged, i.e that we must transfer this newly public
     * destination to the ownership of master admin
     *
     * @param similarDestinationIds The IDs of all destinations which have been found to be similar
     * @param newDestinationId The ID of the destination which has been made public, that will now
     * be used in place of old destinations
     * @return Number of rows changed by this operation (the number of instances where a destination
     * that is being merged was used by a different user)
     */
    public int mergeDestinationsTripData(Collection<Long> similarDestinationIds,
        Long newDestinationId) {

        // Return 0 rows changed if no similar destinations found
        if (similarDestinationIds.isEmpty()) {
            return 0;
        }

        String sql = "UPDATE TripData "
            + "SET destination_id=:newDestId "
            + "WHERE destination_id IN (:oldDestIds);";

        return ebeanServer.createUpdate(TripData.class, sql)
            .setParameter("newDestId", newDestinationId)
            .setParameter("oldDestIds", similarDestinationIds)
            .execute();
    }

    /**
     * Changes any references in DestinationPhoto of similar destinations, to the id of the
     * destination that they have been merged to. I.e if there are 3 Eiffel towers, and one of them
     * is made public, the other two will now point to the new public Eiffel tower.
     *
     *
     * The number of rows changed is also returned, use this to check if there were other
     * destinations in use that have been merged, i.e that we must transfer this newly public
     * destination to the ownership of master admin
     *
     * @param similarDestinationIds The IDs of all destinations which have been found to be similar
     * @param newDestinationId The ID of the destination which has been made public, that will now
     * be used in place of old destinations
     * @return Number of rows changed by this operation (the number of instances where a destination
     * that is being merged was used by a different user)
     */
    public int mergeDestinationsPhotos(Collection<Long> similarDestinationIds,
        Long newDestinationId) {
        // Return 0 rows changed if no similar destinations found
        if (similarDestinationIds.isEmpty()) {
            return 0;
        }

        String sql = "UPDATE DestinationPhoto "
            + "SET destination_id=:newDestId "
            + "WHERE destination_id IN (:oldDestIds);";

        return ebeanServer.createSqlUpdate(sql)
            .setParameter("newDestId", newDestinationId)
            .setParameter("oldDestIds", similarDestinationIds)
            .execute();
    }

    /**
     * Get all destinations that are found to be similar to some other destination. (does not
     * include initial destination)
     *
     *
     * This is checked by comparing their locations, and if these are similar to within some range,
     * then their names are also checked for similarity
     *
     * @param destination New destination to check against existing destinations
     * @return Destinations found to be sufficiently similar
     */
    public List<Destination> getSimilarDestinations(Destination destination) {
        return ebeanServer.find(Destination.class)
            // Add where clause to make sure longitudes are the same
            // (to specified number of decimal places)
            .where(Expr.raw("TRUNCATE(longitude, ?) = ?", new Object[]{
                COORD_DECIMAL_PLACES,
                Math.floor(destination.longitude * Math.pow(10, COORD_DECIMAL_PLACES)) / Math
                    .pow(10, COORD_DECIMAL_PLACES)
                // A slightly hacky way to truncate to COORD_DECIMAL_PLACES dp
            }))
            // Add where clause to make sure latitudes are the
            // same (to specified number of decimal places)
            .where(Expr.raw("TRUNCATE(latitude, ?) = ?", new Object[]{
                COORD_DECIMAL_PLACES,
                Math.floor(destination.latitude * Math.pow(10, COORD_DECIMAL_PLACES)) / Math
                    .pow(10, COORD_DECIMAL_PLACES)
                // A slightly hacky way to truncate to COORD_DECIMAL_PLACES dp
            }))
            .findList().stream()
            // only return results for which the name is suitably
            // similar (i.e Levenshtein distance is less than specified value)
            .filter(x -> new LevenshteinDistance().apply(x.name, destination.name)
                <= NAME_SIMILARITY_THRESHOLD)
            // do not include the destination we are finding similarities for
            .filter(x -> !x.id.equals(destination.id))
            // collect all found destinations into list
            .collect(Collectors.toList());
    }

    /**
     * Gets a single destination given the destination ID.
     *
     * @param id Unique destination ID of the requested destination
     * @return A single destination with the requested ID, or null if none was found
     */
    public CompletableFuture<Destination> getDestination(Long id) {
        return supplyAsync(() -> ebeanServer.find(Destination.class)
            .where()
            .idEq(id)
            .findOneOrEmpty()
            .orElse(null), executionContext);
    }

    /**
     * Gets a single including deleted destination given the destination ID.
     *
     * @param id Unique destination ID of the requested destination
     * @return A single destination with the requested ID, or null if none was found
     */
    public CompletableFuture<Destination> getDeletedDestination(Long id) {
        return supplyAsync(() -> ebeanServer.find(Destination.class)
            .setIncludeSoftDeletes()
            .where()
            .idEq(id)
            .findOneOrEmpty()
            .orElse(null), executionContext);
    }

    /**
     * Gets all the destinations valid for the specified user.
     *
     * @param userId ID of user to retrieve destinations for
     * @return List of destinations
     */
    public CompletableFuture<List<Destination>> getAllDestinations(Long userId) {
        return supplyAsync(() -> ebeanServer.find(Destination.class)
                .where()
                .or()
                .eq("user_id", userId)
                .eq("is_public", 1)
                .findList()
            , executionContext);
    }

    /**
     * Gets all the public destinations.
     *
     * @return List of destinations
     */
    public CompletableFuture<List<Destination>> getAllPublicDestinations() {
        return supplyAsync(() -> ebeanServer.find(Destination.class)
                .where()
                .eq("is_public", 1)
                .findList()
            , executionContext);
    }

    /**
     * Gets all the destinations, ALL of them.
     *
     * @return List of destinations
     */
    public CompletableFuture<List<Destination>> getAllDestinationsAdmin() {
        return supplyAsync(() -> ebeanServer.find(Destination.class)
                .findList()
            , executionContext);
    }

    /**
     * Gets all destinations with a traveller type modification request.
     *
     * @return List of destinations
     */
    public CompletableFuture<List<Destination>> getAllDestinationsWithRequests() {
        return supplyAsync(() -> {

            List<DestinationTravellerTypePending> travellerTypeRequests = ebeanServer
                .find(DestinationTravellerTypePending.class)
                .findList();
            List<PendingDestinationPhoto> photoRequests = ebeanServer
                .find(PendingDestinationPhoto.class)
                .findList();

            Set<Long> destinationsWithPending = new HashSet<>();

            for (DestinationTravellerTypePending request : travellerTypeRequests) {
                destinationsWithPending.add(request.destId);
            }
            for (PendingDestinationPhoto request : photoRequests) {
                destinationsWithPending.add(request.destId);
            }

            if (destinationsWithPending.isEmpty()) {
                return new ArrayList<>();
            } else {
                return ebeanServer.find(Destination.class).where().idIn(destinationsWithPending)
                    .findList();
            }
        }, executionContext);
    }

    /**
     * Gets a paged list of destinations conforming to the amount of destinations requested and the
     * provided order and filters.
     *
     * @param searchQuery Query to search all fields for
     * @param sortBy What column to sort by
     * @param onlyGetMine Whether or not to only get my own destinations
     * @param ascending Whether or not to sort ascendingly
     * @param pageNum Page number to get
     * @param pageSize Number of results to show per page
     * @return Paged list of destinations
     */
    public CompletableFuture<PagedList<Destination>> getPagedDestinations(
        Long userId,
        String searchQuery, // Nullable
        Boolean onlyGetMine,
        String sortBy,
        Boolean ascending,
        Integer pageNum,
        Integer pageSize) {
        String cleanedQueryString = "%" + (searchQuery == null ? "" : searchQuery) + "%";

        return supplyAsync(() -> ebeanServer.find(Destination.class)
            .fetch("country")
            .where()
            // Only get results that match search query, or if no search query provided
            // Big nested or statement allows for searching multiple fields by one value
            .or(
                (searchQuery == null || searchQuery.equals("")) ? SQL_TRUE : SQL_FALSE,
                Expr.or(
                    Expr.ilike("name", cleanedQueryString),
                    Expr.or(
                        Expr.ilike("type", cleanedQueryString),
                        Expr.or(
                            Expr.ilike("country.name", cleanedQueryString),
                            Expr.ilike("district", cleanedQueryString)
                        )
                    )
                )
            ).endOr()
            // If the user only wants their own destinations, filter out all other
            .or(
                Expr.eq("user_id", userId),
                onlyGetMine ? SQL_FALSE : SQL_TRUE
            ).endOr()
            // If the user doesn't only want theirs, show them public and their own
            .or(
                onlyGetMine ? SQL_TRUE : SQL_FALSE,
                Expr.or(
                    Expr.eq("is_public", true),
                    Expr.eq("user_id", userId)
                )
            )
            .orderBy(sortBy + " " + (ascending ? "asc" : "desc"))
            .setFirstRow((pageNum - 1) * pageSize)
            .setMaxRows(pageSize)
            .findPagedList());
    }

    /**
     * Transfers the ownership of a destination to master admin if the destination is being used by
     * another user.
     *
     * @param destinations List of ID's of destinations to check ownership and potentially transfer
     * @param userId ID of user using the destination
     * @param masterId ID of the master admin (ID to transfer ownership to)
     * @return The number of destinations transferred to the master admin
     */
    public Integer updateDestinationOwnershipUsedInTrip(Collection<Long> destinations, Long userId,
        Long masterId) {
        String sql = "UPDATE Destination "
            + "SET user_id = :masterId, "
            + "is_public = 1 "
            + "WHERE user_id != :userId "
            + "AND user_id != :masterId "
            + "AND id IN (:destinations);";

        return ebeanServer.createUpdate(Destination.class, sql)
            .setParameter("destinations", destinations)
            .setParameter("userId", userId)
            .setParameter("masterId", masterId)
            .execute();
    }

    /**
     * Retrieves a list of destinations which have an ID provided
     *
     * @param destinationIds List of ID's of destinations to retrieve
     * @return List of destination objects
     */
    public CompletableFuture<List<Destination>> getDestinationsById(Set<Long> destinationIds) {
        if (destinationIds.isEmpty()) {
            return CompletableFuture.supplyAsync(ArrayList::new);
        } else {
            return supplyAsync(() -> ebeanServer.find(Destination.class)
                    .where()
                    .idIn(destinationIds)
                    .findList()
                , executionContext);
        }
    }

    /**
     * Gets the followerUser object from the database where the two given ids match the relevant
     * columns
     *
     * @param destId the id of the destination which is being followed
     * @param followerId the id of the user following
     * @return the FollowUser object if it exists, null otherwise
     */
    public CompletableFuture<FollowerDestination> getFollower(Long destId, Long followerId) {
        return supplyAsync(() ->
            ebeanServer.find(FollowerDestination.class)
                .where().and(
                Expr.eq("destination_id", destId),
                Expr.eq("follower_id", followerId))
                .findOneOrEmpty()
                .orElse(null));
    }

    /**
     * Inserts a follower destination pair
     *
     * @param followerDestination the object to add
     * @return the guid of the inserted followerUser
     */
    public CompletableFuture<Long> insertFollower(FollowerDestination followerDestination) {
        return supplyAsync(() -> {
            ebeanServer.insert(followerDestination);
            return followerDestination.guid;
        }, executionContext);
    }

    /**
     * Deletes a destination follower pair
     *
     * @param id guid of the FollowerDestination to delete
     * @return the number of rows that were deleted
     */
    public CompletableFuture<Long> deleteFollower(Long id) {
        return supplyAsync(() ->
                Long.valueOf(ebeanServer.delete(FollowerDestination.class, id))
            , executionContext);
    }

    /**
     * Retrieves the count of users following a destination
     *
     * @param destinationId ID of destination to retrieve follower count for
     * @return Number of users following the destination
     */
    public CompletableFuture<Long> getDestinationFollowerCount(Long destinationId) {
        return supplyAsync(() ->
                Long.valueOf(ebeanServer.find(FollowerDestination.class)
                    .where()
                    .eq("destination_id", destinationId)
                    .findCount())
            , executionContext);
    }

    /**
     * Retrieves a paginated list of the destinations that a user is following
     *
     * @param userId ID of user to retrieve destinations followed by
     * @param searchQuery Name of destination to filter from frontend
     * @param pageNum What page of data to return
     * @param pageSize Number of results per page
     * @return Paged list of destinations found sorted by most followers
     */
    public CompletableFuture<PagedList<Destination>> getDestinationsFollowedByUser(Long userId,
        String searchQuery, Integer pageNum, Integer pageSize) {

        return supplyAsync(() -> {

            String sql = "SELECT * FROM Destination "
                + "WHERE id IN (SELECT destination_id FROM FollowerDestination "
                + "WHERE follower_id=" + userId + ") "
                + "AND LOWER(name) LIKE LOWER(:searchQuery) "
                + "ORDER BY (SELECT COUNT(*) FROM FollowerDestination "
                + "WHERE destination_id=Destination.id) desc";

            return ebeanServer.findNative(Destination.class, sql)
                .setParameter("searchQuery",
                    "%" + searchQuery + "%") // No injection here, move along
                .setFirstRow((pageNum - 1) * pageSize)
                .setMaxRows(pageSize)
                .findPagedList();
        });
    }

    /**
     * Gets the number of followers each destination has
     *
     * @param ids DestinationIds of the destinations for which to get the follower counts of
     * @return map between a destination id and the number of followers they have
     */
    public Map<Long, Long> getDestinationsFollowerCounts(List<Long> ids) {
        String sqlQuery = "SELECT destination_id, (SELECT COUNT(*) FROM `FollowerDestination` FD2 "
            + "WHERE FD2.destination_id = FD1.destination_id) AS followCount FROM `FollowerDestination` "
            + "FD1 WHERE FD1.destination_id in (:ids) GROUP BY FD1.destination_id";

        if (ids.isEmpty()) {
            return null;
        } else {
            Map<Long, Long> results = new HashMap<>();
            ebeanServer.createSqlQuery(sqlQuery).setParameter("ids", ids)
                .findEachRow(((resultSet, rowNum) -> {
                    results.put(resultSet.getLong(1), resultSet.getLong(2));
                }));
            return results;
        }
    }
}