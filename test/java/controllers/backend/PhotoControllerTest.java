package controllers.backend;

import static junit.framework.TestCase.assertTrue;
import static org.apache.commons.io.FileUtils.getFile;
import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.CREATED;
import static play.mvc.Http.Status.FORBIDDEN;
import static play.mvc.Http.Status.NOT_FOUND;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.route;

import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import models.Photo;
import models.Tag;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import util.objects.PagingResponse;
import util.objects.Pair;

public class PhotoControllerTest extends controllers.backend.ControllersTest {

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
    public void testFileUpload() {
        // Load a file from the public images to upload
        File file = getFile("./public/images/favicon.png");

        // List of objects that will be appended to the body of our multipart/form-data
        List<Http.MultipartFormData.Part<Source<ByteString, ?>>> partsList = new ArrayList<>();

        // Add text field parts
        for (Pair<String, String> pair : Arrays.asList(
            new Pair<>("isTest", "true"),
            new Pair<>("profilePhotoName", "favicon.png"),
            new Pair<>("publicPhotoFileNames", ""),
            new Pair<>("caption", "Hello"),
            new Pair<>("tags", "[{\"name\":\"Germany\"}]")

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
        assertEquals(CREATED, result.status());
    }

    @Test
    public void PhotoToDestLinking() {
        //create request with no body
        Http.RequestBuilder request = Helpers.fakeRequest().uri("/api/destination/1/photo/1")
            .method("PUT")
            .cookie(adminAuthCookie);
        //put and check response
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }

    @Test
    public void PhotoToDestLinkingNoPhoto() {
        //create request with no body
        Http.RequestBuilder request = Helpers.fakeRequest().uri("/api/destination/1/photo/4")
            .method("PUT")
            .cookie(adminAuthCookie);
        //put and check response
        Result result = route(fakeApp, request);
        assertEquals(NOT_FOUND, result.status());
    }

    @Test
    public void PhotoToDestLinkingNoDestination() {
        //create request with no body
        Http.RequestBuilder request = Helpers.fakeRequest().uri("/api/destination/3/photo/1")
            .method("PUT")
            .cookie(adminAuthCookie);
        //put and check response
        Result result = route(fakeApp, request);
        assertEquals(NOT_FOUND, result.status());
    }

    @Test
    public void deletePhotoToDestLink() {
        //create request with no body
        Http.RequestBuilder request = Helpers.fakeRequest().uri("/api/destination/2/photo/1")
            .method("PUT")
            .cookie(adminAuthCookie);
        //put and check response
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }

    @Test
    public void deletePhotoToDestNoPhoto() {
        //create request with no body
        Http.RequestBuilder request = Helpers.fakeRequest().uri("/api/destination/2/photo/4")
            .method("PUT")
            .cookie(adminAuthCookie);
        //put and check response
        Result result = route(fakeApp, request);
        assertEquals(NOT_FOUND, result.status());
    }

    @Test
    public void deletePhotoToDestNoDestination() {
        //create request with no body
        Http.RequestBuilder request = Helpers.fakeRequest().uri("/api/destination/3/photo/1")
            .method("PUT")
            .cookie(adminAuthCookie);
        //put and check response
        Result result = route(fakeApp, request);
        assertEquals(NOT_FOUND, result.status());
    }

    @Test
    public void togglePhotoPrivacy() throws IOException {
        // Toggle privacy
        Http.RequestBuilder toggleRequest = Helpers.fakeRequest().uri("/api/photo/2/privacy")
            .method("PUT")
            .cookie(adminAuthCookie);
        Result toggleResult = route(fakeApp, toggleRequest);
        assertEquals(OK, toggleResult.status());

        // Get the photo that we toggled
        Http.RequestBuilder photoRequest = Helpers.fakeRequest().uri("/api/user/1/photo")
            .method("GET")
            .cookie(adminAuthCookie);
        Result photoResult = route(fakeApp, photoRequest);
        assertEquals(OK, photoResult.status());

        ObjectMapper mapper = new ObjectMapper();
        PagingResponse<Photo> response =  mapper.convertValue(mapper.readTree(Helpers.contentAsString(photoResult)),
            new TypeReference<PagingResponse<Photo>>(){});

        // Deserialize result to list of photos
        List<Photo> photos = response.data;

        assertEquals(2, photos.size());
        assertEquals(true, photos.get(0).isPublic);
    }

    @Test
    public void togglePhotoPrivacyNoPhoto() {
        // Toggle privacy
        Http.RequestBuilder toggleRequest = Helpers.fakeRequest().uri("/api/photo/5/privacy")
            .method("PUT")
            .cookie(adminAuthCookie);
        Result result = route(fakeApp, toggleRequest);
        assertEquals(NOT_FOUND, result.status());
    }

    @Test
    public void getPhoto() throws IOException {
        // Get photo
        Http.RequestBuilder photoRequest = Helpers.fakeRequest().uri("/api/user/1/photo")
            .method("GET")
            .cookie(adminAuthCookie);
        Result photoResult = route(fakeApp, photoRequest);
        assertEquals(OK, photoResult.status());

        ObjectMapper mapper = new ObjectMapper();
        PagingResponse<Photo> response =  mapper.convertValue(mapper.readTree(Helpers.contentAsString(photoResult)),
            new TypeReference<PagingResponse<Photo>>(){});

        // Deserialize result to list of photos
        List<Photo> photos = response.data;

        assertEquals(2, photos.size());
    }

    @Test
    public void getByPhotoId() throws IOException{
        // Get photo
        Http.RequestBuilder photoRequest = Helpers.fakeRequest().uri("/api/photo/1")
            .method("GET")
            .cookie(adminAuthCookie);
        Result photoResult = route(fakeApp, photoRequest);
        assertEquals(OK, photoResult.status());

        Photo testPhoto = new ObjectMapper().readValue(Helpers.contentAsString(photoResult), Photo.class);
        assertEquals("./public/storage/photos/test/test.jpeg", testPhoto.filename );
    }

    @Test
    public void getByPhotoIdNoPhoto() {
        // Get photo
        Http.RequestBuilder photoRequest = Helpers.fakeRequest().uri("/api/photo/9")
            .method("GET")
            .cookie(adminAuthCookie);
        Result photoResult = route(fakeApp, photoRequest);
        assertEquals(NOT_FOUND, photoResult.status());
    }

    @Test
    public void getCaptionByPhotoId() throws IOException{
        // Get photo
        Http.RequestBuilder photoRequest = Helpers.fakeRequest().uri("/api/photo/1")
            .method("GET")
            .cookie(adminAuthCookie);
        Result photoResult = route(fakeApp, photoRequest);
        assertEquals(OK, photoResult.status());

        Photo testPhoto = new ObjectMapper().readValue(Helpers.contentAsString(photoResult), Photo.class);
        assertEquals("test caption", testPhoto.caption );
    }

    @Test
    public void updatePhotoDetails() throws IOException {
        Tag newTag1 = new Tag("awesome");
        Tag newTag2 = new Tag("biking");
        Set<Tag> tags = new HashSet<>(Arrays.asList(newTag1, newTag2));
        String newCaption = "A new day";

        Photo updateDetails = new Photo();
        updateDetails.caption = newCaption;
        updateDetails.tags = tags;

        // Update photo
        Http.RequestBuilder updateRequest = Helpers.fakeRequest()
            .uri("/api/photo/1")
            .method("PUT")
            .cookie(adminAuthCookie)
            .bodyJson(Json.toJson(updateDetails));

        Result updateResult = route(fakeApp, updateRequest);
        assertEquals(OK, updateResult.status());

        // Check updated details
        Http.RequestBuilder getRequest = Helpers.fakeRequest()
            .uri("/api/photo/1")
            .method("GET")
            .cookie(adminAuthCookie);

        Result getResult = route(fakeApp, getRequest);
        assertEquals(OK, getResult.status());

        Photo photo = new ObjectMapper().readValue(Helpers.contentAsString(getResult), Photo.class);

        assertEquals(newCaption, photo.caption);
        assertEquals(tags.size(), photo.tags.size());
        for (Tag tag : tags) {
            assertTrue(photo.tags.contains(tag));
        }
    }

    @Test
    public void updatePhotoRemoveTags() throws IOException {
        Set<Tag> tags = new HashSet<>();
        String newCaption = "A new day";

        Photo updateDetails = new Photo();
        updateDetails.caption = newCaption;
        updateDetails.tags = tags;

        // Update photo
        Http.RequestBuilder updateRequest = Helpers.fakeRequest()
            .uri("/api/photo/2")
            .method("PUT")
            .cookie(adminAuthCookie)
            .bodyJson(Json.toJson(updateDetails));

        Result updateResult = route(fakeApp, updateRequest);
        assertEquals(OK, updateResult.status());

        // Check updated details
        Http.RequestBuilder getRequest = Helpers.fakeRequest()
            .uri("/api/photo/2")
            .method("GET")
            .cookie(adminAuthCookie);

        Result getResult = route(fakeApp, getRequest);
        assertEquals(OK, getResult.status());

        Photo photo = new ObjectMapper().readValue(Helpers.contentAsString(getResult), Photo.class);

        assertEquals(newCaption, photo.caption);
        assertTrue(photo.tags.isEmpty());
    }

    @Test
    public void updatePhotoChangeTags() throws IOException {
        Tag newTag1 = new Tag("awesome");
        Tag newTag2 = new Tag("biking");
        Tag existingTag = new Tag("sports");
        Set<Tag> tags = new HashSet<>(Arrays.asList(newTag1, newTag2, existingTag));
        String newCaption = "A new day";

        Photo updateDetails = new Photo();
        updateDetails.caption = newCaption;
        updateDetails.tags = tags;

        // Update photo
        Http.RequestBuilder updateRequest = Helpers.fakeRequest()
            .uri("/api/photo/2")
            .method("PUT")
            .cookie(adminAuthCookie)
            .bodyJson(Json.toJson(updateDetails));

        Result updateResult = route(fakeApp, updateRequest);
        assertEquals(OK, updateResult.status());

        // Check updated details
        Http.RequestBuilder getRequest = Helpers.fakeRequest()
            .uri("/api/photo/2")
            .method("GET")
            .cookie(adminAuthCookie);

        Result getResult = route(fakeApp, getRequest);
        assertEquals(OK, getResult.status());

        Photo photo = new ObjectMapper().readValue(Helpers.contentAsString(getResult), Photo.class);

        assertEquals(newCaption, photo.caption);
        assertEquals(tags.size(), photo.tags.size());
        assertTrue(photo.tags.containsAll(tags));
    }

    @Test
    public void updatePhotoInvalidTags() {
        String newCaption = "A new day";

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode json = mapper.createObjectNode();
        json.set("caption", Json.toJson(newCaption));

        // Update photo
        Http.RequestBuilder updateRequest = Helpers.fakeRequest()
            .uri("/api/photo/1")
            .method("PUT")
            .cookie(adminAuthCookie)
            .bodyJson(Json.toJson(json));

        Result updateResult = route(fakeApp, updateRequest);
        assertEquals(BAD_REQUEST, updateResult.status());
    }

    @Test
    public void updatePhotoInvalidCaption() {
        Tag newTag1 = new Tag("awesome");
        Tag newTag2 = new Tag("biking");
        Set<Tag> tags = new HashSet<>(Arrays.asList(newTag1, newTag2));

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode json = mapper.createObjectNode();
        json.set("tags", Json.toJson(tags));

        // Update photo
        Http.RequestBuilder updateRequest = Helpers.fakeRequest()
            .uri("/api/photo/1")
            .method("PUT")
            .cookie(adminAuthCookie)
            .bodyJson(Json.toJson(json));

        Result updateResult = route(fakeApp, updateRequest);
        assertEquals(BAD_REQUEST, updateResult.status());
    }

    @Test
    public void updatePhotoDetailsNotFound() {
        Tag newTag1 = new Tag("awesome");
        Tag newTag2 = new Tag("biking");
        Set<Tag> tags = new HashSet<>(Arrays.asList(newTag1, newTag2));
        String newCaption = "A new day";

        Photo updateDetails = new Photo();
        updateDetails.caption = newCaption;
        updateDetails.tags = tags;

        // Update photo
        Http.RequestBuilder updateRequest = Helpers.fakeRequest()
            .uri("/api/photo/100")
            .method("PUT")
            .cookie(adminAuthCookie)
            .bodyJson(Json.toJson(updateDetails));

        Result updateResult = route(fakeApp, updateRequest);
        assertEquals(NOT_FOUND, updateResult.status());
    }

    @Test
    public void updatePhotoDetailsForbidden() {
        Tag newTag1 = new Tag("awesome");
        Tag newTag2 = new Tag("biking");
        Set<Tag> tags = new HashSet<>(Arrays.asList(newTag1, newTag2));
        String newCaption = "A new day";

        Photo updateDetails = new Photo();
        updateDetails.caption = newCaption;
        updateDetails.tags = tags;

        // Update photo
        Http.RequestBuilder updateRequest = Helpers.fakeRequest()
            .uri("/api/photo/1")
            .method("PUT")
            .cookie(nonAdminAuthCookie)
            .bodyJson(Json.toJson(updateDetails));

        Result updateResult = route(fakeApp, updateRequest);
        assertEquals(FORBIDDEN, updateResult.status());
    }
}
