package actions;

import com.typesafe.config.Config;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Action;
import play.mvc.Http.Cookie;
import javax.inject.Inject;
import util.CryptoManager;
import repository.UserRepository;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import static java.util.concurrent.CompletableFuture.supplyAsync;

import java.lang.ProcessBuilder.Redirect;

import models.User;

public class Authenticator extends Action.Simple { 

    private final Config config;
    private final UserRepository userRepository;

    @Inject
    public Authenticator(Config config, UserRepository userRepository) {
        this.config = config;
        this.userRepository = userRepository;
    }

    public CompletableFuture<Result> call(Http.Request request) {
        String token = getTokenFromCookie(request);
        if (token != null) {
            return userRepository.findByToken(token).thenComposeAsync((user) -> {
                if (user != null && CryptoManager.veryifyToken(token, user.id, config.getString("play.http.secret.key"))) {
                    return roleMatch(request, user);
                }
                return supplyAsync(() -> redirect(controllers.frontend.routes.ApplicationController.cover()));
            });
        } else if (getRoles(request).isEmpty()) {
            // if no roles specified, do nothing (for homepage)
            return delegate.call(request).toCompletableFuture();
        }
        return supplyAsync(() -> redirect(controllers.frontend.routes.ApplicationController.cover()));
    }

    /**
     * Get roles from request
     * @param request request object
     * @return list of roles. empty if none
     */
    public static List<String> getRoles(Http.Request request) {
        List<String> roles;
        try {
            roles = request.attrs().get(ActionState.ROLES);
        }
        catch (NoSuchElementException e) {
            roles = new ArrayList<>();
        }
        return roles;
    }

    /**
     * Complares user role to roles
     * @param request request object
     * @param user User object
     * @return A redirect or delegate depending on roles
     */
    private CompletionStage<Result> roleMatch(Http.Request request, User user){
        List<String> roles = Authenticator.getRoles(request);
        // if no roles have been set assume redirect to home
        if (roles.isEmpty()) {
            return supplyAsync(() -> redirect(controllers.frontend.routes.ApplicationController.home()));
        }
        // if roles set to everyone delegate
        if (roles.contains("everyone")) {
            return delegate.call(request.addAttr(ActionState.USER, user));
        }
        return supplyAsync(() -> redirect(controllers.frontend.routes.ApplicationController.cover()));
        // TODO: implement role check.
    }

    /**
     * extract token from cookie in request
     * @param request Request object
     * @return Jwt token
     */
    public static String getTokenFromCookie(Http.Request request) {
        Optional<Cookie> option = request.cookies().getCookie("JWT-Auth");
        Cookie cookie = option.orElse(null);
        return (cookie != null ? cookie.value() : null);
    }
}