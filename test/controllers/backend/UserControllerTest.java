package controllers.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.Application;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;

import java.beans.Transient;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

public class UserControllerTest extends WithApplication {

    Application fakeApp;

    @Before
    public void setUp() {
        fakeApp = Helpers.fakeApplication();
    }

    @Test
    public void getUsers() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/api/user/search");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }

    @Test
    public void createAndDeleteUser() throws IOException {
        // Create new json object node
        ObjectNode node = Json.newObject();
        node.put("username", "test.account@email.com");
        node.put("password", "password");

        // Create request to create a new user
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .bodyJson(node)
                .uri("/api/user");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        // Get UID of newly created user
        Integer uidOfCreatedUser = new ObjectMapper().readValue(Helpers.contentAsString(result), Integer.class);

        // Create request to delete newly created user
        Http.RequestBuilder request2 = Helpers.fakeRequest()
                .method(DELETE)
                .uri("/api/user/" + uidOfCreatedUser);

        // Get result and check it was successful
        Result result2 = route(fakeApp, request2);
        assertEquals(OK, result2.status());
    }

    @Test
    public void createAndDeleteProfile() throws IOException {
        // Create new json object node
        ObjectNode node = Json.newObject();
        node.put("userId", "2");
        node.put("firstName", "John");
        node.put("middleName", "Nobody");
        node.put("lastName", "Smith");
        node.put("gender", "Male");
        node.put("dateOfBirth", "1999-01-01");
        node.put("nationalities", "France");
        node.put("passports", "France");
        node.put("travellerTypes", "backpacker");

        // Create request to create a new user
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(POST)
            .bodyJson(node)
            .uri("/api/profile");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        // Create request to delete newly created user
        Http.RequestBuilder request2 = Helpers.fakeRequest()
                .method(DELETE)
                .uri("/api/profile/2");

        // Get result and check it was successful
        Result result2 = route(fakeApp, request2);
        assertEquals(OK, result2.status());
    }

    @Test
    public void getProfile() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/api/profile/1");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }

    @After
    public void cleanUp() {

    }
}
