package actions.roles;

import actions.ActionState;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

public class Everyone extends Action.Simple {

    public CompletionStage<Result> call(Http.Request request) {
        List<String> roles = new ArrayList<String>();
        roles.add("everyone");
        return delegate.call(request.addAttr(ActionState.ROLES, roles));
    }
}