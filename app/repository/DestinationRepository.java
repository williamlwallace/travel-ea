package repository;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.notFound;
import static play.mvc.Results.ok;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Expr;
import io.ebean.PagedList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.inject.Inject;
import models.Destination;
import models.TripData;
import models.User;
import org.apache.commons.text.similarity.LevenshteinDistance;
import play.db.ebean.EbeanConfig;
import play.mvc.Result;

/**
 * A repository that executes database operations for the Destination table.
 */
public class DestinationRepository {

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    // The number of decimal places to check for determining similarity of destinations (2 = 1km, 3 = 100m, 4 = 10m, 5 = 1m, ...)
    private static final int COORD_DECIMAL_PLACES = 3;
    // The maximum Levenshtein distance that two destination names may have and still be considered similar (0 = require exact strings, 1000000000 = every string is a match to every other string)
    private static final int NAME_SIMILARITY_THRESHOLD = 10;

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
     * Sets the owner of a destination
     * @param destinaitonId id of destination to update
     * @param newUserId id to set user to 
     */
    public CompletableFuture<Integer> changeDestinationOwner(Long destinationId, Long newUserId) {
        return supplyAsync(() -> 
            ebeanServer.createUpdate(Destination.class, "UPDATE Destination SET user_id=:newUserId WHERE id=:id")
            .setParameter("newUserId", newUserId)
            .setParameter("id", destinationId)
            .execute()
        );
    }

    /**
     * Makes a destination public, if it is found and not already public,
     * Also find similar destinations and merge them into the destination being made public
     * If the destination is being used in a trip by another user, then update the ownership of the destination to be master admin
     * @param destination The destination to mark as public
     * @return notFound if no such ID found, badRequest if it is found but already public, ok if found and successfully updated to public from private
     */
    public CompletableFuture<Result> makeDestinationPublic(User user, Destination destination) {
        return supplyAsync(() -> {
            // Update the publicity of the destination in the database, and check what status gets returned
            Result result = setDestinationToPublicInDatabase(destination.id);

            // if the result was anything other than okay, return this as it is an error condition
            if (result.status() != ok().status()) {
                return result;
            }

            // Find all similar destinations that need to be merged
            List<Destination> destinations = getSimilarDestinations(destination);
            List<Long> similarIds = destinations.stream().map(x -> x.id).collect(Collectors.toList());

            // Re-reference each instance of the old destinations to the new one, keeping track of how many rows were changed
            // TripData
            int rowsChanged = mergeDestinationsTripData(user.id, similarIds, destination.id);
            // Photos
            // TODO: call the method that updates the photos. Also not written
            // If any rows were changed when re-referencing, the destination has been used by another user and must be transferred to admin ownership
            if (rowsChanged > 0) {
                makePermanentlyPublic(destination);
            }
            return ok();
        });
    }

    /**
     * changes the database entry for a destination to have a public state of true, returns not found if no such destination exists,
     * and returns badRequest if the destination was already public
     * @param destinationId ID of destination to make public
     * @return Result of call, ok if good, badRequest if already public, not found if no such destination ID found
     */
    private Result setDestinationToPublicInDatabase(Long destinationId) {
        Destination destination = ebeanServer.find(Destination.class)
                .where()
                .eq("id", destinationId).findOneOrEmpty().orElse(null);
        // If no destination was found, return not found
        if(destination == null) {
            return notFound();
        }
        // If destination was found but is already marked public, return bad request
        if(destination.isPublic) {
            return badRequest();
        }
        // Otherwise set to public, update it, and return ok
        destination.isPublic = true;
        ebeanServer.update(destination);
        return ok();
    }

    /**
     * Changes any references in TripData of any similar destinations that are going to be merged,
     * to the id of the destination that they have been merged to. I.e if there are 3 Eiffel towers, and
     * one of them is made public, the other two will now point to the new public Eiffel tower.
     *
     * The number of rows changed is also returned, use this to check if there were other destinations in use
     * that have been merged, i.e that we must transfer this newly public destination to the ownership of master admin
     *
     * @param userId ID of user who is changing the destination to public (i.e forcing a merge to happen)
     * @param similarDestinationIds The IDs of all destinations which have been found to be similar
     * @param newDestinationId The ID of the destination which has been made public, that will now be used in place of old destinations
     * @return Number of rows changed by this operation (the number of instances where a destination that is being merged was used by a different user)
     */
    private int mergeDestinationsTripData(Long userId, Collection<Long> similarDestinationIds, Long newDestinationId) {
        String sql = "UPDATE TripData " +
                        "SET destination_id=:newDestId " +
                        "WHERE (SELECT user_id FROM TRIP WHERE id = trip_id) != :userId " +
                        "AND destination_id IN (:oldDestIds);";

        return ebeanServer.createUpdate(TripData.class, sql)
                .setParameter("newDestId", newDestinationId)
                .setParameter("userId", userId)
                .setParameter("oldDestIds", similarDestinationIds)
                .execute();
    }

    //TODO: Copy the above method (mergeDestinationsTripData) to also operate on the table joining photos and destinations

    /**
     * Get all destinations that are found to be similar to some other destination (does not include initial destination)
     *
     * This is checked by comparing their locations, and if these are similar to within some range, then their names are also checked for similarity
     *
     * @param destination New destination to check against existing destinations
     * @return Destinations found to be sufficiently similar
     */
    public List<Destination> getSimilarDestinations(Destination destination) {
        return ebeanServer.find(Destination.class)
           // Add where clause to make sure longitudes are the same (to specified number of decimal places)
           .where(Expr.raw("TRUNCATE(longitude, ?) = ?", new Object[] {
                    COORD_DECIMAL_PLACES,
                    Math.floor(destination.longitude * Math.pow(10, COORD_DECIMAL_PLACES)) / Math.pow(10, COORD_DECIMAL_PLACES) // A slightly hacky way to truncate to COORD_DECIMAL_PLACES dp
           }))
           // Add where clause to make sure latitudes are the same (to specified number of decimal places)
           .where(Expr.raw("TRUNCATE(latitude, ?) = ?", new Object[] {
                    COORD_DECIMAL_PLACES,
                    Math.floor(destination.latitude * Math.pow(10, COORD_DECIMAL_PLACES)) / Math.pow(10, COORD_DECIMAL_PLACES) // A slightly hacky way to truncate to COORD_DECIMAL_PLACES dp
           }))
           .findList().stream()
                // only return results for which the name is suitably similar (i.e Levenshtein distance is less than specified value)
                .filter(x -> new LevenshteinDistance().apply(x.name, destination.name) <= NAME_SIMILARITY_THRESHOLD)
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
     * Gets all the destinations valid for the specified user
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
     * Gets all the public destinations
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
     * Checks if a destination belongs to the user provided
     *
     * @param destination Destination object to check ownership of
     * @param userId ID of user using the destination
     * @return A destination object if one is found and doesn't belong to the
     * specified user or the master admin, otherwise null
     */
    public CompletableFuture<Destination> checkDestinationInTrip(Destination destination, Long userId) {
        Long masterAdminId = 1L;    // TODO: Change to master admin constant
        return supplyAsync(() -> ebeanServer.find(Destination.class)
                        .where()
                        .idEq(destination.id)
                        .ne("user_id", userId)
                        .ne("user_id", masterAdminId)
                        .findOneOrEmpty()
                        .orElse(null)
                , executionContext);
    }

    /**
     * Updates the ownership of the destination to the master admin
     *
     * @param destination Destination to be updated
     * @return Modified destination object
     */
    public CompletableFuture<Destination> makePermanentlyPublic(Destination destination) {
        // Change ownership to master admin and ensures destination is public
        destination.user.id = 1L;    // TODO: Change to master admin constant
        destination.isPublic = true;
        return supplyAsync(() -> {
            ebeanServer.update(destination);
            return destination;
        }, executionContext);
    }
}
