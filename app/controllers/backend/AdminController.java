package controllers.backend;

import actions.*;
import actions.roles.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.*;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import repository.*;
import util.validation.UserValidator;
import util.validation.ErrorResponse;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Manage a database of users
 */
public class AdminController extends Controller {

    private final UserRepository userRepository;

    @Inject
    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Grant admin priveledges to user
     *
     * @param request Request object
     * @param id  Id of user to be granted
     * @return Returns a CompletionStage ok type for successful query
     */
    @With({Admin.class, Authenticator.class})
    public CompletionStage<Result> grantAdmin(Http.Request request, Long id) {
        // Run a db operation in another thread (using DatabaseExecutionContext)
        return userRepository.findID(id).thenApplyAsync(user -> {
            if (user != null) {
                user.admin = true;
                userRepository.updateUser(user);
                return ok(Json.toJson("Succefuly adminified"));
            }
            return badRequest(Json.toJson("User not found"));
        });         
    }

    /**
     * Revoke admin priveledges to user
     *
     * @param request Request object
     * @param id  Id of user to be granted
     * @return Returns a CompletionStage ok type for successful query
     */
    @With({Admin.class, Authenticator.class})
    public CompletionStage<Result> revokeAdmin(Http.Request request, Long id) {
        // Run a db operation in another thread (using DatabaseExecutionContext)
        return userRepository.findID(id).thenApplyAsync(user -> {
            if (user != null && user.username != "admin@travelea.co.nz") { //check user is not master admin
                user.admin = false;
                userRepository.updateUser(user);
                return ok(Json.toJson("Succefuly deadminified"));
            }
            return badRequest(Json.toJson("User not found"));
        });         
    }
}

