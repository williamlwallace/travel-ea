package repository;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;
import models.Destination;
import models.DestinationTravellerType;
import play.db.ebean.EbeanConfig;


/**
 * A repository that executes database operations for the Destination table.
 */
@Singleton
public class DestinationTravellerTypeRepository {

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public DestinationTravellerTypeRepository(EbeanConfig ebeanConfig,
        DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    // /**
    //  * Adds a new destination travellerType
    //  *
    //  * @param destinationTravellerType the new destinationtravellertype to add
    //  * @return A CompletableFuture with the new rows updated
    //  */
    // public CompletableFuture<Long> addLink(DestinationTravellerType destinationTravellerType) {
    //     return supplyAsync(() -> {
    //         ebeanServer.insert(destinationTravellerType);
    //         return destinationTravellerType.guid;
    //     }, executionContext);
    // }

    // /**
    //  * Updates a new destination travellerType
    //  *
    //  * @param destinationTravellerType the new destinationtravellertype to update
    //  * @return A CompletableFuture with the new rows updated
    //  */
    // public CompletableFuture<Long> updateLink(DestinationTravellerType destinationTravellerType) {
    //     return supplyAsync(() -> {
    //         DestinationTravellerType dtt = ebeanServer.find(destinationTravellerType)
    //             .where()
    //             .eq("dest_id", destinationTravellerType.destId)
    //             .eq("traveller_type_definition_id", destinationTravellerType.travellerTypeDefinition.id)
    //             .eq("is_pending", !destinationTravellerType.isPending)
    //             .findOneOrEmpty().orElse(null);
    //         destinationTravellerType.guid = dtt.guid;
    //         return ebeanServer.update(destinationTravellerType);
    //     }, executionContext);
    // }
}