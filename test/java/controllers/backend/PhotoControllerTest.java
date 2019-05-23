package controllers.backend;

import akka.util.*;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Source;
import play.db.Database;
import org.junit.*;
import play.Application;
import play.db.evolutions.Evolutions;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;
import util.objects.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import static org.apache.commons.io.FileUtils.getFile;
import static org.junit.Assert.assertEquals;
import static play.test.Helpers.route;

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
        File directory = new File("./public/storage/photos/test/");
        for(File file : Objects.requireNonNull(directory.listFiles())) {
            if(!file.getName().equals("placeholder.txt") && !file.getName().equals("test.jpeg")) {
                file.deleteOnExit();
            }
        }
        directory = new File("./public/storage/photos/test/thumbnails");
        for(File file : Objects.requireNonNull(directory.listFiles())) {
            if(!file.getName().equals("placeholder.txt") && !file.getName().equals("test.jpeg")) {
                file.deleteOnExit();
            }
        }

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
     * Cleans up any added sql data after each test, to allow for them to be re-run for next test
     */
    @After
    public void cleanupEvolutions() {
        Evolutions.cleanupEvolutions(db);
    }

    @Test
    public void testFileUpload() throws IOException {
        // Load a file from the public images to upload
        File file = getFile("./public/images/favicon.png");

        // List of objects that will be appended to the body of our multipart/form-data
        List<Http.MultipartFormData.Part<Source<ByteString, ?>>> partsList = new ArrayList<>();

        // Add text field parts
        for(Pair<String, String> pair : Arrays.asList(
                new Pair<>("isTest", "true"),
                new Pair<>("profilePhotoName", "favicon.png"),
                new Pair<>("publicPhotoFileNames", "")
        )) {
            partsList.add(new Http.MultipartFormData.DataPart(pair.getKey(), pair.getValue()));
        }

        // Convert this file to a multipart form data part
        partsList.add(new Http.MultipartFormData.FilePart<>("picture", "testPhoto.png", "image/png",
                        FileIO.fromPath(file.toPath()),
                        Files.size(file.toPath())));

        // Create a request, with only the single part to add
        Http.RequestBuilder request = Helpers.fakeRequest().uri("/api/photo")
                .method("POST")
                .cookie(authCookie)
                .bodyRaw(
                    partsList,
                    play.libs.Files.singletonTemporaryFileCreator(),
                    app.asScala().materializer()
                );

        // Post to url and get result, checking that a success was returned
        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(201, result.status());

        // Check a success message was sent
        String message = Helpers.contentAsString(result);
        assertEquals("\"File(s) uploaded successfully\"", message);
    }

    @Test
    public void testMultipleFileUpload() throws IOException {
        // Load a file from the public images to upload
        File file1 = getFile("./public/images/favicon.png");
        File file2 = getFile("./public/images/travelEA.png");

        // List of objects that will be appended to the body of our multipart/form-data
        List<Http.MultipartFormData.Part<Source<ByteString, ?>>> partsList = new ArrayList<>();

        // Add text field parts
        for(Pair<String, String> pair : Arrays.asList(
                new Pair<>("isTest", "true"),
                new Pair<>("profilePhotoName", "favicon.png"),
                new Pair<>("publicPhotoFileNames", "travelEA.png")
                )) {
            partsList.add(new Http.MultipartFormData.DataPart(pair.getKey(), pair.getValue()));
        }

        // Convert these files to multipart form data parts
        partsList.add(new Http.MultipartFormData.FilePart<>("picture", "testPhoto1.png", "image/png",
                        FileIO.fromPath(file1.toPath()),
                        Files.size(file1.toPath())));
        partsList.add(new Http.MultipartFormData.FilePart<>("picture", "testPhoto2.png", "image/png",
                        FileIO.fromPath(file2.toPath()),
                        Files.size(file2.toPath())));

        // Create a request, with only the single part to add
        Http.RequestBuilder request = Helpers.fakeRequest().uri("/api/photo")
                .method("POST")
                .cookie(authCookie)
                .bodyRaw(
                        partsList,
                        play.libs.Files.singletonTemporaryFileCreator(),
                        app.asScala().materializer()
                );

        // Post to url and get result, checking that a success was returned
        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(201, result.status());

        // Check a success message was sent
        String message = Helpers.contentAsString(result);
        assertEquals("\"File(s) uploaded successfully\"", message);
    }

    @Test
    public void PhotoToDestLinking() {
        //create request with no body
        Http.RequestBuilder request = Helpers.fakeRequest().uri("/api/destination/1/photo/1")
                .method("PUT")
                .cookie(authCookie);
        //put and check response
        Result result = route(fakeApp, request);
        assertEquals(200, result.status());
    }

    @Test
    public void PhotoToDestLinkingDuplicate() {
        //create request with no body
        Http.RequestBuilder request = Helpers.fakeRequest().uri("/api/destination/2/photo/1")
                .method("PUT")
                .cookie(authCookie);
        //put and check response
        Result result = route(fakeApp, request);
        assertEquals(400, result.status());
    }

    @Test
    public void PhotoToDestLinkingNoPhoto() {
        //create request with no body
        Http.RequestBuilder request = Helpers.fakeRequest().uri("/api/destination/1/photo/2")
                .method("PUT")
                .cookie(authCookie);
        //put and check response
        Result result = route(fakeApp, request);
        assertEquals(404, result.status());
    }

    @Test
    public void PhotoToDestLinkingNoDestination() {
        //create request with no body
        Http.RequestBuilder request = Helpers.fakeRequest().uri("/api/destination/3/photo/1")
                .method("PUT")
                .cookie(authCookie);
        //put and check response
        Result result = route(fakeApp, request);
        assertEquals(404, result.status());
    }

    @Test
    public void deletePhotoToDestLink() {
        //create request with no body
        Http.RequestBuilder request = Helpers.fakeRequest().uri("/api/destination/2/photo/1")
                .method("DELETE")
                .cookie(authCookie);
        //put and check response
        Result result = route(fakeApp, request);
        assertEquals(200, result.status());
    }

    @Test
    public void deletePhotoToDestNoPhoto() {
        //create request with no body
        Http.RequestBuilder request = Helpers.fakeRequest().uri("/api/destination/2/photo/2")
                .method("DELETE")
                .cookie(authCookie);
        //put and check response
        Result result = route(fakeApp, request);
        assertEquals(404, result.status());
    }

    @Test
    public void deletePhotoToDestNoDestination() {
        //create request with no body
        Http.RequestBuilder request = Helpers.fakeRequest().uri("/api/destination/3/photo/1")
                .method("DELETE")
                .cookie(authCookie);
        //put and check response
        Result result = route(fakeApp, request);
        assertEquals(404, result.status());
    }
}
