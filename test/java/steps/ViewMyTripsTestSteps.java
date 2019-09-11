package steps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static play.mvc.Http.HttpVerbs.PUT;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.GET;
import static play.test.Helpers.POST;
import static play.test.Helpers.route;
import static steps.GenericTestSteps.adminAuthCookie;
import static steps.GenericTestSteps.fakeApp;
import static steps.GenericTestSteps.userId;

import com.fasterxml.jackson.core.type.TypeReference;
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
import util.objects.PagingResponse;


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
            .cookie(adminAuthCookie)
            .uri("/api/trip?userId=" + userId);

        Result result = route(fakeApp, request);

        JsonNode tripsJson = new ObjectMapper()
            .readValue(Helpers.contentAsString(result), JsonNode.class);

        PagingResponse<Trip> pagingResponse = new ObjectMapper()
            .readValue(new ObjectMapper().treeAsTokens(tripsJson),
                new TypeReference<PagingResponse<Trip>>() {
                });

        List<Trip> trips = pagingResponse.data;

        for (Trip trip : trips) {
            Http.RequestBuilder deleteRequest = Helpers.fakeRequest()
                .method(PUT)
                .cookie(adminAuthCookie)
                .uri("/api/trip/" + trip.id + "/delete");

            route(fakeApp, deleteRequest);
        }

        Http.RequestBuilder checkEmptyRequest = Helpers.fakeRequest()
            .method(GET)
            .cookie(adminAuthCookie)
            .uri("/api/trip?userId=" + userId);

        Result checkEmptyResult = route(fakeApp, checkEmptyRequest);

        JsonNode checkEmptyTrips = new ObjectMapper()
            .readValue(Helpers.contentAsString(checkEmptyResult), JsonNode.class);

        PagingResponse<Trip> emptyPagingResponse = new ObjectMapper()
            .readValue(new ObjectMapper().treeAsTokens(checkEmptyTrips),
                new TypeReference<PagingResponse<Trip>>() {
                });

        List<Trip> emptyTrips = emptyPagingResponse.data;

        assertEquals(0, emptyTrips.size());
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
        trip.isPublic = false;

        JsonNode node = Json.toJson(trip);

        // Create request to create a new trip
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(POST)
            .bodyJson(node)
            .cookie(adminAuthCookie)
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
            .cookie(adminAuthCookie)
            .uri("/api/trip?userId=" + userId);

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }

    @Then("a list of trips is shown")
    public void a_list_of_trips_is_shown() throws IOException {
        // Create request to view trips
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(adminAuthCookie)
            .uri("/api/trip?userId=" + userId);

        // Deserialize result to list of trips
        Result result = route(fakeApp, request);

        JsonNode json = new ObjectMapper()
            .readValue(Helpers.contentAsString(result), JsonNode.class);

        PagingResponse<Trip> pagingResponse = new ObjectMapper()
            .readValue(new ObjectMapper().treeAsTokens(json),
                new TypeReference<PagingResponse<Trip>>() {
                });

        assertFalse(pagingResponse.data.isEmpty());
    }

    @And("it shows all of my trips")
    public void it_shows_all_of_my_trips() throws IOException {
        // Create request to view trips
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(adminAuthCookie)
            .uri("/api/trip?userId=" + userId);

        // Deserialize result to list of trips
        Result result = route(fakeApp, request);

        JsonNode json = new ObjectMapper()
            .readValue(Helpers.contentAsString(result), JsonNode.class);

        PagingResponse<Trip> pagingResponse = new ObjectMapper()
            .readValue(new ObjectMapper().treeAsTokens(json),
                new TypeReference<PagingResponse<Trip>>() {
                });

        List<Trip> trips = pagingResponse.data;

        assertEquals(1, trips.size());
    }

    @And("only my trips")
    public void only_my_trips() throws IOException {
        // Create request to view trips
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(adminAuthCookie)
            .uri("/api/trip?userId=" + userId);

        // Deserialize result to list of trips
        Result result = route(fakeApp, request);

        JsonNode json = new ObjectMapper()
            .readValue(Helpers.contentAsString(result), JsonNode.class);

        PagingResponse<Trip> pagingResponse = new ObjectMapper()
            .readValue(new ObjectMapper().treeAsTokens(json),
                new TypeReference<PagingResponse<Trip>>() {
                });

        List<Trip> trips = pagingResponse.data;

        for (Trip trip : trips) {
            assertEquals(userId, trip.userId);
        }
    }
}
