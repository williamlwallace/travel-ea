package controllers.backend;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.OK;
import static play.mvc.Http.Status.UNAUTHORIZED;
import static play.test.Helpers.GET;
import static play.test.Helpers.route;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import models.Tag;
import org.junit.Before;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;


public class TagControllerTest extends controllers.backend.ControllersTest {

    /**
     * Runs evolutions before each test These evolutions are found in conf/test/(whatever), and
     * should contain minimal sql data needed for tests
     */
    @Before
    public void runEvolutions() {
        applyEvolutions("test/tag/");
    }

    @Test
    public void getUserTags() throws IOException {
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(adminAuthCookie)
            .uri("/api/tag/1");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        JsonNode tags = new ObjectMapper()
            .readValue(Helpers.contentAsString(result), JsonNode.class);
        assertEquals(3, tags.get("data").size());
    }

    @Test
    public void getDestinationTags() throws IOException {
        ObjectNode node = Json.newObject();
        node.put("tagType", "DESTINATION_TAG");

        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(adminAuthCookie)
            .bodyJson(node)
            .uri("/api/tag/");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        JsonNode tags = new ObjectMapper()
            .readValue(Helpers.contentAsString(result), JsonNode.class);
        assertEquals(2, tags.get("data").size());
    }

    @Test
    public void getPhotoTags() throws IOException {
        ObjectNode node = Json.newObject();
        node.put("tagType", "PHOTO_TAG");

        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(adminAuthCookie)
            .bodyJson(node)
            .uri("/api/tag/");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        JsonNode tags = new ObjectMapper()
            .readValue(Helpers.contentAsString(result), JsonNode.class);
        assertEquals(2, tags.get("data").size());
    }

    @Test
    public void getTripTags() throws IOException {
        ObjectNode node = Json.newObject();
        node.put("tagType", "TRIP_TAG");

        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(adminAuthCookie)
            .bodyJson(node)
            .uri("/api/tag/");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        JsonNode tags = new ObjectMapper()
            .readValue(Helpers.contentAsString(result), JsonNode.class);
        assertEquals(3, tags.get("data").size());
    }

    @Test
    public void getAllUserPhotoTags() throws IOException {
        //Get tags
        Http.RequestBuilder request = Helpers.fakeRequest().uri("/api/user/1/phototags")
            .method("GET")
            .cookie(adminAuthCookie);

        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        List<Tag> tags = Arrays.asList(
            new ObjectMapper().readValue(Helpers.contentAsString(result), Tag[].class));

        assertEquals(2, tags.size());
        assertEquals("Russia", tags.get(1).name);
        assertEquals("sports", tags.get(0).name);

    }

    @Test
    public void getAllUserPhotoTagsNoAuth() {
        //Get tags
        Http.RequestBuilder request = Helpers.fakeRequest().uri("/api/user/1/phototags")
            .method("GET");

        Result result = route(fakeApp, request);
        assertEquals(UNAUTHORIZED, result.status());
    }

    @Test
    public void getAllDestinationPhotoTags() throws IOException {
        Http.RequestBuilder request = Helpers.fakeRequest().uri("/api/destination/2/phototags")
            .method("GET")
            .cookie(adminAuthCookie);

        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        List<Tag> tags = Arrays.asList(
            new ObjectMapper().readValue(Helpers.contentAsString(result), Tag[].class));

        assertEquals(1, tags.size());
        assertEquals("sports", tags.get(0).name);
    }
}

