package repository;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.PagedList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import models.enums.TagType;
import models.UsedTag;
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
     * Returns a collection of tags (the type of the tag is dependant on tagType given)
     * Only tags where name exactly matches search criteria are returned
     *
     * @param tagType The kind of tag to search for
     * @param name Name of tag to search for
     * @return Paged list of tags that match the search
     */
    public CompletableFuture<PagedList<?>> searchTags(TagType tagType, String name, int pageNum, int pageSize) {
        return supplyAsync(() ->
            ebeanServer.find(tagType.getClassType())
                .fetch("tag")
                .where()
                .ieq("tag.name", name)
                .setFirstRow((pageNum - 1) * pageSize)
                .setMaxRows(pageSize)
                .findPagedList()
            );
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
     * Returns a collection of tags (the type of the tag is dependant on tagType given)
     * Only tags where name exactly matches search criteria are returned
     *
     * @param tagType The kind of tag to search for
     * @param pageNum The page to receive
     * @param pageSize Number of entries on a page
     * @return Paged list of tags that match the search
     */
    public CompletableFuture<PagedList<?>> searchTags(TagType tagType, int pageNum, int pageSize) {
        return supplyAsync(() ->
            ebeanServer.find(tagType.getClassType())
                .fetch("tag")
                .where()
                .setFirstRow((pageNum - 1) * pageSize)
                .setMaxRows(pageSize)
                .findPagedList()
            );
    }

    /**
     * Gets all tags used by a user
     *
     * @param userId Id of user to receive tags for
     * @param pageNum The page to receive
     * @param pageSize Number of entries on a page
     * @return Paged list of tags that match the search
     */
    public CompletableFuture<PagedList<?>> getRecentUserTags(long userId, int pageNum, int pageSize) {
        return supplyAsync(() ->
            ebeanServer.find(UsedTag.class)
                .where()
                .eq("user_id", userId)
                .orderBy()
                .desc("timeUsed")
                .setFirstRow((pageNum - 1) * pageSize)
                .setMaxRows(pageSize)
                .findPagedList()
            );
    }

}
