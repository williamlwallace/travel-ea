package repository;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import models.Destination;
import models.DestinationTravellerType;
import play.db.ebean.EbeanConfig;


/**
 * A repository that executes database operations for the Destination table.
 */
public class DestinationTravellerTypeRepository {

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public DestinationTravellerTypeRepository(EbeanConfig ebeanConfig,
        DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    /**
     * Adds a new destination travellerType
     *
     * @param destinationTravellerType the new destinationtravellertype to add
     * @return A CompletableFuture with the new rows updated
     */
    public CompletableFuture<Long> addLink(DestinationTravellerType destinationTravellerType) {
        return supplyAsync(() -> {
            ebeanServer.insert(destinationTravellerType);
            return destinationTravellerType.guid;
        }, executionContext);
    }
}