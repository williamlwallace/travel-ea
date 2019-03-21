package controllers.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import models.User;
import org.junit.*;
import play.Application;
import play.db.Database;
import play.db.evolutions.Evolutions;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;

import java.beans.Transient;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

public class UserControllerTest extends WithApplication {

    private static Application fakeApp;
    private static Database db;

    /**
     * Configures system to use dest database, and starts a fake app
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

        Helpers.start(fakeApp);
    }

    /**
     * Runs evolutions before each test
     * These evolutions are found in conf/test/(whatever), and should contain minimal sql data needed for tests
     */
    @Before
    public void applyEvolutions() {
        Evolutions.applyEvolutions(db, Evolutions.fromClassLoader(getClass().getClassLoader(), "test/user/"));
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
    public void searchUsers() throws IOException {
        //Create request to GET all users
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/api/user/search");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        // Deserialize result to list of users
        List<User> users = Arrays.asList(new ObjectMapper().readValue(Helpers.contentAsString(result), User[].class));

        // Check that list has exactly one result
        assertEquals(1, users.size());

        // Check that the user is what we expect having run destination test evolution
        User user = users.get(0);
        assertEquals(Long.valueOf(1), user.id);
        assertEquals("dave@gmail.com", user.username);
        assertEquals("password", user.password);
        assertEquals("salt", user.salt);
        assertNull(user.authToken);
    }

    @Test
    public void createValidUser() throws IOException {
        // Create new json object node
        ObjectNode node = Json.newObject();
        node.put("username", "catsinhats123@live.com");
        node.put("password", "MeowMeow123");

        // Create request to create a new user
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .bodyJson(node)
                .uri("/api/user");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        // Get id of user, check it is 2
        Long idOfUser = new ObjectMapper().readValue(Helpers.contentAsString(result), Long.class);
        assertEquals(Long.valueOf(2), idOfUser);
    }

    @Test
    public void createUserTakenUsername() {
        // Create new json object node with registered email
        ObjectNode node = Json.newObject();
        node.put("username", "dave@gmail.com");
        node.put("password", "0hYeahYeah");

        // Create request to create a new user
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .bodyJson(node)
                .uri("/api/user");

        // Get result and check a 400 was sent back
        Result result = route(fakeApp, request);
        assertEquals(BAD_REQUEST, result.status());
    }

    @Test
    public void createUserNoUsername() {
        // Create new json object node
        ObjectNode node = Json.newObject();
        node.put("username", "");
        node.put("password", "0hYeahYeah");

        // Create request to create a new user
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .bodyJson(node)
                .uri("/api/user");

        // Get result and check a 400 was sent back
        Result result = route(fakeApp, request);
        assertEquals(BAD_REQUEST, result.status());
    }

    @Test
    public void createUserNoPassword() {
        // Create new json object node
        ObjectNode node = Json.newObject();
        node.put("username", "catsinhats@live.com");
        node.put("password", "");

        // Create request to create a new user
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .bodyJson(node)
                .uri("/api/user");

        // Get result and check a 400 was sent back
        Result result = route(fakeApp, request);
        assertEquals(BAD_REQUEST, result.status());
    }

    @Test
    public void validLogin() {
        // Create new new user, so password is hashed
        ObjectNode node = Json.newObject();
        node.put("username", "catsinhats123@live.com");
        node.put("password", "MeowMeow123");

        // Create request to create a new user
        Http.RequestBuilder request1 =  Helpers.fakeRequest()
                .method(POST)
                .bodyJson(node)
                .uri("/api/user");

        // Get result and check it was successful
        Result result1 = route(fakeApp, request1);
        assertEquals(OK, result1.status());

        // Create request to login
        Http.RequestBuilder request2 = Helpers.fakeRequest()
                .method(POST)
                .bodyJson(node)
                .uri("/api/login");

        // Get result and check OK was sent back
        Result result2 = route(fakeApp, request2);
        assertEquals(OK, result2.status());
    }

    @Test
    public void loginInvalidPassword() {
        //Invalid password
        ObjectNode node2 = Json.newObject();
        node2.put("username", "dave@gmail.com");
        node2.put("password", "yeetyeet");

        // Create request to login
        Http.RequestBuilder request2 = Helpers.fakeRequest()
                .method(POST)
                .bodyJson(node2)
                .uri("/api/login");

        // Get result and check 401 was sent back
        Result result2 = route(fakeApp, request2);
        assertEquals(UNAUTHORIZED, result2.status());
    }

    @Test
    public void loginInvalidUsername() {
        //Invalid password
        ObjectNode node2 = Json.newObject();
        node2.put("username", "steve@gmail.com");
        node2.put("password", "password");

        // Create request to login
        Http.RequestBuilder request2 = Helpers.fakeRequest()
                .method(POST)
                .bodyJson(node2)
                .uri("/api/login");

        // Get result and check 401 was sent back
        Result result2 = route(fakeApp, request2);
        assertEquals(UNAUTHORIZED, result2.status());
    }

    @Test
    public void deleteValidUser() {
        // Create request to delete newly created user
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(DELETE)
                .uri("/api/user/1");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }

    @Test
    public void deleteInvalidUser() {
        // Create request to delete a user that does not exist
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(DELETE)
                .uri("/api/user/12");

        // Get result and check it failed
        Result result = route(fakeApp, request);
        assertEquals(BAD_REQUEST, result.status());
    }

    @Test
    public void createAndDeleteProfile() throws IOException {
        // Create new json object node
        ObjectNode node = Json.newObject();
        node.put("userId", "2");
        node.put("firstName", "John");
        node.put("middleName", "Nobody");
        node.put("lastName", "Smith");
        node.put("gender", "Male");
        node.put("dateOfBirth", "1999-01-01");
        node.put("nationalities", "France");
        node.put("passports", "France");
        node.put("travellerTypes", "backpacker");

        // Create request to create a new user
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(POST)
            .bodyJson(node)
            .uri("/api/profile");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        // Create request to delete newly created user
        Http.RequestBuilder request2 = Helpers.fakeRequest()
                .method(DELETE)
                .uri("/api/profile/2");

        // Get result and check it was successful
        Result result2 = route(fakeApp, request2);
        assertEquals(OK, result2.status());
    }

    @Test
    public void getProfile() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/api/profile/1");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }
}
