package actions.roles;

import actions.ActionState;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Action;
import java.util.List;
import java.util.concurrent.CompletionStage;
import static java.util.concurrent.CompletableFuture.supplyAsync;

import java.util.ArrayList;

public class Everyone extends Action.Simple {
    public CompletionStage<Result> call(Http.Request request) {
        List<String> roles = new ArrayList<String>();
        roles.add("everyone");
        return delegate.call(request.addAttr(ActionState.ROLES, roles));
    }
}