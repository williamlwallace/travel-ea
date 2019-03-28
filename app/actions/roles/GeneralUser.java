package actions.roles;

import actions.*;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Action;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.NoSuchElementException;
import static java.util.concurrent.CompletableFuture.supplyAsync;

import java.util.ArrayList;

public class GeneralUser extends Action.Simple {
    public CompletionStage<Result> call(Http.Request request) {
        List<String> roles = Authenticator.getRoles(request);
        roles.add("generalUser");
        return delegate.call(request.addAttr(ActionState.ROLES, roles));
    }
}