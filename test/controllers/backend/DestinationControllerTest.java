package controllers.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

public class DestinationControllerTest extends WithApplication {

    Application fakeApp;

    @Before
    public void setUp() {
        fakeApp = Helpers.fakeApplication();
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
    public void createAndDeleteDestination() throws IOException {
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

        // Get UID of newly created destination
        Long idOfCreatedDestination = new ObjectMapper().readValue(Helpers.contentAsString(result), Long.class);

        // Create request to delete newly created user
        Http.RequestBuilder request2 = Helpers.fakeRequest()
                .method(DELETE)
                .uri("/api/destination/" + idOfCreatedDestination);

        // Get result and check it was successful
        Result result2 = route(fakeApp, request2);
        assertEquals(OK, result2.status());
    }
}
