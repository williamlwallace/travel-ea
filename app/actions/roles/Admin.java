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
     * Action to pass in the desired roles into the authenticator
     * @param request HTTP request
     * @return passes request to next action with the role as an attribute
     */
    public CompletionStage<Result> call(Http.Request request) {
        List<String> roles = Authenticator.getRoles(request);
        roles.add("admin");
        return delegate.call(request.addAttr(ActionState.ROLES, roles));
    }
}