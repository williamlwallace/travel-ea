package controllers.backend;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.UNAUTHORIZED;
import static play.test.Helpers.GET;
import static play.test.Helpers.POST;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.route;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
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

    /**
     * Runs evolutions before each test. These evolutions are found in conf/test/(whatever), and
     * should contain minimal sql data needed for tests
     */
    @Before
    public void runEvolutions() {
        applyEvolutions("test/treasureHunt/");
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
        treasureHunt.startDate = "2019-05-29";
        treasureHunt.endDate = "2019-07-30";

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

    @Test
    public void getAllUserTreasureHunts() throws IOException {
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(adminAuthCookie)
            .uri("/api/treasureHunt/1");

        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        JsonNode hunts = new ObjectMapper()
            .readValue(Helpers.contentAsString(result), JsonNode.class);
        assertEquals(1, hunts.size());
    }

    @Test
    public void getUnauthorizedTreasureHunts() {
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(nonAdminAuthCookie)
            .uri("/api/treasureHunt/1");

        Result result = route(fakeApp, request);
        assertEquals(UNAUTHORIZED, result.status());
    }
}
