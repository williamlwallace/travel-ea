package controllers.backend;

import actions.ActionState;
import actions.Authenticator;
import actions.roles.Admin;
import actions.roles.Everyone;
import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import models.User;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Cookie;
import play.mvc.Result;
import play.mvc.With;
import play.routing.JavaScriptReverseRouter;
import repository.UserRepository;
import util.CryptoManager;
import util.validation.ErrorResponse;
import util.validation.UserValidator;


/**
 * Manage a database of users.
 */
public class UserController extends Controller {

    private static final String SUCCESS = "Success";
    private static final String JWT_AUTH = "JWT-Auth";
    private static final String U_ID = "User-ID";
    private static final String ERR_OTHER = "other";
    private final UserRepository userRepository;
    private final HttpExecutionContext httpExecutionContext;
    private final Config config;

    @Inject
    public UserController(UserRepository userRepository,
        HttpExecutionContext httpExecutionContext,
        Config config) {
        this.userRepository = userRepository;
        this.httpExecutionContext = httpExecutionContext;
        this.config = config;
    }

    /**
     * Display the paginated list of users.
     *
     * @param request HTTP request
     * @param order Sort order (either asc or desc)
     * @param filter Filter applied on user names
     * @return Returns a CompletionStage ok type for successful query
     */
    @With({Admin.class, Authenticator.class})
    public CompletableFuture<Result> userSearch(Http.Request request, String order, String filter) {
        // Run a db operation in another thread (using DatabaseExecutionContext)
        return userRepository.search(order, filter, request.attrs().get(ActionState.USER).id).thenApplyAsync(users ->
            ok(Json.toJson(users)), httpExecutionContext.current());
    }

    /**
     * Delete a users account.
     *
     * @param request HTTP request
     * @return Ok if user successfully deleted, badrequest if no such user found
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> deleteUser(Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        return deleteUserHelper(user.id);
    }

    /**
     * Delete a user with given uid admin only.
     *
     * @param userId ID of user to delete
     * @return Ok if user successfully deleted, badrequest if no such user found
     */
    @With({Admin.class, Authenticator.class})
    public CompletableFuture<Result> deleteOtherUser(Http.Request request, Long userId) {
        if (userId != 1) { //make sure master user not deleted
            return deleteUserHelper(userId);
        } else {
            return CompletableFuture
                .supplyAsync(() -> badRequest(Json.toJson("Cannot delete Master user")));
        }
    }

    /**
     * Delete a user with given uid helper function.
     *
     * @param userId ID of user to delete
     * @return Ok if user successfully deleted, badrequest if no such user found
     */
    private CompletableFuture<Result> deleteUserHelper(Long userId) {
        return userRepository.deleteUser(userId)
            .thenApplyAsync(rowsDeleted -> (rowsDeleted > 0) ? ok(
                Json.toJson("Successfully deleted user with uid: " + userId))
                    : badRequest(Json.toJson("No user with such uid found")),
                httpExecutionContext.current());
    }

    /**
     * Logs user out.
     *
     * @param request The HTTP request sent, the body of this request should be a JSON User object
     * @return Ok
     */
    @With({Everyone.class, Authenticator.class})
    public Result logout(Http.Request request) {
        return ok(Json.toJson(SUCCESS)).discardingCookie(JWT_AUTH).discardingCookie(U_ID);
    }


    /**
     * Method to handle adding a new user to the database. The username provided must be unique, and
     * the password field must be non-empty.
     *
     * @param request The HTTP request sent, the body of this request should be a JSON User object
     * @return OK if user is successfully added to DB, badRequest otherwise
     */
    public CompletableFuture<Result> addNewUser(Http.Request request) {
        //Get the data from the request as a JSON object
        JsonNode data = request.body().asJson();
        //Sends the received data to the validator for checking
        ErrorResponse validatorResult = new UserValidator(data).login();
        //Checks if the validator found any errors in the data
        if (validatorResult.error()) {
            return CompletableFuture.supplyAsync(() -> badRequest(validatorResult.toJson()));
        } else {
            //Else, no errors found, continue with adding to the database
            //Create a new user from the request data, basing off the User class
            User newUser = Json.fromJson(data, User.class);

            //Generate a new salt for the new user
            newUser.salt = CryptoManager.generateNewSalt();

            //Generate the salted password
            newUser.password = CryptoManager
                .hashPassword(newUser.password, Base64.getDecoder().decode(newUser.salt));

            //This block ensures that the username (email) is
            // not taken already, and returns a CompletableFuture<Result>
            //Check whether the username is already in the database
            return userRepository.findUserName(newUser.username)
                //Pass that result (a User object) into the new function using thenCompose
                .thenComposeAsync(user -> {
                    if (user != null) {
                        //If a user is found pass null into the next function
                        return CompletableFuture.supplyAsync(() -> null);
                    } else {
                        //If a user is not found pass the result of insertUser (a Long) ito the next function
                        return userRepository.insertUser(newUser);
                    }
                })
                .thenApplyAsync(user -> {
                    //Num should be a uid of a new user or null, the return of this lambda is the overall return of the whole method
                    if (user == null) {
                        //Create the error to be sent to client
                        validatorResult.map("Email already in use", ERR_OTHER);
                        return badRequest(validatorResult
                            .toJson());    //If the uid is null, return a badRequest message...
                    } else {
                        if (request.header("Cookie").toString() == "Optional.empty") {
                            //If the uid is not null, return an ok message with the uid contained within
                            return ok(Json.toJson(SUCCESS))
                                    .withCookies(Cookie.builder(JWT_AUTH, createToken(user)).build());
                        } else {
                            return ok(Json.toJson(SUCCESS));
                        }
                    }
                });
        }
    }

    /**
     * Handles login attempts. A username and password must be provided as a JSON object, by default
     * this JSON object deserializes to a User object which is then compared against the database to
     * check for correct password and username etc.
     *
     * @param request HTTP request with username and password in body
     * @return OK with User JSON data on successful login, otherwise badRequest with specific error
     *  message
     */
    public CompletableFuture<Result> login(Http.Request request) {
        // Get json parameters
        JsonNode json = request.body().asJson();

        // Run the Validator
        ErrorResponse errorResponse = new UserValidator(json).login();

        if (errorResponse.error()) {
            return CompletableFuture.supplyAsync(() -> badRequest(errorResponse.toJson()));
        }

        // Find user with username on database
        return userRepository.findUserName(json.get("username").asText(""))
            .thenApplyAsync(foundUser -> {
                // If no such user was found with that username, return bad request
                if (foundUser == null) {
                    errorResponse.map("Unauthorised", ERR_OTHER);
                    return status(401, errorResponse.toJson());
                }
                // Otherwise if a user was found, check if correct password
                else {
                    // Check if password given matches hashed and salted password on db
                    if (CryptoManager
                        .checkPasswordMatch(json.get("password").asText(""), foundUser.salt,
                            foundUser.password)) {
                        return ok(Json.toJson(SUCCESS)).withCookies(
                            Cookie.builder(JWT_AUTH, createToken(foundUser)).build(),
                            Cookie.builder(U_ID, foundUser.id.toString())
                                .withHttpOnly(false)
                                .build());
                    }
                    // If password was incorrect, return bad request
                    else {
                        errorResponse.map("Unauthorised", ERR_OTHER);
                        return status(401, errorResponse.toJson());
                    }
                }
            });
    }

    /**
     * Returns user id in body and sets cookie to store it. This will be used incase a user is
     * authenticated but the user-id cookie is somehow removed.
     *
     * @param request Request object that stores details of the incoming request
     * @return user id in json
     */
    @With({Everyone.class, Authenticator.class})
    public Result setId(Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        return ok(Json.toJson(user.id)).withCookies(
            Cookie.builder(U_ID, user.id.toString())
                .withHttpOnly(false)
                .build());
    }

    /**
     * Create Token and update user with it.
     *
     * @param user User object to be updated
     * @return authToken
     */
    private String createToken(User user) {
        return CryptoManager
            .createToken(user.id, config.getString("play.http.secret.key"));
    }

    /**
     * Lists routes to put in JS router for use from frontend
     * @return JSRouter Play result
     */
    public Result userRoutes(Http.Request request) {
        return ok(
            JavaScriptReverseRouter.create("userRouter", "jQuery.ajax", request.host(),
                controllers.backend.routes.javascript.UserController.deleteOtherUser(),
                controllers.backend.routes.javascript.UserController.userSearch(),
                controllers.backend.routes.javascript.UserController.userSearch()
            )
        ).as(Http.MimeTypes.JAVASCRIPT);
    }
}