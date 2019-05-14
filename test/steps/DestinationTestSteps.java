package steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import models.CountryDefinition;
import models.Destination;
import org.junit.Assert;
import play.Application;
import play.db.Database;
import play.db.evolutions.Evolutions;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;

import javax.validation.constraints.AssertTrue;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.HttpVerbs.PUT;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.GET;
import static play.test.Helpers.POST;
import static play.test.Helpers.route;

public class DestinationTestSteps {

    private static Application fakeApp;
    private static Database db;
    private static Http.Cookie authCookie;

    private Long destinationId;

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

        // Create request to create a new destination
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .bodyJson(node)
                .cookie(this.authCookie)
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
                .cookie(this.authCookie)
                .uri("/api/destination/makePublic/1");

        // Get result and check it was successfully
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }

    @Then("The next time i retrieve it, it is public")
    public void the_next_time_i_retrieve_it_it_is_public() throws IOException {

        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .cookie(this.authCookie)
                .uri("/api/destination/" + destinationId);

        // Check destination is public
        Result result = route(fakeApp, request);

        // Deserialize result to list of destinations
        Destination destinations = new ObjectMapper().readValue(Helpers.contentAsString(result), Destination.class);

        Assert.assertTrue(destinations.isPublic);

    }


    @Then("The next time I retrieve all public destinations, my private destination is not among them")
    public void the_next_time_I_retrieve_all_public_destinations_my_private_destination_is_not_among_them() throws IOException {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .cookie(this.authCookie)
                .uri("/api/destination");

        Result result = route(fakeApp, request);

        // Deserialize result to list of destinations
        List<Destination> destinations = Arrays.asList(
                new ObjectMapper().readValue(Helpers.contentAsString(result), Destination[].class));

        Assert.assertTrue(destinations.stream().map(d -> d.id).collect(Collectors.toList()).contains(destinationId));

    }

}
