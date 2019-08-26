package repository;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.PagedList;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;
import models.Tag;
import models.Taggable;
import models.UsedTag;
import models.User;
import play.db.ebean.EbeanConfig;

/**
 * A repository that executes database operations on the User table in a different execution
 * context.
 */
@Singleton
public class UserRepository {

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public UserRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    /**
     * Return a paged list of users not including provided user id.
     *
     * @param userId The user Id to ignore
     * @param searchQuery username to search by
     * @param sortBy column to sort by
     * @param ascending returns results in ascending order if true or descending order if false
     * @param pageNum page number you are on
     * @param pageSize number of results per page
     * @return a PagedList of users
     */
    public CompletableFuture<PagedList<User>> search(Long userId,
        String searchQuery, String sortBy, Boolean ascending, Integer pageNum, Integer pageSize) {

        final String cleanedSearchQuery = searchQuery == null ? "" : searchQuery;
        return supplyAsync(() ->
            ebeanServer.find(User.class)
                .where()
                .ne("id", String.valueOf(userId))
                .ilike("username", "%" + cleanedSearchQuery + "%")
                .orderBy((sortBy == null ? "id" : sortBy) + " " + (ascending ? "asc"
                    : "desc"))
                .setFirstRow((pageNum - 1) * pageSize)
                .setMaxRows(pageSize)
                .findPagedList());
    }

    /**
     * Gets the user with some id from the database, or null if no such user exists.
     *
     * @param id Unique ID of user to retrieve
     * @return User object with given ID, or null if none found
     */
    public CompletableFuture<User> findID(Long id) {
        return supplyAsync(() ->
                ebeanServer.find(User.class)
                    .where()
                    .idEq(id)
                    .findOneOrEmpty()
                    .orElse(null),
            executionContext);
    }

    /**
     * Gets the deleted user with some id from the database, or null if no such user exists.
     *
     * @param id Unique ID of user to retrieve
     * @return User object with given ID, or null if none found
     */
    public CompletableFuture<User> findDeletedID(Long id) {
        return supplyAsync(() ->
                ebeanServer.find(User.class)
                    .setIncludeSoftDeletes()
                    .where()
                    .idEq(id)
                    .findOneOrEmpty()
                    .orElse(null),
            executionContext);
    }

    /**
     * Find a user with a given username, if one exists, otherwise returns null.
     *
     * @param username Username to search for matching account
     * @return User with username, or null if none found
     */
    public CompletableFuture<User> findUserName(String username) {
        return supplyAsync(() ->
            ebeanServer.find(User.class).where().eq("username", username).findOneOrEmpty()
                .orElse(null));
    }

    /**
     * Update User with user object.
     *
     * @param updatedUser User object
     * @return uid of updated user
     */
    public CompletableFuture<Long> updateUser(User updatedUser) {
        return supplyAsync(() -> {
            ebeanServer.saveAll(updatedUser.usedTags);
            ebeanServer.update(updatedUser);
            return updatedUser.id;
        }, executionContext);
    }

    /**
     * Insert New user.
     *
     * @param newUser User object with new user details.
     * @return uid of new user
     */
    public CompletableFuture<User> insertUser(User newUser) {
        return supplyAsync(() -> {
            newUser.creationDate = LocalDateTime.now();
            ebeanServer.insert(newUser);
            ebeanServer.saveAll(newUser.usedTags);
            return newUser;
        }, executionContext);
    }

    /**
     * Remove a user from db.
     *
     * @param id uid of user
     * @return the number of rows that were deleted
     */
    public CompletableFuture<Integer> deleteUser(Long id) {
        return supplyAsync(() ->
                ebeanServer.delete(User.class, id)
            , executionContext);
    }

    /**
     * Updates this users tags, updating the date of the tag or inserting a new tag. Then calls
     * ebean to commit the changes to the database Compares the original object to the new object to
     * find newly added tags. Tags must already be in the database
     *
     * Use this method signature when the user is updating the object.
     *
     * @param oldTaggable The original tagged object before this user's changes
     * @param newTaggable The new tagged object after this user's changes
     */
    public void updateUsedTags(User user, Taggable oldTaggable, Taggable newTaggable) {
        user.updateUserTags(oldTaggable, newTaggable);
        for (UsedTag usedTag : user.usedTags) {
            if (usedTag.tag != null && usedTag.tag.id == null) {
                usedTag.tag = ebeanServer.find(Tag.class)
                    .where()
                    .eq("name", usedTag.tag.name)
                    .findOneOrEmpty()
                    .orElse(null);
            }
        }
        ebeanServer.saveAll(user.usedTags);
    }

    /**
     * Updates this users tags, updating the date of the tag or inserting a new tag. Then calls
     * ebean to commit the changes to the database Compares the original object to the new object to
     * find newly added tags.
     *
     * Use this method signature when the user is inserting/adding the object.
     *
     * @param taggable The original tagged object before this user's changes
     */
    public void updateUsedTags(User user, Taggable taggable) {
        user.updateUserTags(taggable);
        for (UsedTag usedTag : user.usedTags) {
            if (usedTag.tag != null && usedTag.tag.id == null) {
                usedTag.tag = ebeanServer.find(Tag.class)
                    .where()
                    .eq("name", usedTag.tag.name)
                    .findOneOrEmpty()
                    .orElse(null);
            }
        }
        ebeanServer.saveAll(user.usedTags);
    }
}