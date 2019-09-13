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
        List<Long> photoIds, List<Long> eventIds) {
        super(null, userId, photoRepository, profileRepository, eventIds);
        System.out.println(photoIds);
        System.out.println(userId);
        this.photoIds = photoIds;
    }

    private CompletableFuture<List<Photo>> getReferencedPhotos() {
        return photoRepository.getPhotosByIds(photoIds);
    }

    @Override
    public CompletableFuture<NewsFeedResponseItem> execute() {
        return getReferencedPhotos().thenComposeAsync(photos ->
            getUserProfileAsync().thenApplyAsync(profile -> {
                System.out.println(eventIds);
                ObjectNode returnObject = new ObjectNode(new JsonNodeFactory(false));
                returnObject.set("profile", Json.toJson(profile));
                returnObject.set("photos", Json.toJson(photos));

                System.out.println(eventIds);

                return new NewsFeedResponseItem(
                    String.format("just added %d photo%s!",
                        photos.size(), photos.size() == 1 ? "" : "s"),
                    String.format("%s %s", profile.firstName, profile.lastName),
                    profile.profilePhoto,
                    profile.userId,
                    returnObject,
                    eventIds);

            }));
    }
}
