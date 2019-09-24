package controllers.backend;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.NOT_FOUND;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.GET;
import static play.test.Helpers.PUT;
import static play.test.Helpers.route;
import static org.junit.Assert.assertTrue;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;

import models.Profile;
import models.Destination;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Arrays;

import play.libs.Json;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;


public class NewsFeedControllerTest extends controllers.backend.ControllersTest {

    /**
     * Runs evolutions before each test These evolutions are found in conf/test/(whatever), and
     * should contain minimal sql data needed for tests
     */
    @Before
    public void runEvolutions() {
        applyEvolutions("test/newsfeed/");
    }

    @Test
    public void getNewsFeedEvent() {
        // Create request to like a news feed event
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .cookie(adminAuthCookie)
                .uri("/api/user/1/newsfeed");

        // Get result and check it succeeded
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }

    @Test
    public void likeNewsFeedEvent() throws IOException {
        // Create request to like a news feed event
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(PUT)
                .cookie(adminAuthCookie)
                .uri("/api/newsfeed/1/like");

        // Get result and check it succeeded
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        // Check if content is correct
        String message = new ObjectMapper()
        .readValue(Helpers.contentAsString(result), String.class);

        assertEquals("liked", message);
    }

    @Test
    public void likeNewsFeedEventInvalid() throws IOException {
        // Create request to like a news feed event that does not exist
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(PUT)
                .cookie(adminAuthCookie)
                .uri("/api/newsfeed/5/like");

        // Get result and check it succeeded
        Result result = route(fakeApp, request);
        assertEquals(NOT_FOUND, result.status());
    }

    @Test
    public void unlikeNewsFeedEvent() throws IOException {
        // Create request to unlike a news feed event
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(PUT)
                .cookie(adminAuthCookie)
                .uri("/api/newsfeed/2/like");

        // Get result and check it succeeded
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        // Check if content is correct
        String message = new ObjectMapper()
                .readValue(Helpers.contentAsString(result), String.class);

        assertEquals("unliked", message);
    }

    @Test
    public void checkLikedTrue() throws IOException {
        // Create request to get like status of news feed event
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .cookie(adminAuthCookie)
                .uri("/api/newsfeed/2/like");

        // Get result and check it succeeded
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        // Check if content is correct
        String message = new ObjectMapper()
                .readValue(Helpers.contentAsString(result), String.class);

        assertEquals("true", message);
    }

    @Test
    public void checkLikedFalse() throws IOException {
        // Create request to get like status of news feed event
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .cookie(nonAdminAuthCookie)
                .uri("/api/newsfeed/2/like");

        // Get result and check it succeeded
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        // Check if content is correct
        String message = new ObjectMapper()
                .readValue(Helpers.contentAsString(result), String.class);

        assertEquals("false", message);
    }

    @Test
    public void getLikeCount() throws IOException {
        // Create request to get the like count of news feed event
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(adminAuthCookie)
            .uri("/api/newsfeed/2/likecount");

        // Get result and check it succeeded
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        // Check if content is correct
        ObjectNode message = new ObjectMapper()
            .readValue(Helpers.contentAsString(result), ObjectNode.class);

        Long count = new ObjectMapper()
            .readValue(message.get("likeCount").toString(), Long.class);

        assertEquals(1L, count.longValue());
    }

    @Test
    public void getLikeCountInvalid() throws IOException {
        // Create request to get the like count of news feed event
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(adminAuthCookie)
            .uri("/api/newsfeed/7/likecount");

        // Get result and check it succeeded
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        // Check if content is correct
        ObjectNode message = new ObjectMapper()
            .readValue(Helpers.contentAsString(result), ObjectNode.class);

        Long count = new ObjectMapper()
            .readValue(message.get("likeCount").toString(), Long.class);

        assertEquals(0L, count.longValue());
    }

    @Test
    public void getTrendingUsers() throws IOException {
        Http.RequestBuilder request = Helpers.fakeRequest()
        .method(GET)
        .cookie(adminAuthCookie)
        .uri("/api/newsfeed/trending/user");

        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
        
        List<Long> expectedTrending = Arrays.asList(1L, 4L, 3L, 7L, 5L);

        ObjectMapper mapper = new ObjectMapper();
        List<Profile> profiles =  mapper.convertValue(mapper.readTree(Helpers.contentAsString(result)),
            new TypeReference<List<Profile>>(){});

        assertEquals(5, profiles.size());

        for (Profile profile : profiles) {
            assertTrue(expectedTrending.contains(profile.userId));
        }
    }

    @Test
    public void getTrendingDestinations() throws IOException {
        Http.RequestBuilder request = Helpers.fakeRequest()
        .method(GET)
        .cookie(adminAuthCookie)
        .uri("/api/newsfeed/trending/destination");

        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
        
        List<Long> expectedTrending = Arrays.asList(4L, 1L, 3L, 9L, 5L);

        ObjectMapper mapper = new ObjectMapper();
        List<Destination> destinations =  mapper.convertValue(mapper.readTree(Helpers.contentAsString(result)),
            new TypeReference<List<Destination>>(){});

        assertEquals(5, destinations.size());

        for (Destination destination : destinations) {
            System.out.println(Json.toJson(destination));
            assertTrue(expectedTrending.contains(destination.id));
        }
    }

}
