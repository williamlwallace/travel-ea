package controllers.backend;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.HttpVerbs.DELETE;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.FORBIDDEN;
import static play.mvc.Http.Status.NOT_FOUND;
import static play.test.Helpers.POST;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.route;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import models.Destination;
import models.TreasureHunt;
import models.User;
import org.junit.Before;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;

public class TreasureHuntControllerTest extends controllers.backend.ControllersTest {

    private static String DELETE_TREASURE_HUNT_URI = "/api/treasureHunt/";

    /**
     * Runs evolutions before each test. These evolutions are found in conf/test/(whatever), and
     * should contain minimal sql data needed for tests
     */
    @Before
    public void runEvolutions() {
        applyEvolutions("test/treasureHunt/");
    }

    @Test
    public void deleteTreasureHunt() throws SQLException {
        // Get existing treasure hunts, check that two exist
        assertEquals(2, treasureHuntsFromResultSet(
            db.getConnection().prepareStatement("SELECT * FROM TreasureHunt;").executeQuery()
        ).size());

        // Deletes the destination owned by this user
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(DELETE)
            .cookie(nonAdminAuthCookie)
            .uri(DELETE_TREASURE_HUNT_URI + "2");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        // Now check that treasure hunt has indeed been deleted
        Collection<TreasureHunt> foundTreasureHunts = treasureHuntsFromResultSet(
            db.getConnection().prepareStatement("SELECT * FROM TreasureHunt;").executeQuery());
        assertEquals(1, foundTreasureHunts.size());
        assertEquals("Big pointy thing in Paris", foundTreasureHunts.iterator().next().riddle);
    }

    @Test
    public void deleteOtherPersonTreasureHuntAdmin() throws SQLException {
        // Get existing treasure hunts, check that two exist
        assertEquals(2, treasureHuntsFromResultSet(
            db.getConnection().prepareStatement("SELECT * FROM TreasureHunt;").executeQuery()
        ).size());

        // Deletes the treasure hunt owned by the other user, as an admin
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(DELETE)
            .cookie(adminAuthCookie)
            .uri(DELETE_TREASURE_HUNT_URI + "2");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        // Now check that treasure hunt has indeed been deleted
        Collection<TreasureHunt> foundTreasureHunts = treasureHuntsFromResultSet(
            db.getConnection().prepareStatement("SELECT * FROM TreasureHunt;").executeQuery());
        assertEquals(1, foundTreasureHunts.size());
        assertEquals("Big pointy thing in Paris", foundTreasureHunts.iterator().next().riddle);
    }

    @Test
    public void deleteTreasureHuntUnauthorized() throws SQLException {
        // Get existing treasure hunts, check that two exist
        assertEquals(2, treasureHuntsFromResultSet(
            db.getConnection().prepareStatement("SELECT * FROM TreasureHunt;").executeQuery()
        ).size());

        // Attempt to delete the admin's treasure hunt, as a regular user
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(DELETE)
            .cookie(nonAdminAuthCookie)
            .uri(DELETE_TREASURE_HUNT_URI + "1");

        // Get result and check it was rejected 403
        Result result = route(fakeApp, request);
        assertEquals(FORBIDDEN, result.status());

        // Now check that no treasure hunts have been deleted
        assertEquals(2, treasureHuntsFromResultSet(
            db.getConnection().prepareStatement("SELECT * FROM TreasureHunt;").executeQuery()
        ).size());
    }

    @Test
    public void deleteTreasureHuntNotFound() throws SQLException {
        // Get existing treasure hunts, check that two exist
        assertEquals(2, treasureHuntsFromResultSet(
            db.getConnection().prepareStatement("SELECT * FROM TreasureHunt;").executeQuery()
        ).size());

        // Attempt to delete a treasure hunt that does not exist
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(DELETE)
            .cookie(nonAdminAuthCookie)
            .uri(DELETE_TREASURE_HUNT_URI + "3");

        // Get result and check it returned 404 error
        Result result = route(fakeApp, request);
        assertEquals(NOT_FOUND, result.status());

        // Now check that no treasure hunts have been deleted
        assertEquals(2, treasureHuntsFromResultSet(
            db.getConnection().prepareStatement("SELECT * FROM TreasureHunt;").executeQuery()
        ).size());
    }


    @Test
    public void createValidTreasureHunt() {
        // Create necessary objects for treasure hunt
        TreasureHunt treasureHunt = new TreasureHunt();
        User user = new User();
        Destination destination = new Destination();

        // Set user and destination ID's
        user.id = 2L;
        destination.id = 1L;

        // Assign user and destination information to treasure hunt
        treasureHunt.user = user;
        treasureHunt.destination = destination;

        // Set other required information for a valid treasure hunt
        treasureHunt.riddle = "Init";
        treasureHunt.startDate = LocalDateTime.of(2019, 5, 29, 0, 0, 0);
        treasureHunt.endDate = LocalDateTime.of(2019, 7, 30, 0, 0, 0);

        // Convert treasure hunt object to Json
        JsonNode treasureHuntJson = Json.toJson(treasureHunt);

        // Build HTTP request for creating treasure hunt
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(POST)
            .bodyJson(treasureHuntJson)
            .cookie(nonAdminAuthCookie)
            .uri("/api/treasureHunt");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }

    @Test
    public void createInvalidTreasureHunt() throws IOException {
        // Create necessary objects for treasure hunt
        TreasureHunt treasureHunt = new TreasureHunt();
        User user = new User();
        Destination destination = new Destination();

        // Set user and destination ID's
        user.id = 2L;
        destination.id = 1L;

        // Assign user and destination information to treasure hunt
        treasureHunt.user = user;
        treasureHunt.destination = destination;

        // Set other required fields with invalid information
        treasureHunt.riddle = "";
        treasureHunt.startDate = null;
        treasureHunt.endDate = null;

        // Convert treasure hunt object to JSON
        JsonNode treasureHuntJson = Json.toJson(treasureHunt);

        // Build HTTP request for creating treasure hunt
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(POST)
            .bodyJson(treasureHuntJson)
            .cookie(nonAdminAuthCookie)
            .uri("/api/treasureHunt");

        // Get result and check it failed
        Result result = route(fakeApp, request);
        assertEquals(BAD_REQUEST, result.status());

        // Check error response messages to ensure all expected fields failed
        HashMap<String, String> response = new ObjectMapper()
            .readValue(Helpers.contentAsString(result),
                new TypeReference<HashMap<String, String>>() {
                });

        // Expected error messages
        HashMap<String, String> expectedMessages = new HashMap<>();
        expectedMessages.put("riddle", "Riddle field must be present");
        expectedMessages.put("startDate", "Start date field must be present");
        expectedMessages.put("endDate", "End date field must be present");

        // Check all error messages were present
        for (String key : response.keySet()) {
            assertEquals(expectedMessages.get(key), response.get(key));
        }
    }
}
