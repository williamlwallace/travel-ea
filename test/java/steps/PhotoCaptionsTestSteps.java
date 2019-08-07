package steps;

import static org.apache.commons.io.FileUtils.getFile;
import static play.test.Helpers.GET;
import static play.test.Helpers.POST;
import static play.test.Helpers.PUT;
import static play.test.Helpers.route;
import static steps.GenericTestSteps.adminAuthCookie;
import static steps.GenericTestSteps.fakeApp;

import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import models.Photo;
import org.junit.Assert;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import util.objects.Pair;

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

    @When("I upload a valid photo with the caption {string}")
    public void i_upload_a_valid_photo_with_the_caption(String string) {
        File file = getFile("./public/images/favicon.png");

        List<Http.MultipartFormData.Part<Source<ByteString, ?>>> partsList = new ArrayList<>();

        for (Pair<String, String> pair : Arrays.asList(
            new Pair<>("isTest", "true"),
            new Pair<>("profilePhotoName", "test.png"),
            new Pair<>("publicPhotoFileNames", ""),
            new Pair<>("caption", string)
        )) {
            partsList.add(new Http.MultipartFormData.DataPart(pair.getKey(), pair.getValue()));
        }

        partsList.add(new Http.MultipartFormData.FilePart<>("picture", "testPhoto.png", "image/png",
            FileIO.fromPath(file.toPath()),
            "form-data"));

        Http.RequestBuilder request = Helpers.fakeRequest().uri("/api/photo")
            .method(POST)
            .cookie(adminAuthCookie)
            .bodyMultipart(
                partsList,
                play.libs.Files.singletonTemporaryFileCreator(),
                fakeApp.asScala().materializer()
            );

        Result result = route(fakeApp, request);
        Assert.assertEquals(201, result.status());
    }
}
