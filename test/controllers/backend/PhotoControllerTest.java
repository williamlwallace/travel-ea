package controllers.backend;

import akka.util.*;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Source;
import org.apache.commons.io.FileUtils;
import play.db.Database;
import org.hamcrest.CoreMatchers;
import org.junit.*;
import play.Application;
import play.db.evolutions.Evolutions;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.Collections;

import static org.apache.commons.io.FileUtils.getFile;

public class PhotoControllerTest extends WithApplication {

    private static Application fakeApp;
    private static Database db;
    private static Http.Cookie authCookie;

    /**
     * Configures system to use dest database, and starts a fake app
     */
    @BeforeClass
    public static void setUp() {
        // Create custom settings that change the database to use test database instead of production
        Map<String, String> settings = new HashMap<>();
        settings.put("db.default.driver", "org.h2.Driver");
        settings.put("db.default.url", "jdbc:h2:mem:testdb;MODE=MySQL;");

        // Create a fake app that we can query just like we would if it was running
        fakeApp = Helpers.fakeApplication(settings);
        db = fakeApp.injector().instanceOf(Database.class);
        authCookie = Http.Cookie.builder("JWT-Auth",
                "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJUcmF2ZWxFQSIsInVzZXJJZCI6MX0.85pxdAoiT8xkO-39PUD_XNit5R8jmavTFfPSOVcPFWw")
                .withPath("/").build();

        Helpers.start(fakeApp);
    }

    /**
     * Stop the fake app
     */
    @AfterClass
    public static void stopApp() {
        // Clear the files created

        // Stop the fake app running
        Helpers.stop(fakeApp);
    }

    /**
     * Runs trips before each test These trips are found in conf/test/(whatever), and should contain
     * minimal sql data needed for tests
     */
    @Before
    public void applyEvolutions() {
        // Only certain trips, namely initialisation, and destinations folders
        Evolutions.applyEvolutions(db,
                Evolutions.fromClassLoader(getClass().getClassLoader(), "test/photo/"));
    }

    /**
     * Cleans up trips after each test, to allow for them to be re-run for next test
     */
    @After
    public void cleanupEvolutions() {
        Evolutions.cleanupEvolutions(db);
    }

    @Test
    public void testFileUpload() throws IOException {
        // Load a file from the public images to upload
        File file = getFile("./public/images/SA.jpg");
        // Convert this file to a multipart form data part
        Http.MultipartFormData.Part<Source<ByteString, ?>> part =
                new Http.MultipartFormData.FilePart<>("picture", "testPhoto.jpg", "image/jpeg",
                        FileIO.fromPath(file.toPath()),
                        Files.size(file.toPath()));

        // Create a request, with only the single part to add
        Http.RequestBuilder request = Helpers.fakeRequest().uri("/api/photo")
                .method("POST")
                .bodyRaw(
                        Collections.singletonList(part),
                        play.libs.Files.singletonTemporaryFileCreator(),
                        app.asScala().materializer()
                );

        // Post to url and get result, checking that a success was returned
        Result result = Helpers.route(app, request);
        String content = Helpers.contentAsString(result);
        Assert.assertThat(content, CoreMatchers.equalTo("File uploaded"));
    }

    @Test
    public void testMultipleFileUpload() throws IOException {
        // Load a file from the public images to upload
        File file1 = getFile("./public/images/SA.jpg");
        File file2 = getFile("./public/images/travelEA.png");

        // Convert these files to multipart form data parts
        Http.MultipartFormData.Part<Source<ByteString, ?>> part1 =
                new Http.MultipartFormData.FilePart<>("picture", "testPhoto1.jpg", "image/jpeg",
                        FileIO.fromPath(file1.toPath()),
                        Files.size(file1.toPath()));
        Http.MultipartFormData.Part<Source<ByteString, ?>> part2 =
                new Http.MultipartFormData.FilePart<>("picture", "testPhoto2.jpg", "image/png",
                        FileIO.fromPath(file2.toPath()),
                        Files.size(file2.toPath()));

        // Create a request, with only the single part to add
        Http.RequestBuilder request = Helpers.fakeRequest().uri("/api/photo")
                .method("POST")
                .bodyRaw(
                        Arrays.asList(part1, part2),
                        play.libs.Files.singletonTemporaryFileCreator(),
                        app.asScala().materializer()
                );

        // Post to url and get result, checking that a success was returned
        Result result = Helpers.route(app, request);
        String content = Helpers.contentAsString(result);
        Assert.assertThat(content, CoreMatchers.equalTo("File uploaded"));
    }
}