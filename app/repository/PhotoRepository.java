package repository;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.PagedList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;
import models.DestinationPhoto;
import models.Photo;
import models.PhotoTag;
import models.Tag;
import models.User;
import play.db.ebean.EbeanConfig;
import util.objects.Pair;

/**
 * A repository that executes database operations on the Photo database table.
 */
@Singleton
public class PhotoRepository {

    private static final String FRONTEND_APPEND_DIRECTORY = "../user_content/";
    private static final String USER_ID = "user_id";
    private static final String USED_FOR_PROFILE = "used_for_profile";
    private static final String IS_PUBLIC = "is_public";
    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    @Inject
    public PhotoRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext,
        TagRepository tagRepository, UserRepository userRepository) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
    }

    /**
     * Inserts new photo into database.
     *
     * @param photo Photo to be added
     * @return Ok on success
     */
    public CompletableFuture<Long> addPhoto(Photo photo) {
        return supplyAsync(() -> {
            ebeanServer.insert(photo);
            return photo.guid;
        }, executionContext);
    }

    /**
     * Gets multiple photos by multiple ids
     *
     * @param ids Ids of photos to get
     * @return Photos with given ids
     */
    public CompletableFuture<List<Photo>> getPhotosByIds(Collection<Long> ids) {
        return supplyAsync(() ->
            ebeanServer.find(Photo.class).where()
            .in("guid", ids)
            .findList()
        );
    }

    /**
     * Clears any existing profile photo for a user, and returns filenames of old files.
     *
     * @param userID user ID to clear profile photo of
     * @return A Pair of strings, the key being the filename of the photo and the value being the
     * thumbnail filename of the photo. Returns null if the user didn't have a profile photo
     */
    public CompletableFuture<Pair<String, String>> clearProfilePhoto(long userID) {
        return supplyAsync(() -> {
            Pair<String, String> returnPair;
            Photo profilePhoto = ebeanServer.find(Photo.class)
                .where()
                .eq(USER_ID, userID)
                .eq(USED_FOR_PROFILE, true)
                .findOneOrEmpty().orElse(null);
            if (profilePhoto == null) {
                return null;
            } else {
                returnPair = new Pair<>(profilePhoto.filename, profilePhoto.thumbnailFilename);
                ebeanServer.delete(profilePhoto);
                return returnPair;
            }
        });
    }

    /**
     * Finds all photos in database related to the given user ID that aren't a profile photo
     *
     * @param userID User to find all photos for
     * @return List of Photo objects with the specified user ID
     */
    public CompletableFuture<List<Photo>> getAllUserPhotos(long userID) {
        return supplyAsync(() ->
                ebeanServer.find(Photo.class)
                    .where()
                    .eq(USER_ID, userID)
                    .eq(USED_FOR_PROFILE, false)
                    .findList(),
            executionContext
        );
    }

    /**
     * Finds all public photos in database related to the given user ID that aren't a profile photo
     *
     * @param userID User to find all public photos for
     * @return List of Photo objects with the specified user ID
     */
    public CompletableFuture<List<Photo>> getAllPublicUserPhotos(long userID) {
        return supplyAsync(() ->
            ebeanServer.find(Photo.class)
                .where()
                .eq(USER_ID, userID)
                .eq(USED_FOR_PROFILE, false)
                .eq(IS_PUBLIC, true)
                .findList(),
            executionContext
        );
    }

    /**
     * Finds a page of photos in database related to the given users ID that aren't the profile photo
     *
     * @param userID User to find all photos for
     * @param pageNum Page number of photos to retrieve
     * @param pageSize Number of photos to retrieve
     * @return List of Photo objects with the specified user ID
     */
    public CompletableFuture<PagedList<Photo>> getPagedUserPhotos(long userID, Integer pageNum,
        Integer pageSize) {
        return supplyAsync(() -> {
                PagedList<Photo> photos = ebeanServer.find(Photo.class)
                    .where()
                    .eq(USER_ID, userID)
                    .eq(USED_FOR_PROFILE, false)
                    .setFirstRow((pageNum - 1) * pageSize)
                    .setMaxRows(pageSize)
                    .findPagedList();
                return appendAssetsUrl(photos);
            },
            executionContext);
    }

    /**
     * Finds a page of public photos in database related to the given user ID that aren't the profile photo
     *
     * @param userID User to find all public photos for
     * @param pageNum Page number of photos to retrieve
     * @param pageSize Number of photos to retrieve
     * @return List of Photo objects with the specified user ID
     */
    public CompletableFuture<PagedList<Photo>> getPagedPublicUserPhotos(long userID, Integer pageNum,
        Integer pageSize) {
        return supplyAsync(() -> {
                PagedList<Photo> photos = ebeanServer.find(Photo.class)
                    .where()
                    .eq(USER_ID, userID)
                    .eq(USED_FOR_PROFILE, false)
                    .eq(IS_PUBLIC, true)
                    .setFirstRow((pageNum - 1) * pageSize)
                    .setMaxRows(pageSize)
                    .findPagedList();
                return appendAssetsUrl(photos);
            },
            executionContext);
    }

    /**
     * Finds a page of photos associated with a destination
     *
     * @param userId ID of user to return private photos linked to the destination
     * @param destinationId ID of destination to retrieve photos for
     * @param pageNum Page number of photos to retrieve
     * @param pageSize Amount of photos to retrieve
     * @return A paged list of photos
     */
    public CompletableFuture<PagedList<Photo>> getDestinationPhotosForUser(long userId,
        long destinationId, Integer pageNum, Integer pageSize) {
        return supplyAsync(() -> {
                PagedList<Photo> photos = ebeanServer.find(Photo.class)
                    .where()
                    .eq("destination_id", destinationId)
                    .or()
                    .eq(IS_PUBLIC, true)
                    .eq(USER_ID, userId)
                    .setFirstRow((pageNum - 1) * pageSize)
                    .setMaxRows(pageSize)
                    .findPagedList();
                return appendAssetsUrl(photos);
            },
            executionContext);
    }

    /**
     * Finds the profile picture in the database for the given user ID.
     *
     * @param userID User to find profile picture for
     * @return a photo, which will be null if they user doesn't exist or doesn't have a profile
     * picture
     */
    public CompletableFuture<Photo> getUserProfilePicture(Long userID) {
        return supplyAsync(() -> {
            Photo photo = ebeanServer.find(Photo.class)
                .where()
                .eq(USER_ID, userID)
                .eq(USED_FOR_PROFILE, true)
                .findOneOrEmpty().orElse(null);

            if (photo != null) {
                photo.filename = FRONTEND_APPEND_DIRECTORY + photo.filename;
                photo.thumbnailFilename = FRONTEND_APPEND_DIRECTORY + photo.thumbnailFilename;
            }

            return photo;
        }, executionContext);
    }

    /**
     * Adds photos into the database. Will replace the users profile picture if needed.
     *
     * @param photos A list of photos to upload
     * @return The collection of photos now containing ID's
     */
    public CompletableFuture<List<Photo>> addPhotos(List<Photo> photos, User user) {
        List<CompletableFuture<Set<Tag>>> futures = new ArrayList<>();
        for (Photo photo : photos) {
            userRepository.updateUsedTags(user, photo);
            futures.add(tagRepository.addTags(photo.tags));
        }
        CompletableFuture<Void> allFutures = CompletableFuture
            .allOf(futures.toArray(new CompletableFuture[0]));

        //This ensures that the method can be called and the actions executed without calling
        //ThenApply/Async but also whilst allowing the tests to wait for completion before
        //Continuing execution
        return allFutures.thenApplyAsync(v -> {
            ebeanServer.insertAll(photos);
            return photos;
        }, executionContext);
    }

    /**
     * Deletes a photo from the database.
     *
     * @param id Unique photo ID of destination to be deleted
     * @return The deleted photo, or null if a photo with that id was not found
     */
    public CompletableFuture<Photo> deletePhoto(Long id) {
        return supplyAsync(() -> {
                Photo photo = ebeanServer.find(Photo.class)
                    .where()
                    .eq("guid", id)
                    .findOneOrEmpty()
                    .orElse(null);

                if (photo != null) {
                    photo.delete();
                }
                return photo;
            }
            , executionContext);
    }

    /**
     * For a list of photos, append the default assets path to them.
     *
     * @param photos Photos to append path to
     */
    public PagedList<Photo> appendAssetsUrl(PagedList<Photo> photos) {
        for (Photo photo : photos.getList()) {
            photo.filename = FRONTEND_APPEND_DIRECTORY + photo.filename;
            photo.thumbnailFilename = FRONTEND_APPEND_DIRECTORY + photo.thumbnailFilename;
        }
        return photos;
    }

    /**
     * For a list of photos, append the default assets path to them, when the photos are not paginated
     *
     * @param photos Photos to append path to
     */
    public List<Photo> appendAssetsUrlNoPage(List<Photo> photos) {
        for (Photo photo : photos) {
            photo.filename = FRONTEND_APPEND_DIRECTORY + photo.filename;
            photo.thumbnailFilename = FRONTEND_APPEND_DIRECTORY + photo.thumbnailFilename;
        }
        return photos;
    }

    /**
     * Get photo object form db.
     *
     * @param id id of photo
     */
    public CompletableFuture<Photo> getPhotoById(Long id) {
        return supplyAsync(() -> ebeanServer.find(Photo.class)
                .where()
                .eq("guid", id)
                .findOneOrEmpty()
                .orElse(null),
            executionContext);
    }

    /**
     * Get photo object from db where it has some filename.
     *
     * @param filename Name of file
     * @return true if deleted, false if not
     */
    public CompletableFuture<Boolean> deletePhotoByFilename(String filename) {
        return supplyAsync(() -> {
            Photo photo = ebeanServer.find(Photo.class)
                .where()
                .eq("filename", filename)
                .findOne();
            if (photo != null) {
                photo.delete();
                return true;
            } else {
                return false;
            }
        });
    }

    /**
     * Update photo row in database.
     *
     * @param photo photo object to update
     * @return the updated photo's guid
     */
    public CompletableFuture<Long> updatePhoto(Photo photo) {
        return supplyAsync(() -> {
                if (photo.tags.isEmpty()) {
                    ebeanServer.find(PhotoTag.class).where().eq("photo_id", photo.guid)
                        .delete();
                }
                ebeanServer.update(photo);
                return photo.guid;
            },
            executionContext);
    }

    /**
     * Gets a deleted destination photo form the database
     *
     * @param photoId The id of the photo to retrieve
     * @param destId The id of the associated destination
     * @return The destination photo, or null if not found
     */
    public CompletableFuture<DestinationPhoto> getDeletedDestPhoto(Long photoId, Long destId) {
        return supplyAsync(() -> ebeanServer.find(DestinationPhoto.class)
            .setIncludeSoftDeletes()
            .where()
            .eq("photo_id", photoId)
            .eq("destination_id", destId)
            .findOneOrEmpty()
            .orElse(null), executionContext);
    }
}
