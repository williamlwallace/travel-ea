package repository;

import io.ebean.PagedList;
import models.Destination;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * A repository that executes database operations for the Destination table
 */
public class DestinationRepository {

    private final DatabaseExecutionContext executionContext;

    @Inject
    public DestinationRepository(DatabaseExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    /**
     * Adds a new destination to the database
     * @param destination the new destination to add
     * @return A CompletableFuture with the new destination's id
     */
    public CompletableFuture<Long> addDestination(Destination destination) {
        return supplyAsync(() -> {
            destination.insert();
            return destination.id;
        }, executionContext);
    }

    /**
     * Deletes a destination from the database
     * @param id    Unique destination ID of destination to be deleted
     * @return      The ID of the deleted destination
     */
    public CompletableFuture<Long> deleteDestination(Long id) {
        return supplyAsync(() -> {
            Destination.find.byId(id).delete();
            return id;
        }, executionContext);
    }

    /**
     * Updates a destination
     * @param destination The destination object to update, with the updated parameters
     * @return The updated destination
     */
    public CompletableFuture<Destination> updateDestination(Destination destination) {
        return supplyAsync(() -> {
            destination.update();
            return destination;
        }, executionContext);
    }

    /**
     * Gets a single destination given the destination ID
     * @param id Unique destination ID of the requested destination
     * @return   A single destination with the reqested ID
     */
    public CompletableFuture<Destination> getDestination(Long id) {
        return supplyAsync(() -> Destination.find.byId(id), executionContext);
    }

    /**
     * Gets a list of all the destinations in the database
     * @return list of destinations
     */
    public CompletableFuture<List<Destination>> getAllDestinations() {
        return supplyAsync(() -> Destination.find.all(), executionContext);
    }

    /**
     * Gets a paged list of destinations conforming to the amount of destinations requested and the provided order and
     * filters.
     * @param page      The current page to display
     * @param pageSize  The number of destinations per page
     * @param order     The column to order by
     * @param filter    The sort order (either asc or desc)
     * @return Paged list of destinations
     */
    public CompletableFuture<PagedList<Destination>> getPagedDestinations(int page, int pageSize, String order, String filter) {
        return supplyAsync(() -> Destination.find.query()
                    .where()
                    .ilike("name", "%" + filter + "%")
                    .orderBy(order)
                    .setFirstRow(page * pageSize)
                    .setMaxRows(pageSize)
                    .findPagedList()
                    , executionContext);
    }

}
