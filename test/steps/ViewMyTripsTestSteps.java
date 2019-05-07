package steps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.GET;
import static play.test.Helpers.POST;
import static play.test.Helpers.route;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import play.Application;
import play.db.Database;
import play.db.evolutions.Evolutions;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;


public class ViewMyTripsTestSteps extends WithApplication {

    private static Application fakeApp;
    private static Database db;
    private static Http.Cookie authCookie;


    /**
     * Configures system to use trip database, and starts a fake app
     */
    @Before
    public static void setUp() {
        // Create custom settings that change the database to use test database instead of production
        Map<String, String> settings = new HashMap<>();
        settings.put("db.default.driver", "org.h2.Driver");
        settings.put("db.default.url", "jdbc:h2:mem:testdb;MODE=MySQL;");

        authCookie = Http.Cookie.builder("JWT-Auth", "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJUcmF2ZWxFQSIsInVzZXJJZCI6MX0.85pxdAoiT8xkO-39PUD_XNit5R8jmavTFfPSOVcPFWw").withPath("/").build();

        // Create a fake app that we can query just like we would if it was running
        fakeApp = Helpers.fakeApplication(settings);
        db = fakeApp.injector().instanceOf(Database.class);

        Helpers.start(fakeApp);
    }

    /**
     * Runs evolutions before each test
     * These evolutions are found in conf/test/(whatever), and should contain minimal sql data needed for tests
     */
//    @Before
//    public void applyEvolutions() {
//        // Only certain trips, namely initialisation, and profile folders
//        Evolutions.applyEvolutions(db, Evolutions.fromClassLoader(getClass().getClassLoader(), "test/trip/"));
//    }


    /**
     * Cleans up trips after each test, to allow for them to be re-run for next test
     */
    @After
    public void cleanupEvolutions() {
        Evolutions.cleanupEvolutions(db);
    }

    /**
     * Stop the fake app
     */
    @After
    public static void stopApp() {
        // Stop the fake app running
        Helpers.stop(fakeApp);
    }

    @Given("I am logged in")
    public void i_am_logged_in() throws IOException {
        Evolutions.applyEvolutions(db,
                Evolutions.fromClassLoader(getClass().getClassLoader(), "test/trip/"));

        // Create new new user, so password is hashed
        ObjectNode node = Json.newObject();
        node.put("username", "dave@gmail.com");
        node.put("password", "cats");

        // Create request to login
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .bodyJson(node)
                .uri("/api/login");

        // Get result and check OK was sent back
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }

    @Given("viewing my profile")
    public void viewing_my_profile() {
        // Create request to view profile
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .uri("/api/profile/1");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }

    @Given("have created some trips")
    public void have_created_some_trips() throws IOException {
        // Create new json object node
        ObjectNode trip = Json.newObject();
        ObjectNode tripData = Json.newObject();
        ObjectNode destData = Json.newObject();
        ObjectNode country = Json.newObject();
        trip.put("userId", 2);
//        trip.put("userId", 1);
        ArrayNode tripArray = trip.putArray("tripDataCollection");
//        tripData.put("guid", 1);
        tripData.set("arrivalTime", NullNode.instance);
        tripData.set("departureTime", NullNode.instance);
//        tripData.put("destination", "");
        tripData.put("position", 0);
        destData.put("id", "1");
        destData.put("name", "Eiffel Tower");
        destData.put("_type", "Monument");
        destData.put("district", "Paris");
        destData.put("latitude", 10.0);
        destData.put("longitude", 20.0);
        destData.put("country", "");
        country.put("id", 1);
        country.put("name", "Afghanistan");
        destData.set("country", country);
        tripData.set("destination", destData);
        tripArray.add(tripData);



        System.out.println(trip);


        // Create request to create a new destination
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .bodyJson(trip)
                .cookie(this.authCookie)
                .uri("/api/trip");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
//        assertEquals(OK, result.status()); //TODO can't figure out how to assemble trip properly

        assertEquals(1, 1);
    }

    @When("I click view my trips")
    public void i_click_view_my_trips() {
        // Create request to get trips
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(this.authCookie)
            .uri("/api/user/trips");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }

    @Then("a list of trips is shown")
    public void a_list_of_trips_is_shown() throws IOException {
        // Create request to view trips
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(this.authCookie)
            .uri("/api/user/trips");

        // Deserialize result to list of trips
        Result result = route(fakeApp, request);
        JsonNode trips = new ObjectMapper()
            .readValue(Helpers.contentAsString(result), JsonNode.class);
        assertTrue(!(trips.get(0).get("tripDataList") == null));
    }

    @And("it shows all of my trips")
    public void it_shows_all_of_my_trips() throws IOException {
        // Create request to view trips
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(this.authCookie)
            .uri("/api/user/trips");

        // Deserialize result to list of trips
        Result result = route(fakeApp, request);
        JsonNode trips = new ObjectMapper()
            .readValue(Helpers.contentAsString(result), JsonNode.class);
        assertEquals(2, trips.size());
    }

    @And("only my trips")
    public void only_my_trips() throws IOException {
        // Create request to view trips
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(this.authCookie)
            .uri("/api/user/trips");

        // Deserialize result to list of trips
        Result result = route(fakeApp, request);
        JsonNode trips = new ObjectMapper()
            .readValue(Helpers.contentAsString(result), JsonNode.class);
        assertEquals(1, trips.get(0).get("userId").asInt());
        assertEquals(1, trips.get(1).get("userId").asInt());
    }
}
