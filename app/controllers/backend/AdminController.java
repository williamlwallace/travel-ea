package controllers.backend;

import actions.Authenticator;
import actions.roles.Admin;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import repository.UserRepository;

/**
 * Manage a database of users.
 */
public class AdminController extends Controller {

    private final UserRepository userRepository;

    @Inject
    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Grant admin privileges to user.
     *
     * @param request Request object
     * @param id Id of user to be granted
     * @return Returns a CompletionStage ok type for successful query
     */
    @With({Admin.class, Authenticator.class})
    public CompletionStage<Result> grantAdmin(Http.Request request, Long id) {
        // Run a db operation in another thread (using DatabaseExecutionContext)
        return userRepository.findID(id).thenApplyAsync(user -> {
            if (user != null) {
                user.admin = true;
                userRepository.updateUser(user);
                return ok(Json.toJson("Successfully adminified"));
            }
            return badRequest(Json.toJson("User not found"));
        });
    }

    /**
     * Revoke admin privileges to user.
     *
     * @param request Request object
     * @param id Id of user to be granted
     * @return Returns a CompletionStage ok type for successful query
     */
    @With({Admin.class, Authenticator.class})
    public CompletionStage<Result> revokeAdmin(Http.Request request, Long id) {
        // Run a db operation in another thread (using DatabaseExecutionContext)
        return userRepository.findID(id).thenApplyAsync(user -> {
            if (user != null
                && user.id != 1) { //check user is not master admin
                user.admin = false;
                userRepository.updateUser(user);
                return ok(Json.toJson("Succefuly deadminified"));
            }
            return badRequest(Json.toJson("User not found"));
        });
    }
}

