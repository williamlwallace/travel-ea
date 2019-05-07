package controllers.frontend;

import actions.ActionState;
import actions.Authenticator;
import actions.roles.Everyone;
import controllers.backend.ProfileController;
import java.util.List;
import javax.inject.Inject;
import models.Profile;
import models.User;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import views.html.people;


/**
 * The people controller for the finding a travel partner page.
 */
public class PeopleController extends Controller {

    private final ProfileController profileController;

    /**
     * Used to create example data while building GUI.
     */
    @Inject
    public PeopleController(ProfileController profileController) {

        this.profileController = profileController;

    }


    /**
     * Displays the people page. Called with the /people URL and uses a GET request. Checks that a
     * user is logged in. Takes them to the people page if they are, otherwise they are taken to the
     * start page.
     *
     * @return displays the people or start page.
     */
    @With({Everyone.class, Authenticator.class})
    public Result search(Http.Request request, Long nationalityId, String gender, int minAge,
        int maxAge, Long travellerTypeId) {
        User user = request.attrs().get(ActionState.USER);
        List<Profile> profiles = profileController
            .searchProfiles(request, nationalityId, gender, minAge, maxAge, travellerTypeId)
            .join(); //This is so bad
        return ok(people.render(user, profiles));
    }
}
