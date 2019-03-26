package controllers;

import controllers.backend.ProfileController;
import models.CountryDefinition;
import models.frontend.Account;
import models.Profile;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import repository.CountryDefinitionRepository;
import scala.collection.JavaConverters;
import views.html.people;
import views.html.peopleSearch;
import actions.*;
import actions.roles.*;
import play.mvc.With;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import scala.collection.JavaConversions;


/**
 * The people controller for the finding a travel partner page.
 */
public class PeopleController extends Controller {

//    private final List<Account> accounts;

    private final List<CountryDefinition> countries;
    private final ProfileController profileController;
//    private final CountryDefinitionRepository countryDefinitionRepository;
    /**
     * Used to create example data while building GUI
     */
    @Inject
    public PeopleController(CountryDefinitionRepository countryDefinitionRepository,
                            ProfileController profileController) {
//        this.countryDefinitionRepository = countryDefinitionRepository;
        this.profileController = profileController;
        countries = countryDefinitionRepository.getAllCountries().join();


//        //Test data for accounts. should be replaced with account data from database.
//        Account newUser = new Account("dave@gmail.com", "password");
//        newUser.getProfile().setFirstName("Dave");
//        newUser.getProfile().setLastName("Smith");
//        newUser.getProfile().setGender("Male");
//        newUser.getProfile().setDOB(LocalDate.of(1992, 4, 22));
//        ArrayList<String> travelerTypes = new ArrayList<>();
//        travelerTypes.add("Groupies");
//        newUser.getProfile().setTravelerTypes(travelerTypes);
//        ArrayList<String> nationalities = new ArrayList<>();
//        nationalities.add("bahraini");
//        newUser.getProfile().setNationalities(nationalities);
//
//        Account newUser2 = new Account("Lad420@gmail.com", "password");
//        newUser2.getProfile().setFirstName("Rose");
//        newUser2.getProfile().setLastName("Bagniuk");
//        newUser2.getProfile().setGender("Female");
//        newUser2.getProfile().setDOB(LocalDate.of(2000, 8, 5));
//        ArrayList<String> travelerTypes2 = new ArrayList<>();
//        travelerTypes2.add("Gap Year");
//        newUser2.getProfile().setTravelerTypes(travelerTypes2);
//        ArrayList<String> nationalities2 = new ArrayList<>();
//        nationalities2.add("cambodian");
//        nationalities2.add("djibouti");
//        nationalities2.add("irish");
//        newUser2.getProfile().setNationalities(nationalities2);
//
//        Account newUser3 = new Account("cats22@gmail.com", "password");
//        ArrayList<String> passports3 = new ArrayList<>();
//        ArrayList<String> travelerTypes3 = new ArrayList<>();
//        travelerTypes3.add("backpacker");
//        ArrayList<String> nationalities3 = new ArrayList<>();
//        nationalities3.add("Brazil");
//        newUser3.setProfile(new Profile("11", "Dane", "Lawson", "Kuschek", "Male",
//                LocalDate.of(1988, 5, 5), nationalities3, passports3, travelerTypes3));
//
//        Account newUser4 = new Account("cats22@gmail.com", "password");
//        ArrayList<String> passports4 = new ArrayList<>();
//        ArrayList<String> travelerTypes4 = new ArrayList<>();
//        travelerTypes4.add("backpacker");
//        ArrayList<String> nationalities4 = new ArrayList<>();
//        nationalities4.add("Thailand");
//        newUser4.setProfile(new Profile("11", "Francisca", "Lawson", "Cassely", "Female",
//                LocalDate.of(1994, 5, 15), nationalities4, passports4, travelerTypes4));
//
//        Account newUser5 = new Account("cats22@gmail.com", "password");
//        ArrayList<String> passports5 = new ArrayList<>();
//        ArrayList<String> travelerTypes5 = new ArrayList<>();
//        travelerTypes5.add("Functional/Business Traveller");
//        ArrayList<String> nationalities5 = new ArrayList<>();
//        nationalities5.add("Venezuela");
//        newUser5.setProfile(new Profile("11", "Ezmeralda", "Lawson", "Goshawk", "Female",
//                LocalDate.of(1976, 9, 3), nationalities5, passports5, travelerTypes5));
//
//        Account newUser6 = new Account("cats22@gmail.com", "password");
//        ArrayList<String> passports6 = new ArrayList<>();
//        ArrayList<String> travelerTypes6 = new ArrayList<>();
//        travelerTypes6.add("groupies");
//        ArrayList<String> nationalities6= new ArrayList<>();
//        nationalities6.add("Canada");
//        newUser6.setProfile(new Profile("11", "Leilah", "Lawson", "Ironmonger", "Female",
//                LocalDate.of(1987, 10, 13), nationalities6, passports6, travelerTypes6));
//
//        Account newUser7 = new Account("cats22@gmail.com", "password");
//        ArrayList<String> passports7 = new ArrayList<>();
//        ArrayList<String> travelerTypes7 = new ArrayList<>();
//        travelerTypes7.add("thrillseeker");
//        ArrayList<String> nationalities7 = new ArrayList<>();
//        nationalities7.add("Indonesia");
//        newUser7.setProfile(new Profile("11", "Darrelle", "Lawson", "Dyos", "Female",
//                LocalDate.of(1995, 8, 16), nationalities7, passports7, travelerTypes7));
//
//        Account newUser8 = new Account("cats22@gmail.com", "password");
//        ArrayList<String> passports8 = new ArrayList<>();
//        ArrayList<String> travelerTypes8 = new ArrayList<>();
//        travelerTypes8.add("thrillseeker");
//        ArrayList<String> nationalities8 = new ArrayList<>();
//        nationalities8.add("Jordan");
//        newUser8.setProfile(new Profile("11", "Fayre", "Lawson", "Sibthorp", "Other",
//                LocalDate.of(1985, 10, 9), nationalities8, passports8, travelerTypes8));
//
//        Account newUser9 = new Account("cats22@gmail.com", "password");
//        ArrayList<String> passports9 = new ArrayList<>();
//        ArrayList<String> travelerTypes9 = new ArrayList<>();
//        travelerTypes9.add("groupies");
//        ArrayList<String> nationalities9 = new ArrayList<>();
//        nationalities9.add("Honduras");
//        newUser9.setProfile(new Profile("11", "Ileana", "Lawson", "Hartus", "Female",
//                LocalDate.of(1992, 5, 18), nationalities9, passports9, travelerTypes9));
//
//        Account newUser10 = new Account("cats22@gmail.com", "password");
//        ArrayList<String> passports10 = new ArrayList<>();
//        ArrayList<String> travelerTypes10 = new ArrayList<>();
//        travelerTypes10.add("frequent weekender");
//        travelerTypes10.add("thrillseeker");
//        ArrayList<String> nationalities10 = new ArrayList<>();
//        nationalities10.add("Indonesia");
//        newUser10.setProfile(new Profile("11", "Jack", "Lawson", "Aird", "Male",
//                LocalDate.of(1996, 12, 14), nationalities10, passports10, travelerTypes10));
//
//        Account newUser11 = new Account("cats22@gmail.com", "password");
//        ArrayList<String> passports11 = new ArrayList<>();
//        ArrayList<String> travelerTypes11 = new ArrayList<>();
//        travelerTypes11.add("frequent weekender");
//        ArrayList<String> nationalities11 = new ArrayList<>();
//        nationalities11.add("Peru");
//        nationalities11.add("China");
//        newUser11.setProfile(new Profile("11", "Hallsy", "Lawson", "Wolseley", "Male",
//                LocalDate.of(1970, 10, 19), nationalities11, passports11, travelerTypes11));
//
//        this.accounts = com.google.common.collect.Lists.newArrayList(
//                newUser, newUser2, newUser3, newUser4, newUser5, newUser6, newUser7, newUser8, newUser9, newUser10, newUser11
//        );
    }

    /**
     * Displays the people page. Called with the /people URL and uses a GET request.
     * Checks that a user is logged in. Takes them to the people page if they are,
     * otherwise they are taken to the start page.
     *
     * @return displays the people or start page.
     */
    @With({Everyone.class, Authenticator.class})
    public Result index(Http.Request request) {
        String username = request.attrs().get(ActionState.USER).username;
        List<CountryDefinition> test = com.google.common.collect.Lists.newArrayList(countries);
        return ok(people.render(username, countries));
    }

    /**
     * Displays the people page. Called with the /people URL and uses a GET request.
     * Checks that a user is logged in. Takes them to the people page if they are,
     * otherwise they are taken to the start page.
     *
     * @return displays the people or start page.
     */
    @With({Everyone.class, Authenticator.class})
    public Result search(Http.Request request, Long nationalityId, String gender, int minAge, int maxAge, Long travellerTypeId) {
        String username = request.attrs().get(ActionState.USER).username;
        List<Profile> profiles = profileController.searchProfiles(nationalityId, gender, minAge, maxAge, travellerTypeId).join(); //This is so bad
        return ok(peopleSearch.render(username, profiles));
    }

}
