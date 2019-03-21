package actions;

import com.typesafe.config.Config;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import util.CryptoManager;
import java.util.concurrent.CompletableFuture;

import models.User;

public class Authenticator extends Action.Simple { 

    @Inject
    private final Config config;

    public CompletableFuture<Result> call(Http.Request request) throws Throwable {
        String token = getTokenFromHeader(request);
        if (token != null) {
            return UserRepositroy.findByToken(token).thenApplyAsync((user) -> {
                if (user != null && CryptoManager.verifyToken(token, user.id, config.getString("play.http.secret.key"))) {
                    return delegate.call(request.addAttr(USER, user));
                }
                return status(401,"unauthorized");
            });
        }
    }

    private String getTokenFromHeader(Http.Request request) {
        String[] authTokenHeaderValues = request.headers().get("X-AUTH-TOKEN");
        if ((authTokenHeaderValues != null) && (authTokenHeaderValues.length == 1) && (authTokenHeaderValues[0] != null)) {
            return authTokenHeaderValues[0];
        }
        return null;
    }
}