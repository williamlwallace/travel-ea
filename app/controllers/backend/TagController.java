package controllers.backend;

import actions.ActionState;
import actions.Authenticator;
import actions.roles.Everyone;
import com.google.inject.Inject;
import models.Photo;
import models.Tag;
import models.User;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import repository.DestinationRepository;
import repository.PhotoRepository;
import repository.ProfileRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Manages tags in the database
 */
public class TagController extends TEABackController {

    private PhotoRepository photoRepository;
    private ProfileRepository profileRepository;
    private DestinationRepository destinationRepository;

    @Inject
    public TagController(PhotoRepository photoRepository,
        ProfileRepository profileRepository,
        DestinationRepository destinationRepository) {

        this.photoRepository = photoRepository;
        this.profileRepository = profileRepository;
        this.destinationRepository = destinationRepository;

    }


    /**
     * Retrieves all tags associated with a given user's photos. If the user is viewing their own profile or an admin
     * then retrieve all their private and public photo tags; else, only retrieve their public photo tags
     *
     * @param request Request to read cookie data from
     * @param id ID of the user to retrieve data from
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> getAllUserPhotoTags(Http.Request request, Long id) {
        User user = request.attrs().get(ActionState.USER);

        if (user.id.equals(id) || user.admin) {
            return photoRepository.getAllUserPhotos(id)
                    .thenApplyAsync(photos -> ok(Json.toJson(getTagsFromPhotos(photos))));
        } else {
            return photoRepository.getAllPublicUserPhotos(id)
                    .thenApplyAsync(photos -> ok(Json.toJson(getTagsFromPhotos(photos))));
        }
    }

    /**
     * Retrieves all tags from a list of user photos and returns a set of tags. Iterates through a list of photos and
     * adds each tag set for each photo to the tag set to be returned
     *
     * @param photos Array list of photo objects
     * @return a set of tags
     */
    private Set<Tag> getTagsFromPhotos(List<Photo> photos) {
        Set<Tag> tagSet = new HashSet<>();
        for (Photo photo : photos) {
            tagSet.addAll(photo.tags);
        }
        return tagSet;
    }

}
