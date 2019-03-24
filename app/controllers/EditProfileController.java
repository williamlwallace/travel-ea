package controllers;

import actions.*;
import actions.roles.*;
import models.frontend.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.mvc.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

import static play.libs.Scala.asScala;

/**
 * Processes user profile to populate a form to be edited
 */
@Singleton
public class EditProfileController extends Controller {

    private final Form<ProfileData> form;
    private MessagesApi messagesApi;
    private Profile profile;

    private final Logger logger = LoggerFactory.getLogger(getClass()) ;

    @Inject
    public EditProfileController(FormFactory formFactory, MessagesApi messagesApi) {
        this.form = formFactory.form(ProfileData.class);
        this.messagesApi = messagesApi;
        ArrayList<String> nationalities = new ArrayList<String>();
        nationalities.add("swedish");
        ArrayList<String> passports = new ArrayList<String>();
        passports.add("Sweden");
        ArrayList<String> travelerTypes = new ArrayList<String>();
        travelerTypes.add("groupies");
        String time = getCurrentTimeUsingDate();
        this.profile = new Profile(time, "Karola", "Kiley", "Le Fevre", "Female",
                LocalDate.of(1998, 1, 10), nationalities, passports, travelerTypes);
    }

    /**
     * function to get the current date and time to be stored with the created profile
     */
    public static String getCurrentTimeUsingDate() {
        Date date = new Date();
        String strDateFormat = "hh:mm:ss a";
        DateFormat dateFormat = new SimpleDateFormat(strDateFormat);
        String formattedDate= dateFormat.format(date);
        return formattedDate;
    }

    /**
     * Uses a POST request at /editProfile to validate and update a profile.
     *
     * @param request
     * @return a http result; a redirect if the user credentials are correct, and a bad request in other cases.
     */
    @With({Everyone.class, Authenticator.class})
    public Result index(Http.Request request) {
        String username = request.attrs().get(ActionState.USER).username;
        return ok(views.html.editProfile.render(profile, form, username, request, messagesApi.preferred(request)));
    }

    public Result updateProfile(Http.Request request) {
        final Form<ProfileData> boundForm = form.bindFromRequest(request);

        if (boundForm.hasErrors()) {
            logger.error("errors = {}", boundForm.errors());
            return index(request);
        } else {
            ProfileData data = boundForm.get();
            // This is where the update profile data will be delt with
            // profile = new Profile(data.getFirstName(), data.getMiddleName(), data.getLastName(), data.getGender(), data.getNationalities(), data.getPassports(), data.getTravelerTypes());
            return redirect(routes.EditProfileController.index())
                    .flashing("info", "Profile Updated\n");
        }
    }
}