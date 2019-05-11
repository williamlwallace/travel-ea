package repository;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static play.mvc.Results.ok;

import com.google.common.collect.Iterables;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import models.Photo;
import play.db.ebean.EbeanConfig;
import play.mvc.Result;
import util.customObjects.Pair;

/**
 * A repository that executees database operations on the Photo database table
 */
public class PhotoRepository {

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public PhotoRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    /**
     * Inserts new photo into database
     *
     * @param photo Photo to be added
     * @return Ok on success
     */
    public CompletableFuture<Result> addPhoto(Photo photo) {
        return supplyAsync(() -> {
            ebeanServer.insert(photo);
            return ok();
        }, executionContext);
    }

    /**
     * Clears any existing profile photo for a user, and returns filenames of old files
     *
     * @param userID user ID to clear profile photo of
     * @return OK if successfully cleared existing profile pic, notFound if none found
     */
    public CompletableFuture<Pair<String, String>> clearProfilePhoto(long userID) {
        return supplyAsync(() -> {
            Pair<String, String> returnPair;
            Photo profilePhoto = ebeanServer.find(Photo.class)
                .where()
                .eq("user_id", userID)
                .eq("is_profile", true)
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
     * Finds all photos in database related to the given user ID
     *
     * @param userID User to find all photos for
     * @return List of Photo objects with the specified user ID
     */
    public CompletableFuture<List<Photo>> getAllUserPhotos(long userID) {
        return supplyAsync(() -> {
                List<Photo> photos = ebeanServer.find(Photo.class)
                    .where()
                    .eq("user_id", userID)
                    .eq("is_profile", false)
                    .findList();
                return appendAssetsUrl(photos);
            },
            executionContext);
    }

    /**
     * Finds all public photos in database related to the given user ID
     *
     * @param userID User to find all public photos for
     * @return List of Photo objects with the specified user ID
     */
    public CompletableFuture<List<Photo>> getAllPublicUserPhotos(long userID) {
        return supplyAsync(() -> {
                List<Photo> photos = ebeanServer.find(Photo.class)
                    .where()
                    .eq("user_id", userID)
                    .eq("is_public", true)
                    .eq("is_profile", false)
                    .findList();
                return appendAssetsUrl(photos);
            },
            executionContext);
    }

    /**
     * Finds the profile picture in the database for the given user ID
     *
     * @param userID User to find profile picture for
     * @return a photo
     */
    public CompletableFuture<Photo> getUserProfilePicture(Long userID) {
        return supplyAsync(() -> {
            Photo photo = ebeanServer.find(Photo.class)
                .where()
                .eq("user_id", userID)
                .eq("is_profile", true)
                .findOneOrEmpty().orElse(null);
            if (photo != null) {
                photo.filename = "../user_content/" + photo.filename;
                photo.thumbnailFilename = "../user_content/" + photo.thumbnailFilename;
            }
            return photo;
        }, executionContext);
    }

    /**
     * Adds photos into the database. Will replace the users profile picture if needed.
     *
     * @param photos A list of photos to upload
     * @return an ok response.
     */
    public CompletableFuture<Result> addPhotos(Collection<Photo> photos) {
        return supplyAsync(() -> {
            if (photos.size() == 1) {
                Photo pictureToUpload = Iterables.get(photos, 0);
                if (pictureToUpload.isProfile) {
                    ebeanServer.find(Photo.class)
                        .where()
                        .eq("user_id", pictureToUpload.userId)
                        .eq("is_profile", true)
                        .delete();
                }
            }
            ebeanServer.insertAll(photos);
            return ok();
        }, executionContext);
    }

    /**
     * Deletes a photo from the database.
     *
     * @param id Unique photo ID of destination to be deleted
     * @return The number of rows deleted
     */
    public CompletableFuture<Photo> deletePhoto(Long id) {
        return supplyAsync(() -> {
                Photo photo = ebeanServer.find(Photo.class)
                    .where()
                    .eq("guid", id)
                    .findOneOrEmpty()
                    .orElse(null);

                photo.delete();
                return photo;
            }
            , executionContext);
    }

    private List<Photo> appendAssetsUrl(List<Photo> photos) {
        for (Photo photo : photos) {
            photo.filename = "../user_content/" + photo.filename;
            photo.thumbnailFilename = "../user_content/" + photo.thumbnailFilename;
        }
        return photos;
    }

    /**
     * Get photo object form db
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
     * Update photo row in database
     *
     * @param photo photo object to update
     */
    public CompletableFuture<Long> updatePhoto(Photo photo) {
        return supplyAsync(() -> {
                ebeanServer.update(photo);
                return photo.guid;
            },
            executionContext);
    }

}
