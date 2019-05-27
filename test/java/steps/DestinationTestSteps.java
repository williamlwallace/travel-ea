package steps;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.HttpVerbs.PUT;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.GET;
import static play.test.Helpers.POST;
import static play.test.Helpers.route;
import static steps.GenericTestSteps.authCookie;
import static steps.GenericTestSteps.fakeApp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import models.CountryDefinition;
import models.Destination;
import models.User;
import org.junit.Assert;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;

public class DestinationTestSteps {

    private Long destinationId;

    @Given("I have created a private destination")
    public void i_have_created_a_private_destination() throws IOException {
        // Create new json object node
        ObjectNode node = Json.newObject();
        node.put("name", "Eiffel Tower");
        node.put("_type", "Monument");
        node.put("district", "Paris");
        node.put("latitude", 48.8583);
        node.put("longitude", 2.2945);
        CountryDefinition countryDefinition = new CountryDefinition();
        countryDefinition.id = 1L;
        node.set("country", Json.toJson(countryDefinition));
        User user = new User();
        user.id = 1L;
        node.set("user", Json.toJson(user));

        // Create request to create a new destination
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(POST)
            .bodyJson(node)
            .cookie(authCookie)
            .uri("/api/destination");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        // Get id of destination
        destinationId = new ObjectMapper()
            .readValue(Helpers.contentAsString(result), Long.class);

    }

    @When("I make my destination public")
    public void i_make_my_destination_public() {
        // Create request to make destination public
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(PUT)
            .cookie(authCookie)
            .uri("/api/destination/makePublic/" + destinationId);

        // Get result and check it was successfully
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }

    @When("A public destination is created which is the same as my private destination")
    public void a_public_destination_is_created_which_is_the_same_as_my_private_destination() throws IOException {
        // Create new json object node
        ObjectNode node = Json.newObject();
        node.put("name", "Tower Bridge");
        node.put("_type", "Monument");
        node.put("district", "London");
        node.put("latitude", 51.50333132);
        node.put("longitude", -0.071999712);
        CountryDefinition countryDefinition = new CountryDefinition();
        countryDefinition.id = 1L;
        node.set("country", Json.toJson(countryDefinition));
        User user = new User();
        user.id = 1L;
        node.set("user", Json.toJson(user));

        // Create request to create a new destination
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(POST)
            .bodyJson(node)
            .cookie(authCookie)
            .uri("/api/destination");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        // Get id of destination
        destinationId = new ObjectMapper()
            .readValue(Helpers.contentAsString(result), Long.class);

        // Create request to make destination public
        Http.RequestBuilder privacyRequest = Helpers.fakeRequest()
            .method(PUT)
            .cookie(authCookie)
            .uri("/api/destination/makePublic/" + destinationId);

        // Get result and check it was successfully
        Result privacyResult = route(fakeApp, privacyRequest);
        assertEquals(OK, privacyResult.status());
    }

    @Then("The next time i retrieve it, it is public")
    public void the_next_time_i_retrieve_it_it_is_public() throws IOException {

        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(authCookie)
            .uri("/api/destination/" + destinationId);

        // Check destination is public
        Result result = route(fakeApp, request);

        // Deserialize result to list of destinations
        Destination destinations = new ObjectMapper()
            .readValue(Helpers.contentAsString(result), Destination.class);

        Assert.assertTrue(destinations.isPublic);
    }


    @Then("The next time I retrieve all public destinations, my private destination is not among them")
    public void the_next_time_I_retrieve_all_public_destinations_my_private_destination_is_not_among_them() throws IOException {
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(authCookie)
            .uri("/api/user/destination/1");

        Result result = route(fakeApp, request);

        // Deserialize result to list of destinations
        List<Destination> destinations = Arrays.asList(
            new ObjectMapper().readValue(Helpers.contentAsString(result), Destination[].class));

        Assert.assertTrue(destinations.stream().map(d -> d.id).collect(Collectors.toList()).contains(destinationId));
    }

    @Then("My private destination is automatically merged with the public one")
    public void my_private_destination_is_automatically_merged_with_the_public_one() {
        // Write code here that turns the phrase above into concrete actions
        throw new cucumber.api.PendingException();
    }

    @Then("Any private information on my merged destination remains private")
    public void any_private_information_on_my_merged_destination_remains_private() {
        // Write code here that turns the phrase above into concrete actions
        throw new cucumber.api.PendingException();
    }

}
