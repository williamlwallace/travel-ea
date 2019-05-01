package actions.roles;

import actions.ActionState;
import actions.Authenticator;
import java.util.List;
import java.util.concurrent.CompletionStage;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

public class Admin extends Action.Simple {

    /**
     * TODO: Campbell add javadoc.
     *
     * @param request HTTP request
     * @return TODO
     */
    public CompletionStage<Result> call(Http.Request request) {
        List<String> roles = Authenticator.getRoles(request);
        roles.add("admin");
        return delegate.call(request.addAttr(ActionState.ROLES, roles));
    }
}