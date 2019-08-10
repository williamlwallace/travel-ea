package repository;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
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
     * Adds all the new tags in the list to the database
     *
     * @param tags The list of tags to add, can include existing tags, they will be ignored
     * @return The list of inserted tags
     */
    CompletableFuture<Set<Tag>> addTags(Set<Tag> tags) {
        return supplyAsync(() -> {
            // Finds a set of tags already in the database which have matching names
            Set<String> tagNames = tags.stream().map(tag -> tag.name).collect(Collectors.toSet());
            Set<Tag> existingTags = ebeanServer.find(Tag.class)
                .where()
                .in("name", tagNames)
                .findSet();

            // Creates a set of tags which need to be inserted into the database
            Set<Tag> tagsToAdd = new HashSet<>();
            for (Tag tag : tags) {
                if (existingTags.stream().noneMatch(obj -> obj.name.equals(tag.name))) {
                    tagsToAdd.add(tag);
                }
            }

            // Inserts new tags into database and adds the new tag objects with ID's to the set of existing tags
            ebeanServer.insertAll(tagsToAdd);
            existingTags.addAll(tagsToAdd);
            return existingTags;
        }, executionContext);
    }

    /**
     * Enum of types of tags
     */
    public enum TagType {
        DESTINATION_TAG,
        PHOTO_TAG,
        TRIP_TAG
    }
}
