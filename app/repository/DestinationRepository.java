package repository;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.notFound;
import static play.mvc.Results.ok;

import cucumber.api.java.hu.De;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Expr;
import io.ebean.PagedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.inject.Inject;
import models.Destination;
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
     * Makes a destination public, if it is found and not already public
     * @param destinationId ID of destination to mark as public
     * @return notFound if no such ID found, badRequest if it is found but already public, ok if found and successfully updated to public from private
     */
    public CompletableFuture<Result> makeDestinationPublic(Long destinationId) {
        return supplyAsync(() -> {
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
        });
    }

    /**
     * Get all destinations that are found to be similar to some other destination (does not include initial destination)
     *
     * This is checked by comparing their locations, and if these are similar to within some range, then their names are also checked for similarity
     *
     * @param destination New destination to check against existing destinations
     * @return Destinations found to be sufficiently similar
     */
    public CompletableFuture<List<Destination>> getSimilarDestinations(Destination destination) {
        return supplyAsync(() -> ebeanServer.find(Destination.class)
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
                .collect(Collectors.toList())
        );
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
     * Gets a list of all the destinations in the database.
     *
     * @return list of destinations
     */
    public CompletableFuture<List<Destination>> getAllDestinations() {
        return supplyAsync(() -> ebeanServer.find(Destination.class).findList(), executionContext);
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

}
