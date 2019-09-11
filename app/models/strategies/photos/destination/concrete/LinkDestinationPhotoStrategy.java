package models.strategies.photos.destination.concrete;

import java.util.concurrent.CompletableFuture;
import models.NewsFeedResponseItem;
import models.strategies.photos.destination.DestinationPhotoStrategy;
import models.strategies.photos.destination.UserDestinationPhotoStrategy;
import repository.DestinationRepository;
import repository.PhotoRepository;
import repository.ProfileRepository;

public class LinkDestinationPhotoStrategy extends UserDestinationPhotoStrategy {

    /**
     * Constructor to instantiate an event involving linking a public photo to a destination
     *
     * @param photoId ID of photo referenced in event
     * @param destinationId ID of destination referenced in event
     * @param userId ID of user referenced in event
     * @param photoRepository Instance of photoRepository
     * @param destinationRepository Instance of destinationRepository
     */
    public LinkDestinationPhotoStrategy(Long photoId, Long destinationId, Long userId,
        PhotoRepository photoRepository, DestinationRepository destinationRepository,
        ProfileRepository profileRepository) {
        super(photoId, destinationId, userId, photoRepository, destinationRepository,
            profileRepository);
    }

    /**
     * The method that handles executing the destination photo linking message for the
     * newsfeed strategy
     *
     * @return JSON node containing data that will be sent to front end
     */
    @Override
    public CompletableFuture<NewsFeedResponseItem> execute() {
        return getUserProfileAsync().thenComposeAsync(profile ->
            getReferencedPhotoAsync().thenComposeAsync(photo ->
                getReferencedDestinationAsync().thenApplyAsync(destination ->
                    new NewsFeedResponseItem(
                        String.format("just linked a photo to the destination %s!",
                        destination.name),
                        String.format("%s %s", profile.firstName, profile.lastName),
                        profile.profilePhoto.thumbnailFilename,
                        photo)
                )
            )
        );
    }
}
