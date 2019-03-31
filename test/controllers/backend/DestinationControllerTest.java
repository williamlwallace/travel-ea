package controllers.backend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.CountryDefinition;
import models.Destination;
import org.junit.*;
import play.Application;
import play.db.Database;
import play.db.evolutions.Evolutions;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Http.Cookie;
import play.mvc.Http.CookieBuilder;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;
import util.validation.ErrorResponse;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

public class DestinationControllerTest extends WithApplication {

    private static Application fakeApp;
    private static Database db;
    private static Cookie authCookie;

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
        authCookie = Cookie.builder("JWT-Auth", "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJUcmF2ZWxFQSIsInVzZXJJZCI6MX0.85pxdAoiT8xkO-39PUD_XNit5R8jmavTFfPSOVcPFWw").withPath("/").build();

        Helpers.start(fakeApp);
    }

    /**
     * Runs trips before each test
     * These trips are found in conf/test/(whatever), and should contain minimal sql data needed for tests
     */
    @Before
    public void applyEvolutions() {
        // Only certain trips, namely initialisation, and destinations folders
        Evolutions.applyEvolutions(db, Evolutions.fromClassLoader(getClass().getClassLoader(), "test/destination/"));
    }

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
    @AfterClass
    public static void stopApp() {
        // Stop the fake app running
        Helpers.stop(fakeApp);
    }

    @Test
    public void getDestinations() throws IOException {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/api/destination");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        // Deserialize result to list of destinations
        List<Destination> destinations = Arrays.asList(new ObjectMapper().readValue(Helpers.contentAsString(result), Destination[].class));

        // Check that list has exactly one result
        assertEquals(1, destinations.size());

        // Check that the destination is what we expect having run destination test evolution
        Destination dest = destinations.get(0);
        assertEquals("Eiffel Tower", dest.name);
        assertEquals("Monument", dest._type);
        assertEquals("Paris", dest.district);
        assertEquals(Double.valueOf(10.0), dest.latitude);
        assertEquals(Double.valueOf(20.0), dest.longitude);
        assertEquals(Long.valueOf(1), dest.country.id);
        assertEquals(Long.valueOf(1), dest.id);
    }

    @Test
    public void getDestinationById() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/api/destination/1");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }

    @Test
    public void deleteDestination() {
        // Create request to delete newly created destination
        Http.RequestBuilder request2 = Helpers.fakeRequest()
                .method(DELETE)
                .cookie(this.authCookie)
                .uri("/api/destination/1");

        // Get result and check it was successful
        Result result2 = route(fakeApp, request2);
        assertEquals(OK, result2.status());
    }

    @Test
    public void deleteNonExistingDestination() {
        // Create request to delete newly created user
        Http.RequestBuilder request2 = Helpers.fakeRequest()
                .method(DELETE)
                .cookie(this.authCookie)
                .uri("/api/destination/100");

        // Get result and check it was successful
        Result result2 = route(fakeApp, request2);
        assertEquals(BAD_REQUEST, result2.status());
    }

    @Test
    public void createDestination() throws IOException {
        // Create new json object node
        ObjectNode node = Json.newObject();
        node.put("name", "Test destination");
        node.put("_type", "Monument");
        node.put("district", "Canterbury");
        node.put("latitude", 10.0);
        node.put("longitude", 20.0);
        CountryDefinition countryDefinition = new CountryDefinition();
        countryDefinition.id = 1L;
        node.set("country", Json.toJson(countryDefinition));

        // Create request to create a new destination
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .bodyJson(node)
                .cookie(this.authCookie)
                .uri("/api/destination");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        // Get id of destination, check it is 2
        Long idOfDestination = new ObjectMapper().readValue(Helpers.contentAsString(result), Long.class);
        assertEquals(Long.valueOf(2), idOfDestination);
    }

    @Test
    public void createImproperDestination() throws IOException {
        // Create new json object node
        ObjectNode node = Json.newObject();
        // Fields missing: district, name, _type
        node.put("latitude", -1000.0);
        node.put("longitude", -2000.0);

        // Create request to create a new destination
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .bodyJson(node)
                .cookie(this.authCookie)
                .uri("/api/destination");

        // Get result and check it was bad request
        Result result = route(fakeApp, request);
        assertEquals(BAD_REQUEST, result.status());

        // Get error response
        HashMap<String, String> response = new ObjectMapper().readValue(Helpers.contentAsString(result), new TypeReference<HashMap<String, String>>() {});

        // Expected error messages
        HashMap<String, String> expectedMessages = new HashMap<>();
        expectedMessages.put("district", "district field must be present");
        expectedMessages.put("latitude", "latitude must be at least -90.000000");
        expectedMessages.put("name", "name field must be present");
        expectedMessages.put("_type", "_type field must be present");
        expectedMessages.put("longitude", "longitude must be at least -180.000000");
        expectedMessages.put("country", "country field must be present");

        // Check all error messages were present
        for(String key : response.keySet()) {
            assertEquals(expectedMessages.get(key), response.get(key));
        }
    }
}
