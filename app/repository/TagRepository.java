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
    public CompletableFuture<Set<Tag>> addTags(Set<Tag> tags) {
        return supplyAsync(() -> {
            Set<String> tagNames = tags.stream().map(Tag -> Tag.name).collect(Collectors.toSet());
            Set<Tag> existingTags = ebeanServer.find(Tag.class)
                .where()
                .in("name", tagNames)
                .findSet();

            Set<Tag> tagsToAdd = new HashSet<>();
            for (Tag tag : tags) {
                if (!existingTags.stream().filter(o -> o.name.equals(tag.name)).findFirst().isPresent()) {
                    tagsToAdd.add(tag);
                }
            }

            ebeanServer.insertAll(tagsToAdd);
            existingTags.addAll(tagsToAdd);
            return existingTags;

//            Set<Tag> tagsToAdd = new HashSet<>();
//            for (Tag tag : tags) {
//                if (tag.id == null &&
//                    ebeanServer.find(Tag.class)
//                        .where().eq("name", tag.name)
//                        .findOneOrEmpty()
//                        .orElse(null) == null) {
//                    tagsToAdd.add(tag);
//                }
//            }
//
//            ebeanServer.insertAll(tagsToAdd);
//            return tagsToAdd;
        }, executionContext);
    }

    public enum TagType {
        DESTINATION_TAG,
        PHOTO_TAG,
        TRIP_TAG
    }


}
