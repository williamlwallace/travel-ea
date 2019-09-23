package actions;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import com.typesafe.config.Config;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import models.User;
import play.libs.Json;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Http.Cookie;
import play.mvc.Result;
import repository.ProfileRepository;
import repository.UserRepository;
import util.CryptoManager;

public class Authenticator extends Action.Simple {

    private static final String JWT_AUTH = "JWT-Auth";
    private static final String API = "/api/";
    private static final String FORBIDDEN = "Forbidden";
    private final Config config;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    @Inject
    public Authenticator(Config config, UserRepository userRepository,
        ProfileRepository profileRepository) {
        this.config = config;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
    }

    /**
     * Get roles from request.
     *
     * @param request request object
     * @return list of roles. empty if none
     */
    public static List<String> getRoles(Http.Request request) {
        List<String> roles;
        try {
            roles = request.attrs().get(ActionState.ROLES);
        } catch (NoSuchElementException e) {
            roles = new ArrayList<>();
        }
        return roles;
    }

    /**
     * extract token from cookie in request.
     *
     * @param request Request object
     * @return Jwt token
     */
    public static String getTokenFromCookie(Http.Request request) {
        Optional<Cookie> option = request.cookies().getCookie(JWT_AUTH);
        Cookie cookie = option.orElse(null);
        return (cookie != null ? cookie.value() : null);
    }

    /**
     * Action entry point. Should only let users pass that have been authenticated to the correct
     * degree otherwise redirect or return forbidden
     *
     * @param request HTTP request
     * @return 403 or 401 if auth fails else continue and put the user in the request
     */
    @Override
    public CompletableFuture<Result> call(Http.Request request) {
        String token = getTokenFromCookie(request);
        // Check if api or not and set failure response
        Result fail;
        if (request.uri().contains(API)) {
            fail = (token == null) ? unauthorized(Json.toJson("Unauthorized"))
                : forbidden(Json.toJson(FORBIDDEN));
        } else {
            fail = redirect(controllers.frontend.routes.ApplicationController.cover())
                .discardingCookie(JWT_AUTH);
        }

        if (token != null) {
            //get the userId if authentication is authentic
            Long userId = CryptoManager
                .verifyToken(token, config.getString("play.http.secret.key"));

            if (userId != null) {
                return userRepository.findID(userId).thenComposeAsync(user -> {
                    if (user != null) {
                        return roleMatch(request, user);
                    } else {
                        // if user is no longer in database
                        return supplyAsync(() -> fail);
                    }
                });
            }
        } else if (getRoles(request).isEmpty()) {
            // if no roles specified, do nothing (for homepage)
            return delegate.call(request).toCompletableFuture();
        }
        return supplyAsync(() -> fail);
    }

    /**
     * Compares user role to roles.
     *
     * @param request request object
     * @param user User object
     * @return A redirect or delegate depending on roles
     */
    private CompletionStage<Result> roleMatch(Http.Request request, User user) {
        List<String> roles = Authenticator.getRoles(request);
        // if no roles have been set assume redirect to home
        if (roles.isEmpty()) {
            return supplyAsync(
                () -> redirect(controllers.frontend.routes.ApplicationController.home()));
        }
        // if roles set to everyone delegate
        if (roles.contains("everyone")) {
            return haveProfile(request, user, true);
        }

        //Loop through roles (this is only for future proofing for when we need more roles)
        for (String role : roles) {
            switch (role) {
                case "everyone":
                    return haveProfile(request, user, true);
                case "admin":
                    if (user.admin) {
                        return haveProfile(request, user, true);
                    }
                    break;
                case "generalUser":
                    if (!user.admin) { //add other roles when they come
                        return haveProfile(request, user, true);
                    }
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        }
        return haveProfile(request, user, false);
    }

    /**
     * Checks if user has created a profile, if not, redirects them.
     *
     * @param request Http request object
     * @param user auth-ed user obj
     * @return Redirect to profile page if fails else delegates incoming request
     */
    private CompletionStage<Result> haveProfile(Http.Request request, User user, Boolean matched) {
        return profileRepository.findID(user.id).thenComposeAsync(profile -> {
            if (!(request.uri().equals(
                controllers.frontend.routes.ProfileController.createProfileIndex().toString()) ||
                request.uri().equals(
                controllers.backend.routes.ProfileController.addNewProfile().toString()))
                && profile == null) {
                if (request.uri().contains(API)) {
                    return supplyAsync(() -> forbidden(Json.toJson(FORBIDDEN)));
                } else {
                    return supplyAsync(() -> redirect(
                        controllers.frontend.routes.ProfileController.createProfileIndex()));
                }
            }
            CompletableFuture<Result> fail;
            fail = request.uri().contains(API) ? supplyAsync(() -> forbidden(Json.toJson(FORBIDDEN))) : supplyAsync(
                () -> redirect(controllers.frontend.routes.ApplicationController.cover())
                    .discardingCookie(JWT_AUTH));
            return matched ? delegate.call(request.addAttr(ActionState.USER, user)) : fail;
        });
    }
}