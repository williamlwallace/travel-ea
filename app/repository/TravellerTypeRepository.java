package repository;

import models.TravellerType;
import models.TravellerType.TravellerTypeKey;

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

    private final DatabaseExecutionContext executionContext;

    @Inject
    public TravellerTypeRepository(DatabaseExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    /**
     * Adds a TravellerType to a user's profile
     * @param travellerType The TravellerType to add
     * @return The key of the added TravellerType
     */
    public CompletableFuture<TravellerTypeKey> addTravellerTypeToProfile(TravellerType travellerType) {
        return supplyAsync(() -> {
            travellerType.insert();
            return travellerType.key;
        }, executionContext);
    }

    /**
     * Deletes a TravellerType from a user's profile provided
     * @param profileId The id of the profile to delete the TravellerType from
     * @param travellerTypeId The id of the TravellerTypeDefinition to remove from the user's profile
     * @return false if the delete failed, true otherwise
     */
    public CompletableFuture<Boolean> deleteTravellerTypeFromProfile(Long profileId, Long travellerTypeId) {
        return supplyAsync(() -> TravellerType.find.byId(new TravellerTypeKey(profileId, travellerTypeId)).delete()
                , executionContext);
    }

    /**
     * Deletes a TravellerType from a user's profile provided that TravellerType object
     * @param travellerType The TravellerType object to delete
     * @return false if the delete failed, true otherwise
     */
    public CompletableFuture<Boolean> deleteTravellerTypeFromProfile(TravellerType travellerType){
        return supplyAsync(() -> travellerType.delete(), executionContext);
    }

    /**
     * Finds all TravellerTypes associated with a profile
     * @param profileId The id of the profile to search through
     * @return A list of Traveller Types
     */
    public CompletableFuture<List<TravellerType>> getAllTravellerTypesFromProfile(Long profileId) {
        return supplyAsync(() -> TravellerType.find.query()
                        .where()
                        .ieq("uid", Long.toString(profileId))
                        .findList(),
                executionContext);
    }
}
