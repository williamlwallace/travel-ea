package models.strategies.photos;

import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import models.Photo;
import models.strategies.NewsFeedStrategy;
import repository.PhotoRepository;

public abstract class PhotoStrategy extends NewsFeedStrategy {

    // The ID of the photo that is being referenced by this news feed event
    private Long photoId;

    // Reference to photo repo singleton for fetching photo data
    private PhotoRepository photoRepository;

    /**
     * Constructor to instantiate new photo related strategy
     * @param photoId The ID of the photo the event is referencing
     * @param photoRepository Instance of photoRepository
     */
    public PhotoStrategy(Long photoId, PhotoRepository photoRepository) {
        this.photoId = photoId;
        this.photoRepository = photoRepository;
    }

    /**
     * Returns the photo which has been referenced by id given on object construction
     * @return Completable future that will return referenced photo when allowed to complete
     */
    protected CompletableFuture<Photo> getReferencedPhotoAsync() {
        return photoRepository.getPhotoById(photoId);
    }
}
