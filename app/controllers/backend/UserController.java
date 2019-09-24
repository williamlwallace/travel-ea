package controllers.backend;

import actions.ActionState;
import actions.Authenticator;
import actions.roles.Admin;
import actions.roles.Everyone;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.inject.Inject;
import models.FollowerUser;
import models.Tag;
import models.User;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;
import play.mvc.Http.Cookie;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.With;
import play.routing.JavaScriptReverseRouter;
import repository.ProfileRepository;
import repository.UserRepository;
import util.CryptoManager;
import util.objects.PagingResponse;
import util.validation.ErrorResponse;
import util.validation.UserValidator;


/**
 * Manage a database of users.
 */
public class UserController extends TEABackController {

    private static final String SUCCESS = "Success";
    private static final String JWT_AUTH = "JWT-Auth";
    private static final String U_ID = "User-ID";
    private static final String IS_ADMIN = "Is-Admin";
    private static final String ERR_OTHER = "other";
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final HttpExecutionContext httpExecutionContext;
    private final Config config;

    @Inject
    public UserController(UserRepository userRepository,
        ProfileRepository profileRepository,
        HttpExecutionContext httpExecutionContext,
        Config config) {

        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.httpExecutionContext = httpExecutionContext;
        this.config = config;
    }

    /**
     * Display the paginated list of users.
     *
     * @param request HTTP request
     * @param searchQuery username to search by
     * @param sortBy column to sort by
     * @param ascending returns results in ascending order if true or descending order if false
     * @param pageNum page number you are on
     * @param pageSize number of results per page
     * @param requestOrder The order that this request has, allows frontend to determine what
     * results to take
     * @return a PagedList of users
     */
    @With({Admin.class, Authenticator.class})
    public CompletableFuture<Result> userSearch(Http.Request request, String searchQuery,
        String sortBy, Boolean ascending, Integer pageNum, Integer pageSize, Integer requestOrder) {

        return userRepository
            .search(request.attrs().get(ActionState.USER).id, searchQuery, sortBy, ascending,
                pageNum, pageSize)
            .thenApplyAsync(users -> {
                try {
                    return ok(sanitizeJson(Json.toJson(
                        new PagingResponse<>(users.getList(), requestOrder,
                            users.getTotalPageCount()))));
                } catch (IOException e) {
                    return internalServerError(Json.toJson(SANITIZATION_ERROR));
                }
            }, httpExecutionContext.current());
    }

    /**
     * Delete the logged in users account.
     *
     * @param request HTTP request
     * @return Ok if user successfully deleted, bad request if no such user found
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
     * @return Ok if user successfully deleted, bad request if no such user found
     */
    @With({Admin.class, Authenticator.class})
    public CompletableFuture<Result> deleteOtherUser(Http.Request request, Long userId) {
        return deleteUserHelper(userId);
    }

    /**
     * Delete a user with given uid helper function.
     *
     * @param userId ID of user to delete
     * @return Ok if user soft delete successfully toggled, otherwise appropriate error message
     * returned
     */
    private CompletableFuture<Result> deleteUserHelper(Long userId) {
        return userRepository.findDeletedID(userId).thenComposeAsync(user -> {
            if (user == null) {
                return CompletableFuture.supplyAsync(() -> notFound("No user with such uid found"));
            } else if (userId == MASTER_ADMIN_ID) {
                return CompletableFuture
                    .supplyAsync(() -> badRequest(Json.toJson("Cannot delete master user")));
            } else {
                user.deleted = !user.deleted;
                return userRepository.updateUser(user).thenComposeAsync(uid ->
                    profileRepository.getDeletedProfile(uid).thenComposeAsync(profile -> {
                        if (profile == null) {
                            return CompletableFuture.supplyAsync(() -> ok(Json.toJson(userId)));
                        }

                        // Ensures profile soft delete state matches the user state
                        profile.deleted = user.deleted;
                        return profileRepository.updateProfile(profile)
                            .thenApplyAsync(pid -> ok(Json.toJson(userId)));
                    })
                );
            }
        });
    }

    /**
     * Logs user out.
     *
     * @param request The HTTP request sent, the body of this request should be a JSON User object
     * @return Ok
     */
    @With({Everyone.class, Authenticator.class})
    public Result logout(Http.Request request) {
        return ok(Json.toJson(SUCCESS))
            .discardingCookie(JWT_AUTH)
            .discardingCookie(U_ID)
            .discardingCookie(IS_ADMIN);
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

            try {
                //Generate the salted password
                newUser.password = CryptoManager
                    .hashPassword(newUser.password, Base64.getDecoder().decode(newUser.salt));
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                return CompletableFuture.supplyAsync(() ->
                    internalServerError(Json.toJson("Error hashing password"))
                );
            }

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
                        //If a user is not found pass the result of insertUser
                        // (a Long) ito the next function
                        return userRepository.insertUser(newUser);
                    }
                })
                .thenApplyAsync(user -> {
                    //Num should be a uid of a new user or null,
                    // the return of this lambda is the overall return of the whole method
                    if (user == null) {
                        //Create the error to be sent to client
                        validatorResult.map("Email already in use", ERR_OTHER);
                        return badRequest(validatorResult
                            .toJson());    //If the uid is null, return a badRequest message...
                    } else {
                        if (request.cookies().getCookie(JWT_AUTH).orElse(null) == null) {
                            //If the auth cookie is not null,
                            // return an ok message with the uid contained within
                            return ok(Json.toJson(user.id))
                                .withCookies(Cookie.builder(JWT_AUTH, createToken(user)).build());
                        } else {
                            return ok(Json.toJson(user.id));
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
     * message
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
                    errorResponse.map("Incorrect email", ERR_OTHER);
                    return status(401, errorResponse.toJson());
                }
                // Otherwise if a user was found, check if correct password
                else {
                    // Check if password given matches hashed and salted password on db
                    try {
                        if (CryptoManager
                            .checkPasswordMatch(json.get("password").asText(""), foundUser.salt,
                                foundUser.password)) {
                            return ok(Json.toJson(SUCCESS)).withCookies(
                                Cookie.builder(JWT_AUTH, createToken(foundUser)).build(),
                                Cookie.builder(U_ID, foundUser.id.toString())
                                    .withHttpOnly(false)
                                    .build(),
                                Cookie.builder(IS_ADMIN, foundUser.admin.toString())
                                    .withHttpOnly(false)
                                    .build());
                        } // If password was incorrect, return bad request
                        else {
                            errorResponse.map("Incorrect password", ERR_OTHER);
                            return status(401, errorResponse.toJson());
                        }

                    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                        return internalServerError(Json.toJson("Error checking password!"));
                    }

                }
            });
    }

    /**
     * Returns a user with the given id.
     *
     * @param userId User id of user to retrieve name of
     * @return User object
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> getUser(Http.Request request, Long userId) {
        User loggedInUser = request.attrs().get(ActionState.USER);

        // Returns user if logged in user is admin or same user accessing user data
        if (loggedInUser.admin || loggedInUser.id.equals(userId)) {
            return userRepository.findID(userId).thenApplyAsync(user -> {
                if (user == null) {
                    return badRequest();
                } else {
                    // Builds User object replacing usedTags with tags
                    ObjectMapper mapper = new ObjectMapper();
                    final ObjectNode userNode = mapper.valueToTree(user);
                    Set<Tag> tags = user.usedTags.stream().map(usedTag -> usedTag.tag)
                        .collect(Collectors.toSet());
                    userNode.replace("usedTags", Json.toJson(tags));

                    try {
                        return ok(sanitizeJson(userNode));
                    } catch (IOException e) {
                        return internalServerError(Json.toJson(SANITIZATION_ERROR));
                    }
                }
            });
        } else {
            return CompletableFuture.supplyAsync(Results::forbidden);
        }
    }

    /**
     * Returns user id in body and sets cookie to store it. This will be used when a user is
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
     * Toggles the status whether the current user follows a use with given id
     *
     * @param request Http request contains current users id
     * @param userId id of the user to follow/unfollow
     * @return a result contain a Json of follow or unfollow
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> toggleFollowerStatus(Http.Request request, Long userId) {

        Long followerId = request.attrs().get(ActionState.USER).id;
        if (userId.equals(followerId)) {
            return CompletableFuture.supplyAsync(Results::forbidden);
        }

        return userRepository.findID(userId).thenComposeAsync(usersId -> {
            if (usersId == null) {
                return CompletableFuture.supplyAsync(Results::notFound);
            } else {
                return userRepository.getFollower(userId, followerId)
                    .thenComposeAsync(followerUser -> {
                        if (followerUser == null) {
                            FollowerUser newFollowerUser = new FollowerUser();
                            newFollowerUser.followerId = followerId;
                            newFollowerUser.userId = userId;
                            return userRepository.insertFollower(newFollowerUser)
                                .thenApplyAsync(guid ->
                                    ok(Json.toJson("followed")));
                        } else {
                            return userRepository.deleteFollower(followerUser.guid)
                                .thenApplyAsync(delete ->
                                    ok(Json.toJson("unfollowed")));
                        }
                    });
            }
        });

    }

    /**
     * Gets the following status of a user.
     *
     * @param request Http request contains current users id
     * @param userId id of the user to follow/unfollow
     * @return a result contain a Json of follow or unfollow
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> getFollowerStatus(Http.Request request, Long userId) {

        Long followerId = request.attrs().get(ActionState.USER).id;
        if (userId.equals(followerId)) {
            return CompletableFuture.supplyAsync(Results::forbidden);
        }

        return userRepository.findID(userId).thenComposeAsync(usersId -> {
            if (usersId == null) {
                return CompletableFuture.supplyAsync(Results::notFound);
            } else {
                return userRepository.getFollower(userId, followerId)
                    .thenComposeAsync(followerUser -> {
                        if (followerUser == null) {
                            //Not following
                            return CompletableFuture.supplyAsync(() -> ok(Json.toJson(false)));
                        } else {
                            //Following
                            return CompletableFuture.supplyAsync(() -> ok(Json.toJson(true)));
                        }
                    });
            }
        });
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
     * Lists routes to put in JS router for use from frontend.
     *
     * @return JSRouter Play result
     */
    public Result userRoutes(Http.Request request) {
        return ok(
            JavaScriptReverseRouter.create("userRouter", "jQuery.ajax", request.host(),
                controllers.backend.routes.javascript.UserController.deleteOtherUser(),
                controllers.backend.routes.javascript.UserController.userSearch(),
                controllers.backend.routes.javascript.UserController.getUser(),
                controllers.backend.routes.javascript.UserController.getFollowerStatus(),
                controllers.backend.routes.javascript.UserController.toggleFollowerStatus(),
                controllers.backend.routes.javascript.UserController.setId()
            )
        ).as(Http.MimeTypes.JAVASCRIPT);
    }
}