package repository;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.PagedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import models.Destination;
import play.db.ebean.EbeanConfig;

/**
 * A repository that executes database operations for the Destination table
 */
public class DestinationRepository {

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public DestinationRepository(EbeanConfig ebeanConfig,
        DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    /**
     * Adds a new destination to the database
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
     * Deletes a destination from the database
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
     * Updates a destination
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
     * Gets a single destination given the destination ID
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
     * Gets a list of all the destinations in the database
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
