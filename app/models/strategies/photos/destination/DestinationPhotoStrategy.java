package models.strategies.photos.destination;

import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import models.Destination;
import models.strategies.photos.PhotoStrategy;
import repository.DestinationRepository;

public abstract class DestinationPhotoStrategy extends PhotoStrategy {

    // ID of the destination which this photo event is related to
    private Long destinationId;

    // Reference to destination repo singleton for fetching destination data
    @Inject
    private DestinationRepository destinationRepository;

    /**
     * Constructor to instantiate an event involving some photo and some destination
     * @param photoId ID of photo referenced in event
     * @param destinationId ID of destination referenced in event
     */
    public DestinationPhotoStrategy(Long photoId, Long destinationId) {
        super(photoId);
        this.destinationId = destinationId;
    }

    /**
     * Returns the destination which has been referenced by id given on object construction
     * @return Completable future that will return referenced destination when allowed to complete
     */
    protected CompletableFuture<Destination> getReferencedDestinationAsync() {
        return destinationRepository.getDestination(destinationId);
    }
}
