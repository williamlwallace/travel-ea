package controllers.backend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.HttpVerbs.PUT;
import static play.mvc.Http.Status.FORBIDDEN;
import static play.mvc.Http.Status.NOT_FOUND;
import static play.mvc.Http.Status.OK;
import static play.mvc.Http.Status.UNAUTHORIZED;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import models.CountryDefinition;
import models.Destination;
import models.User;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
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

public class DestinationControllerTest extends WithApplication {

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

    private Destination getDestination(int id) throws IOException {
        Http.RequestBuilder getRequest = Helpers.fakeRequest()
            .method(GET)
            .cookie(authCookie)
            .uri("/api/destination/" + id);

        Result getResult = route(fakeApp, getRequest);

        if (getResult.status() != OK) {
            return null;
        } else {
            return new ObjectMapper()
                .readValue(Helpers.contentAsString(getResult), Destination.class);
        }
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
        List<Destination> destinations = Arrays.asList(
            new ObjectMapper().readValue(Helpers.contentAsString(result), Destination[].class));

        // Check that list has exactly 4 results
        assertEquals(4, destinations.size());

        // Check that the destination is what we expect having run destination test evolution
        Destination dest = destinations.get(0);
        assertEquals("Eiffel Tower", dest.name);
        assertEquals("Monument", dest._type);
        assertEquals("Paris", dest.district);
        assertEquals(Double.valueOf(48.8583), dest.latitude);
        assertEquals(Double.valueOf(2.2945), dest.longitude);
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
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(DELETE)
            .cookie(nonAdminAuthCookie)
            .uri("/api/destination/4");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }

    @Test
    public void deleteNonExistingDestination() {
        // Create request to delete newly created user
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(DELETE)
            .cookie(authCookie)
            .uri("/api/destination/100");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(NOT_FOUND, result.status());
    }

    @Test
    public void deleteDestinationNotOwner() {
        // Create request to delete newly created user
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(DELETE)
            .cookie(nonAdminAuthCookie)
            .uri("/api/destination/2");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(FORBIDDEN, result.status());
    }

    @Test
    public void deleteDestinationNotOwnerButAdmin() {
        // Create request to delete newly created user
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(DELETE)
            .cookie(authCookie)
            .uri("/api/destination/4");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }

    @Test
    public void editDestination() throws IOException {
        Destination destination = getDestination(4);
        assertNotNull(destination);

        destination.name = "Definitely Not Blitzcrank";
        destination.district = "Summoners Rift";

        Http.RequestBuilder putRequest = Helpers.fakeRequest()
            .method(PUT)
            .bodyJson(Json.toJson(destination))
            .cookie(nonAdminAuthCookie)
            .uri("/api/destination/4");

        // Get result and check it was successful
        Result putResult = route(fakeApp, putRequest);
        assertEquals(OK, putResult.status());

        Destination updatedDestination = getDestination(4);
        assertNotNull(updatedDestination);

        assertEquals("Definitely Not Blitzcrank", updatedDestination.name);
        assertEquals("Summoners Rift", updatedDestination.district);
        assertEquals(destination, updatedDestination);
    }

    @Test
    public void editDestinationInvalidData() throws IOException {
        Destination destination = getDestination(4);
        assertNotNull(destination);

        destination.latitude = 200.3;
        destination.longitude = -344.0;

        Http.RequestBuilder putRequest = Helpers.fakeRequest()
            .method(PUT)
            .bodyJson(Json.toJson(destination))
            .cookie(authCookie)
            .uri("/api/destination/4");

        // Get result and check it was successful
        Result putResult = route(fakeApp, putRequest);
        assertEquals(BAD_REQUEST, putResult.status());
    }

    @Test
    public void editNonExistingDestination() throws IOException {
        Destination destination = getDestination(4);
        assertNotNull(destination);

        String originalName = destination.name;
        destination.name = "Shouldn't work";

        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(PUT)
            .bodyJson(Json.toJson(destination))
            .cookie(authCookie)
            .uri("/api/destination/100");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(NOT_FOUND, result.status());

        Destination updatedDestination = getDestination(4);
        assertNotNull(updatedDestination);

        assertEquals(originalName, updatedDestination.name);
        destination.name = originalName;
        assertEquals(destination, updatedDestination);
    }

    @Test
    public void editDestinationNotOwner() throws IOException {
        Destination destination = getDestination(2);
        assertNotNull(destination);

        String originalName = destination.name;
        destination.name = "Shouldn't work";

        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(PUT)
            .bodyJson(Json.toJson(destination))
            .cookie(nonAdminAuthCookie)
            .uri("/api/destination/2");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(FORBIDDEN, result.status());

        Destination updatedDestination = getDestination(2);
        assertNotNull(updatedDestination);

        assertEquals(originalName, updatedDestination.name);
        destination.name = originalName;
        assertEquals(destination, updatedDestination);
    }

    @Test
    public void editDestinationNotOwnerButAdmin() throws IOException {
        Destination destination = getDestination(4);
        assertNotNull(destination);

        destination.name = "YeetEA";
        destination.district = "localhost";

        Http.RequestBuilder putRequest = Helpers.fakeRequest()
            .method(PUT)
            .bodyJson(Json.toJson(destination))
            .cookie(authCookie)
            .uri("/api/destination/4");

        // Get result and check it was successful
        Result putResult = route(fakeApp, putRequest);
        assertEquals(OK, putResult.status());

        Destination updatedDestination = getDestination(4);
        assertNotNull(updatedDestination);

        assertEquals("YeetEA", updatedDestination.name);
        assertEquals("localhost", updatedDestination.district);
        assertEquals(destination, updatedDestination);
    }

    @Test
    public void editDestinationNoAuth() throws IOException {
        Destination destination = getDestination(2);
        assertNotNull(destination);

        String originalName = destination.name;
        destination.name = "Shouldn't work";

        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(PUT)
            .bodyJson(Json.toJson(destination))
            .uri("/api/destination/2");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(UNAUTHORIZED, result.status());

        Destination updatedDestination = getDestination(2);
        assertNotNull(updatedDestination);

        assertEquals(originalName, updatedDestination.name);
        destination.name = originalName;
        assertEquals(destination, updatedDestination);
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
            .cookie(authCookie)
            .uri("/api/destination");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        // Get id of destination, check it is 2
        Long idOfDestination = new ObjectMapper()
            .readValue(Helpers.contentAsString(result), Long.class);
        assertEquals(Long.valueOf(5), idOfDestination);
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
            .cookie(authCookie)
            .uri("/api/destination");

        // Get result and check it was bad request
        Result result = route(fakeApp, request);
        assertEquals(BAD_REQUEST, result.status());

        // Get error response
        HashMap<String, String> response = new ObjectMapper()
            .readValue(Helpers.contentAsString(result),
                new TypeReference<HashMap<String, String>>() {
                });

        // Expected error messages
        HashMap<String, String> expectedMessages = new HashMap<>();
        expectedMessages.put("district", "district field must be present");
        expectedMessages.put("latitude", "latitude must be at least -90.000000");
        expectedMessages.put("name", "name field must be present");
        expectedMessages.put("_type", "_type field must be present");
        expectedMessages.put("longitude", "longitude must be at least -180.000000");
        expectedMessages.put("country", "country field must be present");

        // Check all error messages were present
        for (String key : response.keySet()) {
            assertEquals(expectedMessages.get(key), response.get(key));
        }
    }

    @Test
    public void makeDestinationPublic() throws SQLException {
        // Statement to get destination with id 1
        PreparedStatement statement = db.getConnection()
            .prepareStatement("SELECT * FROM Destination WHERE id = 1;");

        // Store destination and make sure it is not null and is private
        Destination destination = resultSetToDestList(statement.executeQuery()).stream()
            .filter(x -> x.id == 1).findFirst().orElse(null);
        Assert.assertNotNull(destination);
        Assert.assertFalse(destination.isPublic);

        // Create request to make destination public
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(PUT)
            .cookie(authCookie)
            .uri("/api/destination/makePublic/1");

        // Get result and check it was successfully
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        // Check that destination with id 1 is now public
        destination = resultSetToDestList(statement.executeQuery()).stream().filter(x -> x.id == 1)
            .findFirst().orElse(null);
        Assert.assertNotNull(destination);
        Assert.assertTrue(destination.isPublic);
    }

    @Test
    public void makeDestinationPublicForbidden() throws SQLException {
        // Statement to get destination with id 3
        PreparedStatement statement = db.getConnection()
            .prepareStatement("SELECT * FROM Destination WHERE id = 3;");

        // Store destination and make sure it is not null and is private
        Destination destination = resultSetToDestList(statement.executeQuery()).stream()
            .filter(x -> x.id == 3).findFirst().orElse(null);
        Assert.assertNotNull(destination);
        Assert.assertFalse(destination.isPublic);

        // Create request to make destination public
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(PUT)
            .cookie(nonAdminAuthCookie)
            .uri("/api/destination/makePublic/3");

        // Get result and check its unauthorised
        Result result = route(fakeApp, request);
        assertEquals(FORBIDDEN, result.status());

        // Check that destination with id 3 is still private
        destination = resultSetToDestList(statement.executeQuery()).stream().filter(x -> x.id == 3)
            .findFirst().orElse(null);
        Assert.assertNotNull(destination);
        Assert.assertFalse(destination.isPublic);

    }

    @Test
    public void findSimilarDestinations() throws InterruptedException, ExecutionException {
        // Get all destinations on the database
        // NOTE: Using .get() here as running async lead to race conditions on db connection, sorry Harry :(
        List<Destination> allDestinations = destinationRepository.getAllDestinations().get();
        List<Destination> similarDestinations = destinationRepository
            .getSimilarDestinations(allDestinations.get(0));

        // Assert that 3 destinations were found, and 2 similar ones
        Assert.assertEquals(4, allDestinations.size());
        Assert.assertEquals(2, similarDestinations.size());

        // Now check destinations 2 and 3 were found in similarities, and 1 and 4 were not
        for (Destination destination : allDestinations) {
            if (destination.id == 1 || destination.id == 4) {
                assertFalse(similarDestinations.stream().map(x -> x.id).collect(Collectors.toList())
                    .contains(destination.id));
            } else {
                assertTrue(similarDestinations.stream().map(x -> x.id).collect(Collectors.toList())
                    .contains(destination.id));
            }
        }
    }

    /**
     * Converts a result set from a query for rows from destination table into java list of
     * destinations
     *
     * @param rs Result set
     * @return List of destinations read from result set
     */
    private List<Destination> resultSetToDestList(ResultSet rs) throws SQLException {
        List<Destination> destinations = new ArrayList<>();
        while (rs.next()) {
            Destination destination = new Destination();
            destination.id = rs.getLong("id");
            destination._type = rs.getString("type");
            destination.country = new CountryDefinition();
            destination.country.id = rs.getLong("country_id");
            destination.district = rs.getString("district");
            destination.isPublic = rs.getBoolean("is_public");
            destination.latitude = rs.getDouble("latitude");
            destination.longitude = rs.getDouble("longitude");
            destination.user = new User();
            destination.user.id = rs.getLong("user_id");

            destinations.add(destination);
        }

        return destinations;
    }

}
