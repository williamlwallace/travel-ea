package controllers.backend;

import actions.ActionState;
import actions.Authenticator;
import actions.roles.Admin;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import models.User;
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
     * Toggles users admin privileges.
     *
     * @param request Request object
     * @param id Id of user to be granted
     * @return Returns a CompletionStage ok type for successful query
     */
    @With({Admin.class, Authenticator.class})
    public CompletionStage<Result> toggleAdmin(Http.Request request, Long id) {
        User loggedInUser = request.attrs().get(ActionState.USER);
        // Run a db operation in another thread (using DatabaseExecutionContext)
        return userRepository.findID(id).thenComposeAsync(user -> {
            if (user == null) {
                return CompletableFuture.supplyAsync(() -> notFound("This user does not exist"));
            } else if (!loggedInUser.admin) {
                return CompletableFuture.supplyAsync(() -> forbidden("You do not have permission to toggle this user's admin privileges"));
            } else {
                user.admin = !user.admin;
                return userRepository.updateUser(user).thenApplyAsync(userId ->
                    ok(Json.toJson(userId)));
            }
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
                controllers.backend.routes.javascript.AdminController.toggleAdmin()
            )
        ).as(Http.MimeTypes.JAVASCRIPT);
    }
}

