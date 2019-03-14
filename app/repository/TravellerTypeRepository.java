package repository;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import models.TravellerType;
import models.TravellerType.TravellerTypeKey;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * A repository that executes database operations for the TravellerType and TravellerTypeDefinition tables
 *
 * @author Harrison Cook
 */
public class TravellerTypeRepository {

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public TravellerTypeRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    /**
     * Adds a TravellerType to a user's profile
     * @param travellerType The TravellerType to add
     * @return The guid of the added TravellerType
     */
    public CompletableFuture<Long> addTravellerTypeToProfile(TravellerType travellerType) {
        // If traveller type started with an ID, set it to null as this is generated
        travellerType.guid = null;

        return supplyAsync(() -> {
            ebeanServer.insert(travellerType);
            return travellerType.guid;
        }, executionContext);
    }

    /**
     * Deletes a TravellerType from a user's profile provided
     * @param profileId The id of the profile to delete the TravellerType from
     * @param travellerTypeId The id of the TravellerTypeDefinition to remove from the user's profile
     * @return false if the delete failed, true otherwise
     */
    public CompletableFuture<Integer> deleteTravellerTypeFromProfile(Long profileId, Long travellerTypeId) {
        return supplyAsync(() ->
            ebeanServer.find(TravellerType.class)
                    .where()
                    .eq("traveller_type_id", travellerTypeId)
                    .eq("user_id", profileId)
                    .delete()
                , executionContext);
    }

    /**
     * Deletes a TravellerType from a user's profile provided that TravellerType object
     * @param travellerType The TravellerType object to delete
     * @return false if the delete failed, true otherwise
     */
    public CompletableFuture<Integer> deleteTravellerTypeFromProfile(TravellerType travellerType){
        return supplyAsync(() ->
            ebeanServer.find(TravellerType.class)
                    .where()
                    .eq("guid", travellerType.guid)
                    .delete()
                , executionContext);
    }

    /**
     * Finds all TravellerTypes associated with a profile
     * @param profileId The id of the profile to search through
     * @return A list of Traveller Types
     */
    public CompletableFuture<List<TravellerType>> getAllTravellerTypesFromProfile(Long profileId) {
        return supplyAsync(() ->
            ebeanServer.find(TravellerType.class)
                    .where()
                    .ieq("user_id", Long.toString(profileId))
                    .findList(),
                executionContext);
    }
}
