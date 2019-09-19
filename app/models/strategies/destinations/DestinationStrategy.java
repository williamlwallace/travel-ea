package models.strategies.destinations;

import java.util.concurrent.CompletableFuture;
import java.util.List;
import models.Destination;
import models.Photo;
import models.strategies.NewsFeedStrategy;
import repository.DestinationRepository;
import repository.PhotoRepository;

public abstract class DestinationStrategy extends NewsFeedStrategy {

    // The ID of the destination that is being referenced by this news feed event
    private Long destId;

    // Reference to destination repo singleton for fetching photo data
    private DestinationRepository destinationRepository;

    /**
     * Constructor to instantiate strategy related to some destination
     *
     * @param destId ID of destination in event
     * @param destinationRepository Reference to the destination repository
     */
    public DestinationStrategy(Long destId, DestinationRepository destinationRepository, List<Long> eventIds) {
        super(eventIds);
        this.destId = destId;
        this.destinationRepository = destinationRepository;
    }

    /**
     * Returns the destination which has been referenced by id given on object construction
     * @return Completable future that will return referenced destination when allowed to complete
     */
    protected CompletableFuture<Destination> getReferencedDestinationAsync() {
        return destinationRepository.getDestination(destId);
    }
}
