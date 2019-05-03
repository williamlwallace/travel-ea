package repository;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import models.Photo;
import play.db.ebean.EbeanConfig;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static play.mvc.Results.ok;

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
     *  Inserts new photo into database
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
     * Finds all photos in database related to the given user ID
     *
     * @param userID User to find all photos for
     * @return List of Photo objects with the specified user ID
     */
    public CompletableFuture<List<Photo>> getAllUserPhotos(long userID) {
        return supplyAsync(() ->
             ebeanServer.find(Photo.class)
                    .where()
                    .eq("user_id", userID)
                    .findList(),
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
            if(photo != null) {
                photo.filename = "assets/" + photo.filename;
                photo.thumbnailFilename = "assets/" + photo.thumbnailFilename;
            }
            return photo;
        }, executionContext);
    }

    public CompletableFuture<Result> addPhotos(Collection<Photo> photos) {
        return supplyAsync(() -> {
            ebeanServer.insertAll(photos);
            return ok();
        }, executionContext);
    }

    private Collection<Photo> appendAssetsUrl(Collection<Photo> photos) {
        for(Photo photo : photos) {
            photo.filename = "assets/" + photo.filename;
            photo.thumbnailFilename = "assets/" + photo.thumbnailFilename;
        }
        return photos;
    }
}
