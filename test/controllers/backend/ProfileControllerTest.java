package controllers.backend;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.CountryDefinition;
import models.Profile;
import models.TravellerTypeDefinition;
import org.junit.*;
import play.Application;
import play.db.Database;
import play.db.evolutions.Evolutions;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;
import util.validation.ErrorResponse;
import util.validation.UserValidator;
import play.mvc.Http.Cookie;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

public class ProfileControllerTest extends WithApplication {

    private static Application fakeApp;
    private static Database db;
    private static Cookie authCookie;

    /**
     * Configures system to use profile database, and starts a fake app
     */
    @BeforeClass
    public static void setUp() {
        // Create custom settings that change the database to use test database instead of production
        Map<String, String> settings = new HashMap<>();
        settings.put("db.default.driver", "org.h2.Driver");
        settings.put("db.default.url", "jdbc:h2:mem:testdb;MODE=MySQL;");

        // Create a fake app that we can query just like we would if it was running
        fakeApp = Helpers.fakeApplication(settings);
        db = fakeApp.injector().instanceOf(Database.class);
        authCookie = Http.Cookie.builder("JWT-Auth", "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJUcmF2ZWxFQSIsInVzZXJJZCI6MX0.85pxdAoiT8xkO-39PUD_XNit5R8jmavTFfPSOVcPFWw").withPath("/").build();

        Helpers.start(fakeApp);
    }

    /**
     * Runs evolutions before each test
     * These evolutions are found in conf/test/(whatever), and should contain minimal sql data needed for tests
     */
    @Before
    public void applyEvolutions() {
        // Only certain trips, namely initialisation, and profile folders
        Evolutions.applyEvolutions(db, Evolutions.fromClassLoader(getClass().getClassLoader(), "test/profile/"));
    }

    /**
     * Cleans up evolutions after each test, to allow for them to be re-run for next test
     */
    @After
    public void cleanupEvolutions() {
        Evolutions.cleanupEvolutions(db);
    }

    /**
     * Stop the fake app
     */
    @AfterClass
    public static void stopApp() {
        // Stop the fake app running
        Helpers.stop(fakeApp);
    }

    @Test
    public void createProfile() {
        // Create new json object node
        ObjectNode node = Json.newObject();
        node.put("userId", 2);
        node.put("firstName", "Harry");
        node.put("middleName", "Small");
        node.put("lastName", "Test");
        node.put("dateOfBirth", "1986-11-05");
        node.put("gender", "Male");
        node.put("nationalities", "['Test Country 1', 'Test Country 3']");
        node.put("passports", "['Test Country 3', 'Test Country 2']");
        node.put("travellerTypes", "[\"Test TravellerType 1\", \"Test TravellerType 3\"]");

        // Create request to create a new destination
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .bodyJson(node)
                .cookie(authCookie)
                .uri("/api/profile");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }

    @Test
    public void createProfileUserAlreadyHasProfile() {
        // Create new json object node
        ObjectNode node = Json.newObject();
        node.put("userId", 1);
        node.put("firstName", "Harry");
        node.put("middleName", "Small");
        node.put("lastName", "Test");
        node.put("dateOfBirth", "1986-11-05");
        node.put("gender", "Male");
        node.put("nationalities", "['Test Country 1', 'Test Country 3']");
        node.put("passports", "['Test Country 3', 'Test Country 2']");
        node.put("travellerTypes", "[\"Test TravellerType 1\", \"Test TravellerType 3\"]");

        // Create request to create a new destination
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .bodyJson(node)
                .cookie(authCookie)
                .uri("/api/profile");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(BAD_REQUEST, result.status());
    }

    @Test
    public void createProfileInvalidFormData() throws IOException{
        // Create new json object node
        ObjectNode node = Json.newObject();
        node.put("userId", 2);
        node.put("firstName", "");
        node.put("middleName", "");
        node.put("lastName", "");
        node.put("dateOfBirth", "198dfsf");
        node.put("gender", "Helicopter");
        node.put("nationalities", "");
        node.put("passports", "");
        node.put("travellerTypes", "");

        // Create request to create a new destination
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .bodyJson(node)
                .cookie(authCookie)
                .uri("/api/profile");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(BAD_REQUEST, result.status());

        // Get error response
        HashMap<String, String> response = new ObjectMapper().readValue(Helpers.contentAsString(result), new TypeReference<HashMap<String, String>>() {});

        // Expected error messages
        HashMap<String, String> expectedMessages = new HashMap<>();
        expectedMessages.put("firstName", "firstName field must be present");
        //expectedMessages.put("middleName", "middleName field must be present");
        expectedMessages.put("lastName", "lastName field must be present");
        expectedMessages.put("dateOfBirth", "Invalid date");
        expectedMessages.put("gender", "Invalid gender");
        expectedMessages.put("nationalities", "nationalities field must be present");

        // Check all error messages were present
        for(String key : response.keySet()) {
            assertEquals(expectedMessages.get(key), response.get(key));
        }
    }

    @Test
    public void getProfileId() throws IOException {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/api/profile/1");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        // Get response
        HashMap<String, String> response = new ObjectMapper().readValue(Helpers.contentAsString(result), new TypeReference<HashMap<String, String>>() {});

        // Expected response messages
        HashMap<String, String> expectedMessages = new HashMap<>();
        expectedMessages.put("userId", "1");
        expectedMessages.put("firstName", "Dave");
        expectedMessages.put("middleName", "Jimmy");
        expectedMessages.put("lastName", "Smith");
        expectedMessages.put("dateOfBirth", "1986-11-05");
        expectedMessages.put("gender", "Male");
        expectedMessages.put("nationalities", "Test Country 1,Test Country 3");
        expectedMessages.put("passports", "Test Country 1,Test Country 2");
        expectedMessages.put("travellerTypes", "Test TravellerType 1,Test TravellerType 2");

        // Check all error messages were present
        for(String key : response.keySet()) {
            assertEquals(expectedMessages.get(key), response.get(key));
        }

    }

    @Test
    public void getProfileIdDoesntExist() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/api/profile/2");

        // Get result and check it was not successful
        Result result = route(fakeApp, request);
        assertEquals(BAD_REQUEST, result.status());

    }

    @Test
    public void getProfileAll() throws IOException {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/api/profile");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        // Get response
        HashMap<String, String> response = new ObjectMapper().readValue(Helpers.contentAsString(result), new TypeReference<HashMap<String, String>>() {});

        // Expected response messages
        HashMap<String, String> expectedMessages = new HashMap<>();
        expectedMessages.put("userId", "1");
        expectedMessages.put("firstName", "Dave");
        expectedMessages.put("middleName", "Jimmy");
        expectedMessages.put("lastName", "Smith");
        expectedMessages.put("dateOfBirth", "1986-11-05");
        expectedMessages.put("gender", "Male");
        expectedMessages.put("nationalities", "Test Country 1,Test Country 3");
        expectedMessages.put("passports", "Test Country 1,Test Country 2");
        expectedMessages.put("travellerTypes", "Test TravellerType 1,Test TravellerType 2");

        // Check all error messages were present
        for(String key : response.keySet()) {
            assertEquals(expectedMessages.get(key), response.get(key));
        }
    }

    private List<Profile> searchProfiles (String parameters) throws IOException{
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/api/profile/search?" + parameters);

        Result result = route(fakeApp, request);

        return Arrays.asList(new ObjectMapper().readValue(Helpers.contentAsString(result), Profile[].class));
    }

    @Test
    public void searchProfilesNoFilter() throws IOException {
        List<Profile> profiles = searchProfiles("");

        //Expect 4 profiles to be returned
        assertEquals(4, profiles.size());

        //Check that id and first name are correct for each profile

        long testUserId = 1;

        //User 1: Dave
        Profile dave = profiles.get(0);
        assertEquals(testUserId, (long) dave.userId);
        assertEquals("Dave", dave.firstName);

        //User 2: Steve
        Profile steve = profiles.get(1);
        testUserId = 2;
        assertEquals(testUserId, (long) steve.userId);
        assertEquals("Steve", steve.firstName);

        //User 1: Jim
        Profile jim = profiles.get(2);
        testUserId = 3;
        assertEquals(testUserId, (long) jim.userId);
        assertEquals("Jim", jim.firstName);

        //User 1: Ya boi
        Profile yaBoi = profiles.get(3);
        testUserId = 4;
        assertEquals(testUserId, (long) yaBoi.userId);
        assertEquals("YA BOI", yaBoi.firstName);
    }

    @Test
    public void searchProfilesGenderMale() throws IOException {
        List<Profile> profiles = searchProfiles("gender=male");

        //Expect 2 profiles to be found
        assertEquals(2, profiles.size());

        for (Profile profile : profiles) {
            assertEquals("male", profile.gender.toLowerCase());
        }
    }

    @Test
    public void searchProfilesGenderOther() throws IOException {
        List<Profile> profiles = searchProfiles("gender=other");

        //Expect 1 profiles to be found
        assertEquals(1, profiles.size());

        for (Profile profile : profiles) {
            assertEquals("other", profile.gender.toLowerCase());
        }
    }

    @Test
    public void searchProfilesNationalityFrance() throws IOException {
        List<Profile> profiles = searchProfiles("nationalityId=2");

        long countryId = 2;

        //Expect 3 profiles to be found
        assertEquals(3, profiles.size());

        for (Profile profile : profiles) {
            boolean found = false;
            for (CountryDefinition country : profile.nationalities) {
                if (country.id == countryId) {
                    found = true;
                }
            }
            assert(found);
        }
    }

    @Test
    public void searchProfilesTravellerTypeBackpacker() throws IOException {
        List<Profile> profiles = searchProfiles("travellerTypeId=2");

        long travellerTypeId = 2;

        //Expect 2 profiles to be found
        assertEquals(2, profiles.size());

        for (Profile profile : profiles) {
            boolean found = false;
            for (TravellerTypeDefinition travellerType : profile.travellerTypes) {
                if (travellerType.id == travellerTypeId) {
                    found = true;
                }
            }
            assert(found);
        }
    }

    @Test
    public void searchProfilesMinAge30() throws IOException {
        List<Profile> profiles = searchProfiles("minAge=30");

        //Expect 2 profiles to be found
        assertEquals(2, profiles.size());

        for (Profile profile : profiles) {
            assert(profile.calculateAge() >= 30);
        }
    }

    @Test
    public void searchProfilesMaxAge40() throws IOException {
        List<Profile> profiles = searchProfiles("maxAge=40");

        //Expect 3 profiles to be found
        assertEquals(3, profiles.size());

        for (Profile profile : profiles) {
            assert(profile.calculateAge() <= 40);
        }
    }

    @Test
    public void searchProfilesNoneFound() throws IOException {
        List<Profile> profiles = searchProfiles("minAge=999");

        //Expect 0 profiles to be found
        assertEquals(0, profiles.size());
    }

    @Test
    public void searchProfilesMultipleParams() throws IOException {
        List<Profile> profiles = searchProfiles("minAge=30&travellerTypeId=2");

        //Expect 1 profiles to be found
        assertEquals(1, profiles.size());

        long travellerTypeId = 2;

        for (Profile profile : profiles) {
            assert(profile.calculateAge() >= 30);

            boolean found = false;
            for (TravellerTypeDefinition travellerType : profile.travellerTypes) {
                if (travellerType.id == travellerTypeId) {
                    found = true;
                }
            }
            assert(found);
        }
    }




    @Test
    public void deleteProfileId() {
        // Create request to delete newly created profile
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(DELETE)
                .cookie(this.authCookie)
                .uri("/api/profile/1");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }

    @Test
    public void deleteProfileIdDoesntExist() {
        // Create request to delete a profile that doesn't exist
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(DELETE)
                .cookie(this.authCookie)
                .uri("/api/profile/10");

        // Get result and check it was not successful
        Result result = route(fakeApp, request);
        assertEquals(BAD_REQUEST, result.status());
    }
}
