package models.strategies.photos.destination.concrete;

import java.util.concurrent.CompletableFuture;
import models.NewsFeedResponseItem;
import models.strategies.photos.destination.DestinationPhotoStrategy;
import repository.DestinationRepository;
import repository.PhotoRepository;

public class NewPrimaryDestinationPhotoStrategy extends DestinationPhotoStrategy {

    /**
     * Constructor to instantiate an event involving setting new primary photo of destination
     * @param photoId ID of photo referenced in event
     * @param destinationId ID of destination referenced in event
     * @param photoRepository Instance of photoRepository
     * @param destinationRepository Instance of destinationRepository
     */
    public NewPrimaryDestinationPhotoStrategy(Long photoId, Long destinationId,
        PhotoRepository photoRepository,
        DestinationRepository destinationRepository) {
        super(photoId, destinationId, photoRepository, destinationRepository);
    }

    /**
     * Execution method to handle the event where a destination has its primary photo updated
     *
     * @return NewsFeedResponseItem containing a user friendly message, and the photo object that has been set as primary
     */
    @Override
    public CompletableFuture<NewsFeedResponseItem> execute() {
        return getReferencedDestinationAsync().thenComposeAsync(destination ->
            getReferencedPhotoAsync().thenApplyAsync(photo ->
                new NewsFeedResponseItem(String.format("The destination '%s' has a new primary photo", destination.name),
                    photo)
            )
        );
    }
}
