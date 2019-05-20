package repositories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.HttpVerbs.PUT;
import static play.mvc.Http.Status.FORBIDDEN;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.BAD_REQUEST;
import static play.test.Helpers.DELETE;
import static play.test.Helpers.GET;
import static play.test.Helpers.POST;
import static play.test.Helpers.route;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import models.CountryDefinition;
import models.Destination;
import models.User;
import org.junit.*;
import play.Application;
import play.db.Database;
import play.db.evolutions.Evolutions;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Http.Cookie;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;
import repository.DestinationRepository;

public class DestinationRepositoryTest extends WithApplication {

    private static Application fakeApp;
    private static Database db;
    private static Cookie authCookie;
    private static Cookie nonAdminAuthCookie;
    private static DestinationRepository destinationRepository;

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
        authCookie = Cookie.builder("JWT-Auth",
                "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJUcmF2ZWxFQSIsInVzZXJJZCI6MX0.85pxdAoiT8xkO-39PUD_XNit5R8jmavTFfPSOVcPFWw")
                .withPath("/").build();
        nonAdminAuthCookie = Cookie.builder("JWT-Auth",
                "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJUcmF2ZWxFQSIsInVzZXJJZCI6Mn0.sGyO22MrNoNrH928NpSK8PJXmE88_DhivVWgCl3faJ4")
                .withPath("/").build();

        destinationRepository = fakeApp.injector().instanceOf(DestinationRepository.class);

        Helpers.start(fakeApp);
    }

    /**
     * Stop the fake app
     */
    @AfterClass
    public static void stopApp() {
        // Stop the fake app running
        Helpers.stop(fakeApp);
    }

    /**
     * Runs trips before each test These trips are found in conf/test/(whatever), and should contain
     * minimal sql data needed for tests
     */
    @Before
    public void applyEvolutions() {
        // Only certain trips, namely initialisation, and destinations folders
        Evolutions.applyEvolutions(db,
                Evolutions.fromClassLoader(getClass().getClassLoader(), "test/destination/"));
    }

    /**
     * Cleans up trips after each test, to allow for them to be re-run for next test
     */
    @After
    public void cleanupEvolutions() {
        Evolutions.cleanupEvolutions(db);
    }


    @Test
    public void makePermanentlyPublicModifiesOwner() throws IOException {
        // Creates User Object
        User user = new User();
        user.username = "newUser123@test.com";
        user.password = "newUserPassword123";

        // Insert User into database
        Http.RequestBuilder userRequest = Helpers.fakeRequest()
                .method(POST)
                .bodyJson(Json.toJson(user))
                .uri("/api/user");

        // Get result and check it was successful
        Result userResult = route(fakeApp, userRequest);
        assertEquals(OK, userResult.status());

        user.id = 2L;

        // Create new Destination
        Destination createDest = new Destination();
        createDest.user = user;
        createDest.name = "Test destination";
        createDest._type = "Monument";
        createDest.district = "Canterbury";
        createDest.latitude = 10.0;
        createDest.longitude = 20.0;
        createDest.isPublic = true;
        createDest.country = new CountryDefinition();
        createDest.country.id = 1L;

        // Create request to create a new destination
        Http.RequestBuilder createDestRequest = Helpers.fakeRequest()
                .method(POST)
                .bodyJson(Json.toJson(createDest))
                .cookie(nonAdminAuthCookie)
                .uri("/api/destination");

        // Get result and check it was successful
        Result createDestResult = route(fakeApp, createDestRequest);
        assertEquals(OK, createDestResult.status());

        // Get id of destination
        Long destinationId = new ObjectMapper()
                .readValue(Helpers.contentAsString(createDestResult), Long.class);

        Destination destinationToModify = new Destination();
        destinationToModify.id = destinationId;
        destinationToModify.user.id = 2L;

        List<Destination> destinationsToModify = new ArrayList<>();
        destinationsToModify.add(destinationToModify);

        destinationRepository.makePermanentlyPublic(1L, destinationsToModify);

        // Get destinations of user
        Http.RequestBuilder getDestRequest = Helpers.fakeRequest()
                .method(GET)
                .bodyJson(Json.toJson(createDest))
                .cookie(nonAdminAuthCookie)
                .uri("/api/destination/" + destinationId);

        Result getDestResult = route(fakeApp, getDestRequest);
        assertEquals(OK, getDestResult.status());

        // Get id of destination
        Destination destRetrieved = new ObjectMapper()
                .readValue(Helpers.contentAsString(getDestResult), Destination.class);

        System.out.println(destRetrieved.user.id);

        assertEquals(destRetrieved.id, createDest.id);
        assertEquals(destRetrieved.name, createDest.name);
        assertEquals(destRetrieved.latitude, createDest.latitude);
        assertEquals(destRetrieved.longitude, createDest.longitude);
        assertEquals(destRetrieved._type, createDest._type);
        assertEquals(destRetrieved.district, createDest.district);
    }
}