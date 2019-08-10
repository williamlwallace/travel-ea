package controllers.backend;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.GET;
import static play.test.Helpers.route;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
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
        applyEvolutions("test/trip/");
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
}

