package repository;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static play.mvc.Results.notFound;
import static play.mvc.Results.ok;

import com.google.common.collect.Iterables;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;
import models.DestinationPhoto;
import models.Photo;
import play.db.ebean.EbeanConfig;
import play.mvc.Result;
import util.objects.Pair;

/**
 * A repository that executes database operations on the Photo database table.
 */
@Singleton
public class PhotoRepository {

    public static final String FRONTEND_APPEND_DIRECTORY = "../user_content/";
    private static final String USER_ID = "user_id";
    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public PhotoRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    /**
     * Inserts new photo into database.
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
     * Finds all photos in database related to the given user ID.
     *
     * @param userID User to find all photos for
     * @return List of Photo objects with the specified user ID
     */
    public CompletableFuture<List<Photo>> getAllUserPhotos(long userID) {
        return supplyAsync(() -> {
                List<Photo> photos = ebeanServer.find(Photo.class)
                    .where()
                    .eq(USER_ID, userID)
                    .eq("used_for_profile", false)
                    .findList();
                return appendAssetsUrl(photos);
            },
            executionContext);
    }

    /**
     * Finds all public photos in database related to the given user ID.
     *
     * @param userID User to find all public photos for
     * @return List of Photo objects with the specified user ID
     */
    public CompletableFuture<List<Photo>> getAllPublicUserPhotos(long userID) {
        return supplyAsync(() -> {
                List<Photo> photos = ebeanServer.find(Photo.class)
                    .where()
                    .eq(USER_ID, userID)
                    .eq("used_for_profile", false)
                    .eq("is_public", true)
                    .findList();
                return appendAssetsUrl(photos);
            },
            executionContext);
    }

    /**
     * Adds photos into the database. Will replace the users profile picture if needed.
     *
     * @param photos A list of photos to upload
     */
    public void addPhotos(Collection<Photo> photos) {
        ebeanServer.insertAll(photos);
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

    /**
     * For a list of photos, append the default assets path to them.
     *
     * @param photos Photos to append path to
     */
    public List<Photo> appendAssetsUrl(List<Photo> photos) {
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
     */
    public CompletableFuture<Photo> getPhotoByFilename(String filename) {
        return supplyAsync(() -> ebeanServer.find(Photo.class)
                .where()
                .eq("filename", filename)
                .findOneOrEmpty()
                .orElse(null),
            executionContext);

    }

    /**
     * Get photo object from db where it has some filename.
     *
     * @param filename Name of file
     */
    public CompletableFuture<Boolean> deletePhotoByFilename(String filename) {
        return supplyAsync(() -> {
            Photo photo = ebeanServer.find(Photo.class)
                .where()
                .eq("filename", filename)
                .findOne();
            if(photo != null) {
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
     */
    public CompletableFuture<Long> updatePhoto(Photo photo) {
        return supplyAsync(() -> {
                ebeanServer.update(photo);
                return photo.guid;
            },
            executionContext);
    }

    public CompletableFuture<DestinationPhoto> getDeletedDestPhoto(Long photoId, Long destId) {
        return supplyAsync(() -> ebeanServer.find(DestinationPhoto.class)
            .setIncludeSoftDeletes()
            .where()
            .eq("photo_id", photoId)
            .eq("destination_id", destId)
            .findOneOrEmpty()
            .orElse(null), executionContext);
    };
}
