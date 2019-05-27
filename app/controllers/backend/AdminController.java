package controllers.backend;

import actions.Authenticator;
import actions.roles.Admin;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import play.routing.JavaScriptReverseRouter;
import repository.UserRepository;

/**
 * Manage a database of users.
 */
public class AdminController extends TEABackController {

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
                return ok(Json.toJson("Successfully promoted user to admin"));
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
                && user.id != MASTER_ADMIN_ID) { //check user is not master admin
                user.admin = false;
                userRepository.updateUser(user);
                return ok(Json.toJson("Successfully demoted user from admin"));
            }
            return badRequest(Json.toJson("User not found"));
        });
    }

    /**
     * Lists routes to put in JS router for use from frontend.
     *
     * @return JSRouter Play result
     */
    public Result adminRoutes(Http.Request request) {
        return ok(
            JavaScriptReverseRouter.create("adminRouter", "jQuery.ajax", request.host(),
                controllers.backend.routes.javascript.AdminController.revokeAdmin(),
                controllers.backend.routes.javascript.AdminController.grantAdmin()
            )
        ).as(Http.MimeTypes.JAVASCRIPT);
    }
}

