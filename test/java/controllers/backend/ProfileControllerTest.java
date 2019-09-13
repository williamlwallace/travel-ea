package controllers.backend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.NOT_FOUND;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.CREATED;
import static play.test.Helpers.GET;
import static play.test.Helpers.POST;
import static play.test.Helpers.UNAUTHORIZED;
import static play.test.Helpers.route;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import models.CountryDefinition;
import models.Profile;
import models.TravellerTypeDefinition;
import org.junit.Before;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import util.objects.PagingResponse;

@SuppressWarnings("unchecked")
public class ProfileControllerTest extends controllers.backend.ControllersTest {

    /**
     * Runs evolutions before each test These evolutions are found in conf/test/(whatever), and
     * should contain minimal sql data needed for tests
     */
    @Before
    public void runEvolutions() {
        applyEvolutions("test/profile/");
    }

    @Test
    public void createProfile() {
        //Create the profile
        Profile newProfile = new Profile();

        newProfile.userId = 5L;
        newProfile.firstName = "NotDave";
        newProfile.lastName = "DefinitelyNotSmith";
        newProfile.dateOfBirth = "1186-11-05";
        newProfile.gender = "Male";

        CountryDefinition france = new CountryDefinition();
        france.name = "France";
        france.id = 2L;

        TravellerTypeDefinition backpacker = new TravellerTypeDefinition();
        backpacker.description = "Backpacker";
        backpacker.id = 2L;

        newProfile.nationalities = new ArrayList<>();
        newProfile.nationalities.add(france);

        newProfile.passports = new ArrayList<>();
        newProfile.passports.add(france);

        newProfile.travellerTypes = new ArrayList<>();
        newProfile.travellerTypes.add(backpacker);

        // Create request to create a new destination
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(POST)
            .bodyJson(Json.toJson(newProfile))
            .cookie(adminAuthCookie)
            .uri("/api/profile");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(CREATED, result.status());
    }

    @Test
    public void createProfileUserAlreadyHasProfile() {
        //Create the profile
        Profile newProfile = new Profile();

        newProfile.userId = 1L;
        newProfile.firstName = "NotDave";
        newProfile.lastName = "DefinitelyNotSmith";
        newProfile.dateOfBirth = "1186-11-05";
        newProfile.gender = "Male";
        newProfile.nationalities = new ArrayList<>();
        newProfile.passports = new ArrayList<>();
        newProfile.travellerTypes = new ArrayList<>();

        // Create request to create a new destination
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(POST)
            .bodyJson(Json.toJson(newProfile))
            .cookie(adminAuthCookie)
            .uri("/api/profile");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(BAD_REQUEST, result.status());
    }

    @Test
    public void createProfileInvalidFormData() throws IOException {
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
            .cookie(adminAuthCookie)
            .uri("/api/profile");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(BAD_REQUEST, result.status());

        // Get error response
        HashMap<String, String> response = new ObjectMapper()
            .readValue(Helpers.contentAsString(result),
                new TypeReference<HashMap<String, String>>() {
                });

        // Expected error messages
        HashMap<String, String> expectedMessages = new HashMap<>();
        expectedMessages.put("firstName", "First Name field must be present");
        expectedMessages.put("lastName", "Last Name field must be present");
        expectedMessages.put("dateOfBirth", "Invalid date");
        expectedMessages.put("gender", "Invalid gender");
        expectedMessages.put("nationalities", "Nationality field must be present");
        expectedMessages.put("travellerTypes", "Traveller Type field must be present");

        // Check all error messages were present
        for (String key : response.keySet()) {
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
        Profile profile = new ObjectMapper()
            .readValue(Helpers.contentAsString(result), Profile.class);

        //Check the profile is not null
        assert (profile != null);

        //Check the user id is correct
        assertEquals(Long.valueOf(1), profile.userId);

        //Check the name is correct
        assertEquals("Dave", profile.firstName);

        //Check the right number of passports is valid
        assertEquals(2, profile.passports.size());
    }

    @Test
    public void getProfileIdDoesntExist() {
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .uri("/api/profile/5");

        // Get result and check it was not successful
        Result result = route(fakeApp, request);
        assertEquals(NOT_FOUND, result.status());
    }

    @Test
    public void getMyProfileNoAuth() {
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .uri("/api/profile/1");

        // Get result and check it was successful
        Result result = route(fakeApp, request);

        // Check that user was redirected due to lack of auth token
        assertEquals(UNAUTHORIZED, result.status());
    }

    @Test
    public void getMyProfile() throws IOException {
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(nonAdminAuthCookie)
            .uri("/api/profile/2");

        // Get result and check it was successful
        Result result = route(fakeApp, request);

        // Check that the request was successful
        assertEquals(OK, result.status());

        // Get response
        Profile profile = new ObjectMapper().readValue(Helpers.contentAsString(result), Profile.class);

        // Check profile has follower details
        assertNotNull(profile.followerUsersCount);
        assertNotNull(profile.followingUsersCount);
    }

    private List<Profile> searchProfiles(String parameters) throws IOException {
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(adminAuthCookie)
            .uri("/api/profile/search?" + parameters);

        Result result = route(fakeApp, request);
        ObjectMapper mapper = new ObjectMapper();

        PagingResponse<Profile> response =  mapper.convertValue(mapper.readTree(Helpers.contentAsString(result)),
            new TypeReference<PagingResponse<Profile>>(){});

        return response.data;
    }

    @Test
    public void searchProfilesNoFilter() throws IOException {
        List<Profile> profiles = searchProfiles("");

        //Expect 4 profiles to be returned
        assertEquals(3, profiles.size());

        //Check that id and first name are correct for each profile
        //User 1: Steve
        Profile steve = profiles.get(0);
        assertEquals(Long.valueOf(2), steve.userId);
        assertEquals("Steve", steve.firstName);

        //User 2: Jim
        Profile jim = profiles.get(1);
        assertEquals(Long.valueOf(3), jim.userId);
        assertEquals("Jim", jim.firstName);

        //User 3: Ya boi
        Profile yaBoi = profiles.get(2);
        assertEquals(Long.valueOf(4), yaBoi.userId);
        assertEquals("YA BOI", yaBoi.firstName);
    }

    @Test
    public void searchProfilesGenderMale() throws IOException {
        List<Profile> profiles = searchProfiles("genders=Male");

        //Expect 2 profiles to be found
        assertEquals(1, profiles.size());

        for (Profile profile : profiles) {
            assertEquals("male", profile.gender.toLowerCase());
        }
    }

    @Test
    public void searchProfilesGenderOther() throws IOException {
        List<Profile> profiles = searchProfiles("genders=Other");

        //Expect 1 profiles to be found
        assertEquals(1, profiles.size());

        for (Profile profile : profiles) {
            assertEquals("other", profile.gender.toLowerCase());
        }
    }

    @Test
    public void searchProfilesNationalityFrance() throws IOException {
        List<Profile> profiles = searchProfiles("nationalityIds=2");

        long countryId = 2;

        //Expect 3 profiles to be found
        assertEquals(2, profiles.size());

        for (Profile profile : profiles) {
            boolean found = false;
            for (CountryDefinition country : profile.nationalities) {
                if (country.id == countryId) {
                    found = true;
                }
            }
            assert (found);
        }
    }

    @Test
    public void searchProfilesTravellerTypeBackpacker() throws IOException {
        List<Profile> profiles = searchProfiles("travellerTypeIds=2");

        long travellerTypeId = 2;

        //Expect 2 profiles to be found
        assertEquals(1, profiles.size());

        for (Profile profile : profiles) {
            boolean found = false;
            for (TravellerTypeDefinition travellerType : profile.travellerTypes) {
                if (travellerType.id == travellerTypeId) {
                    found = true;
                }
            }
            assert (found);
        }
    }

    @Test
    public void searchProfilesMinAge30() throws IOException {
        List<Profile> profiles = searchProfiles("minAge=30");

        //Expect 2 profiles to be found
        assertEquals(1, profiles.size());

        for (Profile profile : profiles) {
            assert (profile.calculateAge() >= 30);
        }
    }

    @Test
    public void searchProfilesMaxAge40() throws IOException {
        List<Profile> profiles = searchProfiles("maxAge=40");

        //Expect 3 profiles to be found
        assertEquals(2, profiles.size());

        for (Profile profile : profiles) {
            assert (profile.calculateAge() <= 40);
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
        List<Profile> profiles = searchProfiles("minAge=20&travellerTypeIds=1");

        long travellerTypeId = 1;

        //Expect 1 profiles to be found
        assertEquals(2, profiles.size());

        for (Profile profile : profiles) {
            assert (profile.calculateAge() >= 20);

            boolean found = false;
            for (TravellerTypeDefinition travellerType : profile.travellerTypes) {
                if (travellerType.id.equals(travellerTypeId)) {
                    found = true;
                }
            }
            assertTrue(found);
        }
    }
}
