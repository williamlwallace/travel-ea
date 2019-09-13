package models.strategies.photos.user.concrete;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import models.NewsFeedResponseItem;
import models.Photo;
import models.strategies.photos.user.UserPhotoStrategy;
import play.libs.Json;
import repository.PhotoRepository;
import repository.ProfileRepository;

public class GroupedUserProfilePhotoStrategy extends UserPhotoStrategy {

    List<Long> photoIds;

    public GroupedUserProfilePhotoStrategy(Long userId,
        PhotoRepository photoRepository, ProfileRepository profileRepository,
        List<Long> photoIds) {
        super(null, userId, photoRepository, profileRepository);
        this.photoIds = photoIds;
    }

    private CompletableFuture<List<Photo>> getReferencedPhotos() {
        return photoRepository.getPhotosByIds(photoIds);
    }

    @Override
    public CompletableFuture<NewsFeedResponseItem> execute() {
        return getReferencedPhotos().thenComposeAsync(photos ->
            getUserProfileAsync().thenApplyAsync(profile -> {
                ObjectNode returnObject = new ObjectNode(new JsonNodeFactory(false));
                returnObject.set("profile", Json.toJson(profile));
                returnObject.set("photos", Json.toJson(photos));

                return new NewsFeedResponseItem(
                    String.format("just added %d photo%s!",
                        photos.size(), photos.size() == 1 ? "" : "s"),
                    String.format("%s %s", profile.firstName, profile.lastName),
                    (profile.profilePhoto == null) ? null : profile.profilePhoto.thumbnailFilename,
                    profile.userId,
                    returnObject);

            }));
    }
}
