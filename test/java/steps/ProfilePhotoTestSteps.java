package steps;

import static org.apache.commons.io.FileUtils.getFile;
import static org.junit.Assert.assertEquals;
import static play.test.Helpers.GET;
import static play.test.Helpers.route;
import static steps.GenericTestSteps.adminAuthCookie;
import static steps.GenericTestSteps.fakeApp;

import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import models.Photo;
import models.Profile;
import org.junit.Assert;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import util.objects.Pair;

public class ProfilePhotoTestSteps {

    @Then("I can set it as my profile photo")
    @When("I set it as my profile photo")
    public void i_set_it_as_my_profile_photo() throws IOException {
        File file = getFile("./public/images/favicon.png");

        List<Http.MultipartFormData.Part<Source<ByteString, ?>>> partsList = new ArrayList<>();

        // Add text field parts
        for (Pair<String, String> pair : Arrays.asList(
            new Pair<>("isTest", "true"),
            new Pair<>("profilePhotoName", "favicon.png"),
            new Pair<>("publicPhotoFileNames", ""),
            new Pair<>("is_profile", "true")
        )) {
            partsList.add(new Http.MultipartFormData.DataPart(pair.getKey(), pair.getValue()));
        }

        // Convert this file to a multipart form data part
        partsList.add(new Http.MultipartFormData.FilePart<>("picture", "favicon.png", "image/png",
            FileIO.fromPath(file.toPath()),
            "form-data"));

        // Create a request, with only the single part to add
        Http.RequestBuilder request = Helpers.fakeRequest().uri("/api/photo")
            .method("POST")
            .cookie(adminAuthCookie)
            .bodyMultipart(
                partsList,
                play.libs.Files.singletonTemporaryFileCreator(),
                fakeApp.asScala().materializer()
            );
        Result result = route(fakeApp, request);
        assertEquals(201, result.status());

        // Create a request to set the newly uploaded photo to be the profile
        Photo returnedPhoto = new ObjectMapper()
            .readValue(Helpers.contentAsString(result), Photo.class);

        Http.RequestBuilder updateRequest = Helpers.fakeRequest().uri("/api/photo/1/profile")
            .method("PUT")
            .cookie(adminAuthCookie)
            .bodyJson(Json.toJson(returnedPhoto.guid));

        Result updateResult = route(fakeApp, updateRequest);
        assertEquals(200, updateResult.status());
    }

    @When("I set it as my cover photo")
    public void I_set_it_as_my_cover_photo() throws IOException {
        Http.RequestBuilder updateRequest = Helpers.fakeRequest().uri("/api/photo/1/cover")
            .method("PUT")
            .cookie(adminAuthCookie)
            .bodyJson(Json.toJson(1));
        Result updateResult = route(fakeApp, updateRequest);
        assertEquals(200, updateResult.status());
    }

    @Then("A thumbnail is created")
    public void a_thumbnail_is_created() throws IOException {
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(adminAuthCookie)
            .uri("/api/profile/1");

        Result result = route(fakeApp, request);
        assertEquals(200, result.status());
        Profile profile = new ObjectMapper()
            .readValue(Helpers.contentAsString(result), Profile.class);
        String filename = profile.profilePhoto.thumbnailFilename;
        Assert.assertTrue(filename.contains("thumbnails"));
    }

    @Then("It is returned as my profile picture")
    public void it_is_returned_as_my_profile_picture() throws IOException {
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(adminAuthCookie)
            .uri("/api/profile/1");

        Result result = route(fakeApp, request);
        assertEquals(200, result.status());
        Profile profile = new ObjectMapper()
            .readValue(Helpers.contentAsString(result), Profile.class);
        String filename = profile.profilePhoto.filename;

    }

    @Then("It is returned as my cover photo")
    public void it_is_returned_as_my_cover_photo() throws IOException {
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(adminAuthCookie)
            .uri("/api/profile/1");

        Result result = route(fakeApp, request);
        assertEquals(200, result.status());
        Profile profile = new ObjectMapper()
            .readValue(Helpers.contentAsString(result), Profile.class);
        String filename = profile.coverPhoto.filename;
    }
}
