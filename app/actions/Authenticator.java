package actions;

import com.typesafe.config.Config;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Action;
import javax.inject.Inject;
import util.CryptoManager;
import repository.UserRepository;
import java.util.concurrent.CompletableFuture;
import java.util.Optional;
import static java.util.concurrent.CompletableFuture.supplyAsync;

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
        String token = getTokenFromHeader(request);
        if (token != null) {
            return userRepository.findByToken(token).thenComposeAsync((user) -> {
                if (user != null && CryptoManager.veryifyToken(token, user.id, config.getString("play.http.secret.key"))) {
                    return delegate.call(request.addAttr(ActionState.USER, user));
                }
                return supplyAsync(() -> status(401,"unauthorized"));
            });
        }
        return supplyAsync(() -> status(401,"unauthorized"));
    }

    private String getTokenFromHeader(Http.Request request) {
        Optional<String> authTokenHeaderValues = request.getHeaders().get("X-AUTH-TOKEN");
        if (authTokenHeaderValues.isPresent()) {
            return authTokenHeaderValues.orElse(null);
        }
        return null;
    }
}