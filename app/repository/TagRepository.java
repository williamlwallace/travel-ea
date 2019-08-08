package repository;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;
import models.Tag;
import play.db.ebean.EbeanConfig;

@Singleton
public class TagRepository {

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;
    @Inject
    public TagRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    /**
     * Adds all the new tags (tags without an id) in the list to the database
     *
     * @param tags The list of tags to add, can include existing tags, they will be ignored
     * @return The list of inserted tags
     */
    public CompletableFuture<List<Tag>> addTags(List<Tag> tags) {
        return supplyAsync(() -> {
            List<Tag> tagsToAdd = new ArrayList<>();
            for (Tag tag : tags) {
                if (tag.id == null) {
                    tagsToAdd.add(tag);
                }
            }

            ebeanServer.insertAll(tagsToAdd);
            return tagsToAdd;
        }, executionContext);
    }

    public enum TagType {
        DESTINATION_TAG,
        PHOTO_TAG,
        TRIP_TAG
    }


}
