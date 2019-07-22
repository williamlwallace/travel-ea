package repository;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Expr;
import io.ebean.PagedList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import models.Destination;
import models.TripData;
import org.apache.commons.text.similarity.LevenshteinDistance;
import play.db.ebean.EbeanConfig;

/**
 * A repository that executes database operations for the Destination table.
 */
@Singleton
public class DestinationRepository {

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
     * Deletes multiple destinations from database.
     *
     * @param ids Unique destination IDs of destinations to be deleted
     * @return The number of rows deleted
     */
    public CompletableFuture<Integer> deleteDestinations(Collection<Long> ids) {
        return supplyAsync(() ->
                ebeanServer.find(Destination.class)
                    .where()
                    .idIn(ids)
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
            ebeanServer.update(destination);
            return destination;
        }, executionContext);
    }

    /**
     * Sets the owner of a destination.
     *
     * @param destinationId id of destination to update
     * @param newUserId id to set user to
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
     * @return Result of call, ok if good, badRequest if already public, not found if no such
     * destination ID found
     */
    public void setDestinationToPublicInDatabase(Long destinationId) {
        Destination destination = ebeanServer.find(Destination.class)
            .where()
            .eq("id", destinationId).findOneOrEmpty().orElse(null);

        // Otherwise set to public, update it, and return ok
        destination.isPublic = true;
        ebeanServer.update(destination);
    }

    /**
     * Changes any references in TripData of any similar destinations that are going to be merged.
     * to the id of the destination that they have been merged to. I.e if there are 3 Eiffel towers,
     * and one of them is made public, the other two will now point to the new public Eiffel tower.
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
     * Gets a single includeding deleted destination given the destination ID.
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
     * Gets a paged list of destinations conforming to the amount of destinations requested and the
     * provided order and filters.
     *
     * @param page The current page to display
     * @param pageSize The number of destinations per page
     * @param order The column to order by
     * @param filter The sort order (either asc or desc)
     * @return Paged list of destinations
     */
    public CompletableFuture<PagedList<Destination>> getPagedDestinations(int page, int pageSize,
        String order, String filter) {
        return supplyAsync(() -> ebeanServer.find(Destination.class)
                .where()
                .ilike("name", "%" + filter + "%")
                .orderBy(order)
                .setFirstRow(page * pageSize)
                .setMaxRows(pageSize)
                .findPagedList()
            , executionContext);
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
}
