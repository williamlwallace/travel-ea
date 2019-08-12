package controllers.backend;

import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.OK;
import static play.mvc.Http.Status.UNAUTHORIZED;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.Tag;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static play.test.Helpers.route;

public class TagControllerTest extends ControllersTest {

    /**
     * Runs trips before each test These trips are found in conf/test/(whatever), and should contain
     * minimal sql data needed for tests
     */
    @Before
    public void runEvolutions() {applyEvolutions("test/tag/"); }

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

        assertEquals(1, tags.size());
        assertEquals("Russia", tags.get(0).name);
    }

    @Test
    public void getAllUserPhotoTagsNoAuth() {
        //Get tags
        Http.RequestBuilder request = Helpers.fakeRequest().uri("/api/user/1/phototags")
                .method("GET");

        Result result = route(fakeApp, request);
        assertEquals(UNAUTHORIZED, result.status());
    }

}
