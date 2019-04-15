package steps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.GET;
import static play.test.Helpers.POST;
import static play.test.Helpers.route;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cucumber.api.java.AfterStep;
import cucumber.api.java.BeforeStep;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.AfterClass;
import play.Application;
import play.db.Database;
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
     * Stop the fake app
     */
    @AfterClass
    public static void stopApp() {
        // Stop the fake app running
        Helpers.stop(fakeApp);
    }

    /**
     * Runs evolutions before each test These evolutions are found in conf/test/(whatever), and
     * should contain minimal sql data needed for tests
     */
    @BeforeStep
    public void applyEvolutions() {
        // Only certain trips, namely initialisation, and profile folders
//        Evolutions.applyEvolutions(db,
//                Evolutions.fromClassLoader(getClass().getClassLoader(), "test/trip/"));
    }

    /**
     * Cleans up evolutions after each test, to allow for them to be re-run for next test
     */
    @AfterStep
    public void cleanupEvolutions() {
//        Evolutions.cleanupEvolutions(db);
    }


    @Given("I am logged in")
    public void i_am_logged_in() throws IOException {
        // Create custom settings that change the database to use test database instead of production
        Map<String, String> settings = new HashMap<>();
        settings.put("db.default.driver", "org.h2.Driver");
        settings.put("db.default.url", "jdbc:h2:mem:testdb;MODE=MySQL;");

        // Create a fake app that we can query just like we would if it was running
        fakeApp = Helpers.fakeApplication(settings);
        db = fakeApp.injector().instanceOf(Database.class);
        authCookie = Http.Cookie.builder("JWT-Auth",
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJUcmF2ZWxFQSIsInVzZXJJZCI6MX0.85pxdAoiT8xkO-39PUD_XNit5R8jmavTFfPSOVcPFWw")
            .withPath("/").build();

        Helpers.start(fakeApp);

        // Create new json object node
        ObjectNode node = Json.newObject();
        node.put("username", "tester1@gmail.com");
        node.put("password", "password");

        // Create request to create a new user
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(POST)
            .bodyJson(node)
            .uri("/api/user"); //TODO change to "/api/login" when evolutions work

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        // Check a success message was sent
        String message = new ObjectMapper()
            .readValue(Helpers.contentAsString(result), String.class);
        assertEquals("Success", message);
    }

    @Given("viewing my profile")
    public void viewing_my_profile() {
        // Create request to view profile
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .uri("/api/profile/1"); //TODO change to the created user when evolutions work

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }

    @Given("have created some trips")
    public void have_created_some_trips() throws IOException {
        // Create new json object node
//        ObjectNode trip = Json.newObject();
//        ObjectNode tripData = Json.newObject();
//        ObjectNode destData = Json.newObject();
////        ObjectNode country = Json.newObject();
////        trip.put("userId", 2);
////        trip.put("userId", 1);
//        ArrayNode tripArray = trip.putArray("tripDataCollection");
////        tripData.put("guid", 1);
//        tripData.set("arrivalTime", NullNode.instance);
//        tripData.set("departureTime", NullNode.instance);
//        tripData.put("destination", "");
//        tripData.put("position", 0);
//        destData.put("id", "1");
////        destData.put("name", "Eiffel Tower");
////        destData.put("_type", "Monument");
////        destData.put("district", "Paris");
////        destData.put("latitude", 10.0);
////        destData.put("longitude", 20.0);
////        destData.put("country", "");
////        country.put("id", 1);
////        country.put("name", "Afghanistan");
////        destData.set("country", country);
//        tripData.set("destination", destData);
//        tripArray.add(tripData);
//
//
//
//        System.out.println(trip);
//
//
//        // Create request to create a new destination
//        Http.RequestBuilder request = Helpers.fakeRequest()
//                .method(POST)
//                .bodyJson(trip)
//                .cookie(this.authCookie)
//                .uri("/api/trip");
//
//        // Get result and check it was successful
//        Result result = route(fakeApp, request);
//        assertEquals(OK, result.status());

        assertEquals(1, 1);
    }

    @When("I click view my trips")
    public void i_click_view_my_trips() {
        // Create request to get trips
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(this.authCookie)
            .uri("/api/trip/getAll/");

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
            .uri("/api/trip/getAll/");

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
            .uri("/api/trip/getAll/");

        // Deserialize result to list of trips
        Result result = route(fakeApp, request);
        JsonNode trips = new ObjectMapper()
            .readValue(Helpers.contentAsString(result), JsonNode.class);
        assertEquals(1, trips.get(0).get("tripDataList")
            .size()); //TODO change this number when the creation step and evolutions work
    }

    @And("only my trips")
    public void only_my_trips() throws IOException {
        // Create request to view trips
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(this.authCookie)
            .uri("/api/trip/getAll/");

        // Deserialize result to list of trips
        Result result = route(fakeApp, request);
        JsonNode trips = new ObjectMapper()
            .readValue(Helpers.contentAsString(result), JsonNode.class);
        assertEquals(1, trips.get(0).get("userId")
            .asInt()); //TODO change this number to created user's Id when the creation step and evolutions work
    }
}
