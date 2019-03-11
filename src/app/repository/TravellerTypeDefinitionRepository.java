package repository;

import models.TravellerTypeDefinition;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import static java.util.concurrent.CompletableFuture.supplyAsync;

public class TravellerTypeDefinitionRepository {

    private final DatabaseExecutionContext executionContext;

    @Inject
    public TravellerTypeDefinitionRepository(DatabaseExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    /**
     * Gets a TravellerTypeDefinition given that TravellerTypeDefinition's id
     * @param id The id of the TravellerTypeDefinition
     * @return The TravellerTypeDefinition with matching id, null if no TravellerTypeDefinition found
     */
    public CompletableFuture<TravellerTypeDefinition> getTravellerTypeDefinitionById(Long id) {
        return supplyAsync(() -> TravellerTypeDefinition.find.byId(id), executionContext);
    }

    /**
     * Gets a TravellerTypeDefinition given a string that matches part or all of a TravellerTypeDefinition description
     * @param description The full or partial description to search for
     * @return The first TravellerTypeDefinition which has a matching description, or null if no matching description found
     */
    public CompletableFuture<TravellerTypeDefinition> getTravellerTypeDefinitionByDescription(String description) {
        return supplyAsync(() -> TravellerTypeDefinition.find.query()
                .where()
                .ilike("description", "%" + description + "%")
                .findOneOrEmpty()
                .orElse(null),
            executionContext);
    }

}
