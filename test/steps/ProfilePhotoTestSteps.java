package steps;

import akka.http.javadsl.model.FormData;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.backend.PhotoController;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.ebean.config.ServerConfig;
import org.junit.Assert;
import play.Application;
import play.db.Database;
import play.db.ebean.EbeanConfig;
import play.db.evolutions.Evolutions;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;
import repository.DatabaseExecutionContext;
import repository.PhotoRepository;
import util.customObjects.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import static org.apache.commons.io.FileUtils.getFile;
import static play.test.Helpers.GET;
import static play.test.Helpers.POST;
import static play.test.Helpers.route;

public class ProfilePhotoTestSteps extends WithApplication {

    private static Application fakeApp;
    private static Database db;
    private static Http.Cookie authCookie;

    /**
     * Configures system to use trip database, and starts a fake app
     */
    @Before
    public static void setUp() {
        // Create custom settings that change the database to use test database instead of production
        Map<String, String> settings = new HashMap<>();
        settings.put("db.default.driver", "org.h2.Driver");
        settings.put("db.default.url", "jdbc:h2:mem:testdb;MODE=MySQL;");

        authCookie = Http.Cookie.builder("JWT-Auth", "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJUcmF2ZWxFQSIsInVzZXJJZCI6MX0.85pxdAoiT8xkO-39PUD_XNit5R8jmavTFfPSOVcPFWw").withPath("/").build();

        // Create a fake app that we can query just like we would if it was running
        fakeApp = Helpers.fakeApplication(settings);
        db = fakeApp.injector().instanceOf(Database.class);

        Helpers.start(fakeApp);

    }


    /**
     * Cleans up trips after each test, to allow for them to be re-run for next test
     */
    @After
    public void cleanupEvolutions() {
        Evolutions.cleanupEvolutions(db);
        stopApp();
    }

    /**
     * Stop the fake app
     */

    public static void stopApp() {
        // Stop the fake app running
        Helpers.stop(fakeApp);
    }


    @Then("I can set it as my profile photo")
    @When("I set it as my profile photo")
    public void i_set_it_as_my_profile_photo() throws IOException {
        System.out.println("hi");
        File file = getFile("./public/images/favicon.png");

        List<Http.MultipartFormData.Part<Source<ByteString, ?>>> partsList = new ArrayList<>();

        // Add text field parts
        for(Pair<String, String> pair : Arrays.asList(
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
                Files.size(file.toPath())));

        // Create a request, with only the single part to add
        Http.RequestBuilder request = Helpers.fakeRequest().uri("/api/photo")
                .method("POST")
                .cookie(authCookie)
                .bodyRaw(
                        partsList,
                        play.libs.Files.singletonTemporaryFileCreator(),
                        fakeApp.asScala().materializer()
                );

        Result result = route(fakeApp, request);

        Assert.assertEquals(201, result.status());

    }

    @Then("A thumbnail is created")
    public void a_thumbnail_is_created() throws IOException {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .cookie(this.authCookie)
                .uri("/api/photo/1/profile");

        Result result = route(fakeApp, request);
        Assert.assertEquals(200, result.status());
        JsonNode photo = new ObjectMapper()
                .readValue(Helpers.contentAsString(result), JsonNode.class);
        String thumbnail = photo.get("thumbnailFilename").toString();
        Assert.assertTrue(thumbnail.contains("favicon.png"));
        Assert.assertTrue(thumbnail.contains("thumbnails"));

    }

    @Then("It is returned as my profile picture")
    public void it_is_returned_as_my_profile_picture() throws IOException {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .cookie(this.authCookie)
                .uri("/api/photo/1/profile");

        Result result = route(fakeApp, request);
        Assert.assertEquals(200, result.status());
        JsonNode photo = new ObjectMapper()
                .readValue(Helpers.contentAsString(result), JsonNode.class);
        String filename = photo.get("filename").toString();
        Assert.assertTrue(filename.contains("favicon.png"));

    }
}
