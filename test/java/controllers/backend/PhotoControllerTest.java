package controllers.backend;

import static controllers.backend.ControllersTest.adminAuthCookie;
import static controllers.backend.ControllersTest.fakeApp;
import static org.apache.commons.io.FileUtils.getFile;
import static org.junit.Assert.assertEquals;
import static play.test.Helpers.route;

import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import models.Photo;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import util.objects.Pair;

public class PhotoControllerTest extends ControllersTest {


    /**
     * Stop the fake app
     */
    @AfterClass
    public static void cleanUp() {
        // Clear the files created
        File directory = new File("./public/storage/photos/test/");
        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (!file.getName().equals("placeholder.txt") && !file.getName().equals("test.jpeg")) {
                file.deleteOnExit();
            }
        }
        directory = new File("./public/storage/photos/test/thumbnails");
        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (!file.getName().equals("placeholder.txt") && !file.getName().equals("test.jpeg")) {
                file.deleteOnExit();
            }
        }
    }

    /**
     * Runs trips before each test These trips are found in conf/test/(whatever), and should contain
     * minimal sql data needed for tests
     */
    @Before
    public void runEvolutions() {
        applyEvolutions("test/photo/");
    }

    @Test
    public void testFileUpload() throws IOException {
        // Load a file from the public images to upload
        File file = getFile("./public/images/favicon.png");

        // List of objects that will be appended to the body of our multipart/form-data
        List<Http.MultipartFormData.Part<Source<ByteString, ?>>> partsList = new ArrayList<>();

        // Add text field parts
        for (Pair<String, String> pair : Arrays.asList(
            new Pair<>("isTest", "true"),
            new Pair<>("profilePhotoName", "favicon.png"),
            new Pair<>("publicPhotoFileNames", ""),
            new Pair<>("caption", "Hello")
        )) {
            partsList.add(new Http.MultipartFormData.DataPart(pair.getKey(), pair.getValue()));
        }

        // Convert this file to a multipart form data part
        partsList.add(new Http.MultipartFormData.FilePart<>("picture", "testPhoto.png", "image/png",
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

        // Post to url and get result, checking that a success was returned
        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(201, result.status());
    }

    @Test
    public void testMultipleFileUpload() throws IOException {
        // Load a file from the public images to upload
        File file1 = getFile("./public/images/favicon.png");
        File file2 = getFile("./public/images/travelEA.png");

        // List of objects that will be appended to the body of our multipart/form-data
        List<Http.MultipartFormData.Part<Source<ByteString, ?>>> partsList = new ArrayList<>();

        // Add text field parts
        for (Pair<String, String> pair : Arrays.asList(
            new Pair<>("isTest", "true"),
            new Pair<>("profilePhotoName", "favicon.png"),
            new Pair<>("publicPhotoFileNames", "travelEA.png")
        )) {
            partsList.add(new Http.MultipartFormData.DataPart(pair.getKey(), pair.getValue()));
        }

        // Convert these files to multipart form data parts
        partsList
            .add(new Http.MultipartFormData.FilePart<>("picture", "testPhoto1.png", "image/png",
                FileIO.fromPath(file1.toPath()),
                "form-data"));
        partsList
            .add(new Http.MultipartFormData.FilePart<>("picture", "testPhoto2.png", "image/png",
                FileIO.fromPath(file2.toPath()),
                "form-data"));

        // Create a request, with only the single part to add
        Http.RequestBuilder request = Helpers.fakeRequest().uri("/api/photo")
            .method("POST")
            .cookie(adminAuthCookie)
            .bodyMultipart(
                partsList,
                play.libs.Files.singletonTemporaryFileCreator(),
                app.asScala().materializer()
            );

        // Post to url and get result, checking that a success was returned
        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(201, result.status());
    }

    @Test
    public void PhotoToDestLinking() {
        //create request with no body
        Http.RequestBuilder request = Helpers.fakeRequest().uri("/api/destination/1/photo/1")
            .method("PUT")
            .cookie(adminAuthCookie);
        //put and check response
        Result result = route(fakeApp, request);
        assertEquals(200, result.status());
    }

    @Test
    public void PhotoToDestLinkingNoPhoto() {
        //create request with no body
        Http.RequestBuilder request = Helpers.fakeRequest().uri("/api/destination/1/photo/3")
            .method("PUT")
            .cookie(adminAuthCookie);
        //put and check response
        Result result = route(fakeApp, request);
        assertEquals(404, result.status());
    }

    @Test
    public void PhotoToDestLinkingNoDestination() {
        //create request with no body
        Http.RequestBuilder request = Helpers.fakeRequest().uri("/api/destination/3/photo/1")
            .method("PUT")
            .cookie(adminAuthCookie);
        //put and check response
        Result result = route(fakeApp, request);
        assertEquals(404, result.status());
    }

    @Test
    public void deletePhotoToDestLink() {
        //create request with no body
        Http.RequestBuilder request = Helpers.fakeRequest().uri("/api/destination/2/photo/1")
            .method("PUT")
            .cookie(adminAuthCookie);
        //put and check response
        Result result = route(fakeApp, request);
        assertEquals(200, result.status());
    }

    @Test
    public void deletePhotoToDestNoPhoto() {
        //create request with no body
        Http.RequestBuilder request = Helpers.fakeRequest().uri("/api/destination/2/photo/3")
            .method("PUT")
            .cookie(adminAuthCookie);
        //put and check response
        Result result = route(fakeApp, request);
        assertEquals(404, result.status());
    }

    @Test
    public void deletePhotoToDestNoDestination() {
        //create request with no body
        Http.RequestBuilder request = Helpers.fakeRequest().uri("/api/destination/3/photo/1")
            .method("PUT")
            .cookie(adminAuthCookie);
        //put and check response
        Result result = route(fakeApp, request);
        assertEquals(404, result.status());
    }

    @Test
    public void togglePhotoPrivacy() throws IOException{
        // Toggle privacy
        Http.RequestBuilder toggleRequest = Helpers.fakeRequest().uri("/api/photo/privacy/2")
            .method("PUT")
            .cookie(adminAuthCookie);
        Result toggleResult = route(fakeApp, toggleRequest);
        assertEquals(200, toggleResult.status());

        // Get the photo that we toggled
        Http.RequestBuilder photoRequest = Helpers.fakeRequest().uri("/api/photo/1")
            .method("GET")
            .cookie(adminAuthCookie);
        Result photoResult = route(fakeApp, photoRequest);
        assertEquals(200, photoResult.status());

        List<Photo> photos = Arrays
            .asList(new ObjectMapper().readValue(Helpers.contentAsString(photoResult), Photo[].class));
        assertEquals(1, photos.size());
        assertEquals(true, photos.get(0).isPublic);
    }

    @Test
    public void togglePhotoPrivacyNoPhoto() {
        // Toggle privacy
        Http.RequestBuilder toggleRequest = Helpers.fakeRequest().uri("/api/photo/privacy/5")
            .method("PUT")
            .cookie(adminAuthCookie);
        Result result = route(fakeApp, toggleRequest);
        assertEquals(404, result.status());
    }

    @Test
    public void getPhoto() throws IOException {
        // Get photo
        Http.RequestBuilder photoRequest = Helpers.fakeRequest().uri("/api/photo/1")
            .method("GET")
            .cookie(adminAuthCookie);
        Result photoResult = route(fakeApp, photoRequest);
        assertEquals(200, photoResult.status());

        List<Photo> photos = Arrays
            .asList(new ObjectMapper().readValue(Helpers.contentAsString(photoResult), Photo[].class));
        assertEquals(1, photos.size());
    }

}
