package controllers.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.TripData;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

public class TripControllerTest extends WithApplication {

    Application fakeApp;

    @Before
    public void setUp() {
        fakeApp = Helpers.fakeApplication();
    }

    @Test
    public void getTrips() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/api/trip/1");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }

    @Test
    public void createAndDeleteTest() throws IOException {
        // Create json object to store trip to add
        ObjectNode node = Json.newObject();
        node.put("uid", 0);

        // Add sample trip data
        TripData data1 = new TripData();
        data1.destinationId = 2L;
        data1.position = 0L;

        // Map data to array node
        ArrayList<TripData> dataArrayList = new ArrayList<>();
        dataArrayList.add(data1);

        // Put the array node in the json object to be sent
        ArrayNode arrayNode = new ObjectMapper().valueToTree(dataArrayList);
        node.putArray("tripDataCollection").addAll(arrayNode);

        // Create request to create a new trip
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .bodyJson(node)
                .uri("/api/trip");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        // Get UID of newly created user
        Long idOfCreatedTrip = new ObjectMapper().readValue(Helpers.contentAsString(result), Long.class);

        // Create request to delete newly created trip
        Http.RequestBuilder request2 = Helpers.fakeRequest()
                .method(DELETE)
                .uri("/api/trip/" + idOfCreatedTrip);

        // Get result and check it was successful
        Result result2 = route(fakeApp, request2);
        assertEquals(OK, result2.status());
    }
}
