package steps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.HttpVerbs.PUT;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.GET;
import static play.test.Helpers.POST;
import static play.test.Helpers.route;
import static steps.GenericTestSteps.adminAuthCookie;
import static steps.GenericTestSteps.fakeApp;
import static steps.GenericTestSteps.nonAdminAuthCookie;

import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import java.io.IOException;
import java.util.HashSet;
import models.CountryDefinition;
import models.Destination;
import models.User;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;

public class DestinationTestSteps {

    @Given("I have created a private destination")
    public void i_have_created_a_private_destination() throws IOException {

        Destination destination = new Destination();
        destination.name = "Not The Eiffel Tower";
        destination.destType = "Monument";
        destination.district = "Paris";
        destination.latitude = 48.8583;
        destination.longitude = 2.2945;
        destination.country = new CountryDefinition();
        destination.country.id = 1L;
        destination.user = new User();
        destination.user.id = 2L;
        destination.isPublic = false;
        destination.tags = new HashSet<>();

        // Create request to create a new destination
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(POST)
            .bodyJson(Json.toJson(destination))
            .cookie(nonAdminAuthCookie)
            .uri("/api/destination");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        // Get id of destination
        assertSame(6L, new ObjectMapper()
            .readValue(Helpers.contentAsString(result), Long.class));
    }

    @Given("I have created a public destination")
    public void i_have_created_a_public_destination() throws IOException {

        Destination destination = new Destination();
        destination.name = "Not The Eiffel Tower";
        destination.destType = "Monument";
        destination.district = "Paris";
        destination.latitude = 48.8583;
        destination.longitude = 2.2945;
        destination.country = new CountryDefinition();
        destination.country.id = 1L;
        destination.user = new User();
        destination.user.id = 2L;
        destination.isPublic = true;
        destination.tags = new HashSet<>();

        // Create request to create a new destination
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(POST)
            .bodyJson(Json.toJson(destination))
            .cookie(nonAdminAuthCookie)
            .uri("/api/destination");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        // Get id of destination
        assertSame(6L, new ObjectMapper()
            .readValue(Helpers.contentAsString(result), Long.class));
    }

    @When("I make my destination public")
    public void i_make_my_destination_public() {
        // Create request to make destination public
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(PUT)
            .cookie(nonAdminAuthCookie)
            .uri("/api/destination/makePublic/" + 6);

        // Get result and check it was successfully
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }

    @When("I make the destination with id {int} public")
    public void i_make_my_destination_public(int int1) {
        // Create request to make destination public
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(PUT)
            .cookie(nonAdminAuthCookie)
            .uri("/api/destination/makePublic/" + int1);

        // Get result and check it was successfully
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }

    @Then("The next time i retrieve it, it is public")
    public void the_next_time_i_retrieve_it_it_is_public() throws IOException {
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(nonAdminAuthCookie)
            .uri("/api/destination/" + 6);

        // Check destination is public
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        // Deserialize result to list of destinations
        Destination destinations = new ObjectMapper()
            .readValue(Helpers.contentAsString(result), Destination.class);

        assertTrue(destinations.isPublic);
    }

    @Given("I link the photo with id {int} to the destination with id {int}")
    public void i_link_the_photo_with_id_to_the_destination_with_id(Integer int1, Integer int2) {
        //create request with no body
        Http.RequestBuilder request = Helpers.fakeRequest().uri("/api/destination/" + int2 + "/photo/" + int1)
            .method("PUT")
            .cookie(adminAuthCookie);
        //put and check response
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }
}
