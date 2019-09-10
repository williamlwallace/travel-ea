package models.strategies.photos.destination.concrete;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.concurrent.CompletableFuture;
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
     * The method that handles executing whatever relevant code for any news feed strategy
     *
     * @return JSON node containing data that will be sent to front end
     */
    @Override
    public CompletableFuture<JsonNode> execute() {
        // getReferencedDestinationAsync() and getReferencedPhotoAsync() will be useful here
        return null;
    }
}
