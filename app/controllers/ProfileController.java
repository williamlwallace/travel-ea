package controllers;

import models.frontend.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.mvc.*;

import views.html.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static play.libs.Scala.asScala;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's profile page.
 */
public class ProfileController extends Controller {

    private final Form<ProfileData> form;
    private MessagesApi messagesApi;
    private final List<Profile> profiles;

    private final Logger logger = LoggerFactory.getLogger(getClass()) ;

    @Inject
    public ProfileController(FormFactory formFactory, MessagesApi messagesApi) {
        ArrayList<String> nationalities = new ArrayList<String>();
        nationalities.add("New Zealander");
        ArrayList<String> passports = new ArrayList<String>();
        passports.add("New Zealand");
        ArrayList<String> travelerTypes = new ArrayList<String>();
        travelerTypes.add("Thrill Seeker");
        travelerTypes.add("Backpacker");
        this.form = formFactory.form(ProfileData.class);
        this.messagesApi = messagesApi;
        String time = getCurrentTimeUsingDate();
        this.profiles = com.google.common.collect.Lists.newArrayList(
                new Profile(time, "Claudia", "Rachel", "Field", "Female", LocalDate.of(1998, 1, 10), nationalities, passports, travelerTypes),
                new Profile(time, "Jane", null, "Doe", "Female", LocalDate.of(1995, 3, 8), nationalities, passports, travelerTypes)
                );
    }

    /**
     * Displays the profile page. Called with the /profile URL and uses a GET request.
     * Checks that a user is logged in. Takes them to the profile page if they are,
     * otherwise they are taken to the start page.
     *
     * @return displays the profile or start page.
     */
    public Result index(Http.Request request) {
        return request.session()
                .getOptional("connected")
                .map(user -> ok(profile.render(asScala(profiles), form, user, request, messagesApi.preferred(request))))
                .orElseGet(() -> redirect(controllers.frontend.routes.ApplicationController.cover()));
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
     * Uses a POST request at /profile to validate creating a profile.
     * If the account details are in the database. The user will be logged in and taken to the home page
     *
     * @param request
     * @return a http result; a redirect if the user credentials are correct, and a bad request in other cases.
     */
    public Result createProfile(Http.Request request) {
        final Form<ProfileData> boundForm = form.bindFromRequest(request);

        if (boundForm.hasErrors()) {
            logger.error("errors = {}", boundForm.errors());
            return badRequest(views.html.profile.render(asScala(profiles), boundForm, "user", request, messagesApi.preferred(request)));
        } else {
            ProfileData data = boundForm.get();
            String time = getCurrentTimeUsingDate();
            profiles.add(new Profile(time, data.getFirstName(), data.getMiddleName(), data.getLastName(), data.getGender(), data.getDOB(), data.getNationalities(), data.getPassports(), data.getTravelerTypes()));
            return redirect(controllers.frontend.routes.ApplicationController.home());
        }
    }

}

