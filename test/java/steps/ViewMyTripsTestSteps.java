package steps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.GET;
import static play.test.Helpers.POST;
import static play.test.Helpers.DELETE;
import static play.test.Helpers.route;
import static steps.GenericTestSteps.authCookie;
import static steps.GenericTestSteps.fakeApp;
import static steps.GenericTestSteps.userId;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import models.CountryDefinition;
import models.Destination;
import models.Trip;
import models.TripData;

import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;


public class ViewMyTripsTestSteps extends WithApplication {

    @Given("viewing my profile")
    public void viewing_my_profile() {
        // Create request to view profile
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .uri("/api/profile/" + userId);

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }

    @Given("I have no trips")
    public void i_have_no_trips() throws IOException {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .cookie(authCookie)
                .uri("/api/user/trips/" + userId);

        Result result = route(fakeApp, request);
        JsonNode trips = new ObjectMapper()
                .readValue(Helpers.contentAsString(result), JsonNode.class);

        for (int i = 0; i < trips.size(); i++) {
            Http.RequestBuilder deleteRequest = Helpers.fakeRequest()
                    .method(DELETE)
                    .cookie(authCookie)
                    .uri("/api/trip/" + trips.get(i).get("id"));

            Result deleteResult = route(fakeApp, deleteRequest);
        }

        Http.RequestBuilder checkEmptyRequest = Helpers.fakeRequest()
                .method(GET)
                .cookie(authCookie)
                .uri("/api/user/trips/" + userId);

        Result checkEmptyResult = route(fakeApp, checkEmptyRequest);
        JsonNode checkEmptyTrips = new ObjectMapper()
                .readValue(Helpers.contentAsString(checkEmptyResult), JsonNode.class);

        assertEquals(0, checkEmptyTrips.size());
    }


    @Given("I have created some trips")
    public void have_created_some_trips() {
        CountryDefinition countryDefinition = new CountryDefinition();
        countryDefinition.id = 1L;

        Destination dest1 = new Destination();
        dest1.id = 1L;
        dest1.country = countryDefinition;

        Destination dest2 = new Destination();
        dest2.id = 2L;
        dest2.country = countryDefinition;

        TripData tripData1 = new TripData();
        tripData1.position = 1L;
        tripData1.destination = dest1;

        TripData tripData2 = new TripData();
        tripData2.position = 2L;
        tripData2.destination = dest2;

        Trip trip = new Trip();
        List<TripData> tripArray = new ArrayList<>();
        tripArray.add(tripData1);
        tripArray.add(tripData2);
        trip.tripDataList = tripArray;
        trip.userId = userId;
        trip.privacy = 0L;

        JsonNode node = Json.toJson(trip);

        // Create request to create a new trip
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .bodyJson(node)
                .cookie(authCookie)
                .uri("/api/trip");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }

    @When("I click view my trips")
    public void i_click_view_my_trips() {
        // Create request to get trips
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(authCookie)
            .uri("/api/user/trips/" + userId);

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }

    @Then("a list of trips is shown")
    public void a_list_of_trips_is_shown() throws IOException {
        // Create request to view trips
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(authCookie)
            .uri("/api/user/trips/" + userId);

        // Deserialize result to list of trips
        Result result = route(fakeApp, request);
        JsonNode trips = new ObjectMapper()
            .readValue(Helpers.contentAsString(result), JsonNode.class);
        assertTrue(trips.get(0).get("tripDataList") != null);
    }

    @And("it shows all of my trips")
    public void it_shows_all_of_my_trips() throws IOException {
        // Create request to view trips
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(authCookie)
            .uri("/api/user/trips/" + userId);

        // Deserialize result to list of trips
        Result result = route(fakeApp, request);
        JsonNode trips = new ObjectMapper()
            .readValue(Helpers.contentAsString(result), JsonNode.class);
        assertEquals(1, trips.size());
    }

    @And("only my trips")
    public void only_my_trips() throws IOException {
        // Create request to view trips
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(authCookie)
            .uri("/api/user/trips/" + userId);

        // Deserialize result to list of trips
        Result result = route(fakeApp, request);
        JsonNode trips = new ObjectMapper()
            .readValue(Helpers.contentAsString(result), JsonNode.class);
        assertEquals(1, trips.get(0).get("userId").asInt());
    }
}
