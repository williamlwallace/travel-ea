package steps;

import static play.test.Helpers.GET;
import static play.test.Helpers.PUT;
import static play.test.Helpers.route;
import static steps.GenericTestSteps.adminAuthCookie;
import static steps.GenericTestSteps.fakeApp;

import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import models.Photo;
import org.junit.Assert;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;

public class PhotoCaptionsTestSteps {
    @When("set the photo caption to {string}")
    public void set_the_photo_caption_to(String string) throws IOException {
        // Create a request, with only the single part to add
        Http.RequestBuilder request = Helpers.fakeRequest().uri("/api/photo/1/setCaption")
            .method(PUT)
            .cookie(adminAuthCookie)
            .bodyJson(Json.toJson(string));

        Result result = route(fakeApp, request);
        Assert.assertEquals(200, result.status());

        // Check that the request returned a response with an empty string body (the previous caption)
        Assert.assertEquals("", new ObjectMapper().readValue(Helpers.contentAsString(result), String.class));
    }

    @Then("when I view the photo, it will have the caption {string}")
    public void when_I_view_the_photo_it_will_have_the_caption(String string) throws IOException {
        // Create a request, with only the single part to add
        Http.RequestBuilder request = Helpers.fakeRequest().uri("/api/photo/1")
            .method(GET)
            .cookie(adminAuthCookie);

        Result result = route(fakeApp, request);
        Assert.assertEquals(200, result.status());

        List<Photo> response = Arrays.asList(new ObjectMapper().readValue(Helpers.contentAsString(result), Photo[].class));
        Assert.assertEquals(1, response.size());
        Assert.assertEquals(string, response.get(0).caption);
    }
}
