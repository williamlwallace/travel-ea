package controllers.frontend;

import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;

/**
 * Manage a database of users
 */
public class UserController extends Controller {


    @Inject
    public UserController() {
    }

    /**
     * Displays the signUp page to the user
     * @return An ok response with the form to be rendered to the client contained within
     */
    public Result signUp() {
        return ok(views.html.signUpForm.render());
    }

    /**
     * Displays the login page to the user
     * @return An ok response with the form to be rendered to the client contained within
     */
    public Result login() {
        return ok(views.html.loginForm.render());
    }

    /**
     * Displays the create profile page to the user
     * @return An ok response with the form to be rendered to the client contained within
     */
    public Result addNewProfile() {
        return ok(views.html.createProfileForm.render());
    }

    public Result viewProfile(Long uid) {
        return ok(views.html.viewProfileForm.render(uid));
    }

    public Result index() {return ok(views.html.index.render());}

}
