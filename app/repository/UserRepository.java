package repository;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import models.User;
import play.db.ebean.EbeanConfig;

/**
 * A repository that executes database operations on the User table in a different execution
 * context.
 */
public class UserRepository {

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public UserRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    /**
     * Return a paged list of user.
     *
     * @param order Sort order (either or asc or desc)
     * @param filter Filter applied on the name column
     */
    public CompletableFuture<List<User>> search(String order, String filter) {
        return supplyAsync(() ->
                ebeanServer.find(User.class)
                    .where()
                    .ilike("username", "%" + filter + "%")
                    .orderBy("username " + order)
                    .findList(),
            executionContext);
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
            ebeanServer.insert(newUser);
            return newUser;
        }, executionContext);
    }

    /**
     * remove a user from db.
     *
     * @param id uid of user
     * @return Ok result object in a completableFuture
     */
    public CompletableFuture<Integer> deleteUser(Long id) {
        return supplyAsync(() ->
                ebeanServer.delete(User.class, id)
            , executionContext);
    }
}