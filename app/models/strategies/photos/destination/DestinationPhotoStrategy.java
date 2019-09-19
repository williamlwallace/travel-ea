package models.strategies.photos.destination;

import java.util.concurrent.CompletableFuture;
import java.util.List;
import javax.inject.Inject;
import models.Destination;
import models.strategies.photos.PhotoStrategy;
import repository.DestinationRepository;
import repository.PhotoRepository;

public abstract class DestinationPhotoStrategy extends PhotoStrategy {

    // ID of the destination which this photo event is related to
    private Long destinationId;

    // Reference to destination repo singleton for fetching destination data
    private DestinationRepository destinationRepository;

    /**
     * Constructor to instantiate an event involving some photo and some destination
     * @param photoId ID of photo referenced in event
     * @param destinationId ID of destination referenced in event
     * @param photoRepository Instance of photoRepository
     * @param destinationRepository Instance of destinationRepository
     */
    public DestinationPhotoStrategy(Long photoId, Long destinationId, PhotoRepository photoRepository, DestinationRepository destinationRepository, List<Long> eventIds) {
        super(photoId, photoRepository, eventIds);
        this.destinationId = destinationId;
        this.destinationRepository = destinationRepository;
    }

    /**
     * Returns the destination which has been referenced by id given on object construction
     * @return Completable future that will return referenced destination when allowed to complete
     */
    protected CompletableFuture<Destination> getReferencedDestinationAsync() {
        return destinationRepository.getDestination(destinationId);
    }
}
