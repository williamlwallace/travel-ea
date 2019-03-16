package controllers.backend;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.*;
import play.Application;
import play.api.db.evolutions.ClassLoaderEvolutionsReader;
import play.db.Database;
import play.db.evolutions.Evolutions;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

public class DestinationControllerTest extends WithApplication {

    private Application fakeApp;
    private Database db;

    @Before
    public void setUp() {
        // Create custom settings that change the database to use test database instead of production
        Map<String, String> settings = new HashMap<>();
        settings.put("db.default.url", "jdbc:mysql://mysql2.csse.canterbury.ac.nz/seng302-2019-team400-test");

        // Create a fake app that we can query just like we would if it was running
        fakeApp = Helpers.fakeApplication(settings);
        db = fakeApp.injector().instanceOf(Database.class);

        // Clean up the evolutions that were automatically run on startup (i.e, wipe the database)
        Evolutions.cleanupEvolutions(db);

        // Only run the evolutions found in conf/test/evolutions/default instead
        Evolutions.applyEvolutions(db, Evolutions.fromClassLoader(getClass().getClassLoader(), "test/"));

        Helpers.start(app);
    }

    @After
    public void cleanUp() {
        Evolutions.cleanupEvolutions(db);
        Helpers.stop(fakeApp);
    }

    @Test
    public void getDestinations() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/api/destination");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
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
    public void createDestination() {
        // Create new json object node
        ObjectNode node = Json.newObject();
        node.put("name", "Test destination");
        node.put("_type", "Monument");
        node.put("district", "Canterbury");
        node.put("latitude", 10.0);
        node.put("longitude", 20.0);
        node.put("countryId", 5);

        // Create request to create a new destination
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .bodyJson(node)
                .uri("/api/destination");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }
}
