package controllers.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Destination;
import org.junit.*;
import play.Application;
import play.db.Database;
import play.db.evolutions.Evolutions;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

public class DestinationControllerTest extends WithApplication {

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
        // Only certain evolutions, namely initialisation, and destinations folders
        Evolutions.applyEvolutions(db, Evolutions.fromClassLoader(getClass().getClassLoader(), "test/destination/"));
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
        assertEquals(Long.valueOf(1), dest.countryId);
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
        // Create request to delete newly created user
        Http.RequestBuilder request2 = Helpers.fakeRequest()
                .method(DELETE)
                .uri("/api/destination/1");

        // Get result and check it was successful
        Result result2 = route(fakeApp, request2);
        assertEquals(OK, result2.status());
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
        node.put("countryId", 1);

        // Create request to create a new destination
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .bodyJson(node)
                .uri("/api/destination");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        // Get id of destination, check it is 2
        Long idOfDestination = new ObjectMapper().readValue(Helpers.contentAsString(result), Long.class);
        assertEquals(Long.valueOf(2), idOfDestination);
    }
}
