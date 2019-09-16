package models.strategies.photos.destination.concrete;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import models.NewsFeedResponseItem;
import models.Photo;
import models.strategies.photos.destination.UserDestinationPhotoStrategy;
import play.libs.Json;
import repository.DestinationRepository;
import repository.PhotoRepository;
import repository.ProfileRepository;

public class GroupedLinkDestinationPhotoStrategy extends UserDestinationPhotoStrategy {

    List<Long> photoIds;

    public GroupedLinkDestinationPhotoStrategy(Long destinationId, Long userId,
        PhotoRepository photoRepository,
        DestinationRepository destinationRepository,
        ProfileRepository profileRepository, List<Long> photoIds) {
        super(null, destinationId, userId, photoRepository, destinationRepository,
            profileRepository);
        this.photoIds = photoIds;
    }

    private CompletableFuture<List<Photo>> getReferencedPhotos() {
        return photoRepository.getPhotosByIds(photoIds);
    }

    @Override
    public CompletableFuture<NewsFeedResponseItem> execute() {
        return getReferencedPhotos().thenComposeAsync(photos ->
            getUserProfileAsync().thenComposeAsync(profile ->

                getReferencedDestinationAsync().thenApplyAsync(destination -> {
                    ObjectNode returnObject = new ObjectNode(new JsonNodeFactory(false));
                    returnObject.set("destination", Json.toJson(destination));
                    returnObject.set("photos", Json.toJson(photos));

                    return new NewsFeedResponseItem(
                        String.format("just linked %d photo%s to the destination %s!",
                            photos.size(), photos.size() == 1 ? "" : "s", destination.name),
                        String.format("%s %s", profile.firstName, profile.lastName),
                        profile.profilePhoto,
                        profile.userId,
                        returnObject);

                })));
    }
}
