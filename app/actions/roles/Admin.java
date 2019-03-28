package actions.roles;

import actions.ActionState;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Action;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.NoSuchElementException;
import static java.util.concurrent.CompletableFuture.supplyAsync;

import java.util.ArrayList;

public class Admin extends Action.Simple {
    public CompletionStage<Result> call(Http.Request request) {
        List<String> roles;
        try {
            roles = request.attrs().get(ActionState.ROLES);
        }
        catch (NoSuchElementException e) {
            roles = new ArrayList<>();
        }
        roles.add("admin");
        return delegate.call(request.addAttr(ActionState.ROLES, roles));
    }
}