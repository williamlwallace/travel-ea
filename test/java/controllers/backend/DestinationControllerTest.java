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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import models.CountryDefinition;
import models.Destination;
import models.TripData;
import models.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;

public class DestinationControllerTest extends controllers.backend.ControllersTest {

    private static final String DEST_URL_SLASH = "/api/destination/";
    private static final String USER_DEST_URL = "/api/user/destination/";
    private static final String CREATE_DEST_URL = "/api/destination";

    /**
     * Runs trips before each test These trips are found in conf/test/(whatever), and should contain
     * minimal sql data needed for tests
     */
    @Before
    public void runEvolutions() {
        applyEvolutions("test/destination/");
    }


    private Destination getDestination(int id) throws IOException {
        Http.RequestBuilder getRequest = Helpers.fakeRequest()
            .method(GET)
            .cookie(adminAuthCookie)
            .uri(DEST_URL_SLASH + id);

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
        // Gets all destinations of user with ID 1
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(adminAuthCookie)
            .uri(USER_DEST_URL + "1");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        // Deserialize result to list of destinations
        List<Destination> destinations = Arrays.asList(
            new ObjectMapper().readValue(Helpers.contentAsString(result), Destination[].class));

        // Check that list has exactly 3 results
        assertEquals(6, destinations.size());

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
            .uri(DEST_URL_SLASH + "1");

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
            .uri(DEST_URL_SLASH + "7");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }

    @Test
    public void deleteNonExistingDestination() {
        // Create request to delete newly created user
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(DELETE)
            .cookie(adminAuthCookie)
            .uri(DEST_URL_SLASH + "100");

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
            .uri(DEST_URL_SLASH + "2");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(FORBIDDEN, result.status());
    }

    @Test
    public void deleteDestinationNotOwnerButAdmin() {
        // Create request to delete newly created user
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(DELETE)
            .cookie(adminAuthCookie)
            .uri(DEST_URL_SLASH + "4");

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
            .uri(DEST_URL_SLASH + "7");

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
            .cookie(adminAuthCookie)
            .uri(DEST_URL_SLASH + "4");

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
            .cookie(adminAuthCookie)
            .uri(DEST_URL_SLASH + "100");

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
            .uri(DEST_URL_SLASH + "2");

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
            .cookie(adminAuthCookie)
            .uri(DEST_URL_SLASH + "4");

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
            .uri(DEST_URL_SLASH + "2");

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
        User user = new User();
        user.id = 1L;
        node.set("user", Json.toJson(user));

        // Create request to create a new destination
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(POST)
            .bodyJson(node)
            .cookie(adminAuthCookie)
            .uri(CREATE_DEST_URL);

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        // Get id of destination, check it is 5
        Long idOfDestination = new ObjectMapper()
            .readValue(Helpers.contentAsString(result), Long.class);
        assertEquals(Long.valueOf(9), idOfDestination);
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
            .cookie(adminAuthCookie)
            .uri(CREATE_DEST_URL);

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
        expectedMessages.put("district", "District field must be present");
        expectedMessages.put("latitude", "latitude must be at least -90.000000");
        expectedMessages.put("name", "Destination Name field must be present");
        expectedMessages.put("_type", "Destination Type field must be present");
        expectedMessages.put("longitude", "longitude must be at least -180.000000");
        expectedMessages.put("country", "Country field must be present");
        expectedMessages.put("user", "User field must be present");

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
        Destination destination = destinationsFromResultSet(statement.executeQuery()).stream()
            .filter(x -> x.id == 1).findFirst().orElse(null);
        Assert.assertNotNull(destination);
        Assert.assertFalse(destination.isPublic);

        // Create request to make destination public
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(PUT)
            .cookie(adminAuthCookie)
            .uri("/api/destination/makePublic/1");

        // Get result and check it was successfully
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        // Check that destination with id 1 is now public
        destination = destinationsFromResultSet(statement.executeQuery()).stream().filter(x -> x.id == 1)
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
        Destination destination = destinationsFromResultSet(statement.executeQuery()).stream()
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
        destination = destinationsFromResultSet(statement.executeQuery()).stream().filter(x -> x.id == 3)
            .findFirst().orElse(null);
        Assert.assertNotNull(destination);
        Assert.assertFalse(destination.isPublic);
    }

    @Test
    public void findSimilarDestinations() throws InterruptedException, ExecutionException {
        // Get all destinations on the database
        // NOTE: Using .get() here as running async lead to race conditions on db connection, sorry Harry :(
        List<Destination> allDestinations = destinationRepository.getAllDestinations(1L).get();
        List<Destination> similarDestinations = destinationRepository
            .getSimilarDestinations(allDestinations.get(0));

        // Assert that 3 destinations were found, and 2 similar ones
        assertEquals(3, similarDestinations.size());

        // Now check destinations 2, 3, and 8 were found in similarities
        assertTrue(similarDestinations.stream().map(x -> x.id).collect(Collectors.toList())
            .contains(2L));
        assertTrue(similarDestinations.stream().map(x -> x.id).collect(Collectors.toList())
            .contains(3L));
        assertTrue(similarDestinations.stream().map(x -> x.id).collect(Collectors.toList())
            .contains(8L));
    }

    @Test
    public void makeDestinationPublicAndMergeSimilar() throws SQLException {
        // Get existing trip data and photo which reference destination 2
        TripData oldTripData = tripDataFromResultSet(db.getConnection().prepareStatement("SELECT * FROM TripData WHERE position = 2;").executeQuery()).iterator().next();
        ResultSet rs = db.getConnection().prepareStatement("SELECT destination_id FROM DestinationPhoto;").executeQuery();
        rs.next();
        Long oldPhotoDestId = rs.getLong(1);
        assertEquals((Long)2L, oldTripData.destination.id);
        assertEquals((Long)2L, oldPhotoDestId);

        // Call API to make destination 8 public, this should merge all of destination 1 and 2 into destination 8
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(PUT)
            .cookie(nonAdminAuthCookie)
            .uri("/api/destination/makePublic/8");

        // Get result and check it was successfully
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        // Check that trip data got pointed to new destination
        TripData newTripData = tripDataFromResultSet(db.getConnection().prepareStatement("SELECT * FROM TripData WHERE position = 2;").executeQuery()).iterator().next();
        assertEquals((Long)8L, newTripData.destination.id);

        // Check that photo got pointed to new destination
        ResultSet newRs = db.getConnection().prepareStatement("SELECT destination_id FROM DestinationPhoto;").executeQuery();
        newRs.next();
        Long newPhotoDestId = newRs.getLong(1);
        assertEquals((Long)8L, newPhotoDestId);
    }
}
