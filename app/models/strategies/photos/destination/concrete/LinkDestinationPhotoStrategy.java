package models.strategies.photos.destination.concrete;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.concurrent.CompletableFuture;
import models.strategies.photos.destination.DestinationPhotoStrategy;

public class LinkDestinationPhotoStrategy extends DestinationPhotoStrategy {

    /**
     * Constructor to instantiate an event for linking a photo to a destination
     * @param photoId ID of photo referenced in event
     * @param destinationId ID of destination referenced in event
     */
    public LinkDestinationPhotoStrategy(Long photoId, Long destinationId) {
        super(photoId, destinationId);
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
