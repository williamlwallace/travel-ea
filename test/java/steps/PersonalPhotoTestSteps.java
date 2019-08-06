package steps;

import static org.apache.commons.io.FileUtils.getFile;
import static play.test.Helpers.DELETE;
import static play.test.Helpers.GET;
import static play.test.Helpers.OK;
import static play.test.Helpers.POST;
import static play.test.Helpers.route;
import static steps.GenericTestSteps.adminAuthCookie;
import static steps.GenericTestSteps.fakeApp;

import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import util.objects.Pair;

public class PersonalPhotoTestSteps {

    @Given("I have no photos")
    public void i_have_no_photos() throws IOException {
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(adminAuthCookie)
            .uri("/api/photo/1");

        Result result = route(fakeApp, request);
        JsonNode photos = new ObjectMapper()
            .readValue(Helpers.contentAsString(result), JsonNode.class);

        for (int i = 0; i < photos.size(); i++) {
            Http.RequestBuilder deleteRequest = Helpers.fakeRequest()
                .method(DELETE)
                .cookie(adminAuthCookie)
                .uri("/api/photo/" + photos.get(i).get("guid"));

            Result deleteResult = route(fakeApp, deleteRequest);
            Assert.assertEquals(OK, deleteResult.status());
        }

        Http.RequestBuilder checkEmptyRequest = Helpers.fakeRequest()
            .method(GET)
            .cookie(adminAuthCookie)
            .uri("/api/photo/1");

        Result checkEmptyResult = route(fakeApp, checkEmptyRequest);
        JsonNode checkEmptyPhotos = new ObjectMapper()
            .readValue(Helpers.contentAsString(checkEmptyResult), JsonNode.class);

        Assert.assertEquals(0, checkEmptyPhotos.size());
    }

    @When("I upload a valid photo")
    public void i_upload_a_valid_photo() throws IOException {
        // Load a file from the public images to upload
        File file = getFile("./public/images/favicon.png");

        // List of objects that will be appended to the body of our multipart/form-data
        List<Http.MultipartFormData.Part<Source<ByteString, ?>>> partsList = new ArrayList<>();

        // Add text field parts
        for (Pair<String, String> pair : Arrays.asList(
            new Pair<>("isTest", "true"),
            new Pair<>("profilePhotoName", "test.png"),
            new Pair<>("publicPhotoFileNames", "")
        )) {
            partsList.add(new Http.MultipartFormData.DataPart(pair.getKey(), pair.getValue()));
        }

        // Convert this file to a multipart form data part
        partsList.add(new Http.MultipartFormData.FilePart<>("picture", "testPhoto.png", "image/png",
            FileIO.fromPath(file.toPath()),
            "form-data"));

        // Create a request, with only the single part to add
        Http.RequestBuilder request = Helpers.fakeRequest().uri("/api/photo")
            .method(POST)
            .cookie(adminAuthCookie)
            .bodyMultipart(
                partsList,
                play.libs.Files.singletonTemporaryFileCreator(),
                fakeApp.asScala().materializer()
            );

        // Post to url and get result, checking that a success was returned
        // Get result and check it was successful
        Result result = route(fakeApp, request);
        Assert.assertEquals(201, result.status());
    }

    @Then("the number of photos i can view will be {int}")
    public void the_number_of_photos_i_have_will_be(int int1) throws IOException {
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(adminAuthCookie)
            .uri("/api/photo/1");

        Result result = route(fakeApp, request);
        Assert.assertEquals(OK, result.status());
        JsonNode photos = new ObjectMapper()
            .readValue(Helpers.contentAsString(result), JsonNode.class);

        Assert.assertEquals(int1, photos.size());
    }


    @Then("I can view all my photos")
    public void i_can_view_all_my_photos() throws IOException {
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(adminAuthCookie)
            .uri("/api/photo/1");

        Result result = route(fakeApp, request);
        JsonNode photos = new ObjectMapper()
            .readValue(Helpers.contentAsString(result), JsonNode.class);

        Assert.assertTrue(photos.size() > 0);
    }
}
