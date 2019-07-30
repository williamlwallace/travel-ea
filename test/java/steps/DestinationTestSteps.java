package steps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.HttpVerbs.PUT;
import static play.mvc.Http.Status.NOT_FOUND;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.GET;
import static play.test.Helpers.POST;
import static play.test.Helpers.route;
import static steps.GenericTestSteps.adminAuthCookie;
import static steps.GenericTestSteps.fakeApp;
import static steps.GenericTestSteps.nonAdminAuthCookie;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import models.CountryDefinition;
import models.Destination;
import models.TravellerTypeDefinition;
import models.Trip;
import models.TripData;
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
        assertSame(5L, new ObjectMapper()
            .readValue(Helpers.contentAsString(result), Long.class));

        //Create a (private) trip using this destination
        Trip trip = new Trip();
        trip.userId = 2L;
        trip.isPublic = false;
        List<TripData> tripData = new ArrayList<>();

        TripData tripData1 = new TripData();
        tripData1.trip = trip;
        tripData1.position = 1L;
        tripData1.destination = destination;

        Http.RequestBuilder destRequest = Helpers.fakeRequest()
            .method(GET)
            .cookie(adminAuthCookie)
            .uri("/api/destination/1");

        Result destResult = route(fakeApp, destRequest);
        assertEquals(OK, destResult.status());

        Destination destination2 = new ObjectMapper()
            .readValue(Helpers.contentAsString(destResult), Destination.class);

        System.out.println(destination2.id + " " + destination2.name);

        TripData tripData2 = new TripData();
        tripData2.trip = trip;
        tripData2.position = 2L;
        tripData2.destination = destination2;

        tripData.add(tripData1);
        tripData.add(tripData2);

        trip.tripDataList = tripData;


        Http.RequestBuilder tripRequest = Helpers.fakeRequest()
            .method(POST)
            .cookie(nonAdminAuthCookie)
            .bodyJson(Json.toJson(trip))
            .uri("/api/trip");

        Result tripResult = route(fakeApp, tripRequest);
        assertEquals(OK, tripResult.status());

//        //Add a traveller type
//        Http.RequestBuilder requestTraveller = Helpers.fakeRequest()
//            .method(PUT)
//            .bodyJson(Json.toJson(destination))
//            .cookie(nonAdminAuthCookie)
//            .uri("/api/destination/5/travellertype/1/add");
//
//        Result resultTraveller = route(fakeApp, requestTraveller);
//        assertEquals(OK, resultTraveller.status());
//
//        //Check it was added correctly
//        Http.RequestBuilder requestCheck = Helpers.fakeRequest()
//            .method(GET)
//            .cookie(nonAdminAuthCookie)
//            .uri("/api/destination/" + 5);
//
//        Result resultCheck = route(fakeApp, requestCheck);
//
//        assertEquals(OK, resultCheck.status());
//        Destination destinationCheck = new ObjectMapper()
//            .readValue(Helpers.contentAsString(resultCheck), Destination.class);
//
//        boolean found = false;
//        for (TravellerTypeDefinition travellerType : destinationCheck.travellerTypes) {
//            if (travellerType.id == 1L) {
//                found = true;
//                break;
//            }
//        }
//        assertTrue(found);

    }

    @When("I make my destination public")
    public void i_make_my_destination_public() {
        // Create request to make destination public
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(PUT)
            .cookie(nonAdminAuthCookie)
            .uri("/api/destination/makePublic/" + 5);

        // Get result and check it was successfully
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }

    @When("A public destination is created which is the same as my private destination")
    public void a_public_destination_is_created_which_is_the_same_as_my_private_destination()
        throws IOException {
        Destination destination = new Destination();
        destination.name = "Not The Eiffel Tower";
        destination.destType = "Monument";
        destination.district = "Paris";
        destination.latitude = 48.8583;
        destination.longitude = 2.2945;
        destination.country = new CountryDefinition();
        destination.country.id = 1L;
        destination.user = new User();
        destination.user.id = 1L;
        destination.isPublic = false;

        // Create request to create a new destination
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(POST)
            .bodyJson(Json.toJson(destination))
            .cookie(adminAuthCookie)
            .uri("/api/destination");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        // Get id of destination
        assertSame(6L, new ObjectMapper()
            .readValue(Helpers.contentAsString(result), Long.class));

        // Create request to make destination public
        Http.RequestBuilder requestPublic = Helpers.fakeRequest()
            .method(PUT)
            .cookie(adminAuthCookie)
            .uri("/api/destination/makePublic/" + 6);

        // Get result and check it was successfully
        Result resultPublic = route(fakeApp, requestPublic);
        assertEquals(OK, resultPublic.status());
    }

    @Then("The next time i retrieve it, it is public")
    public void the_next_time_i_retrieve_it_it_is_public() throws IOException {
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(nonAdminAuthCookie)
            .uri("/api/destination/" + 5);

        // Check destination is public
        Result result = route(fakeApp, request);

        // Deserialize result to list of destinations
        Destination destinations = new ObjectMapper()
            .readValue(Helpers.contentAsString(result), Destination.class);

        assertTrue(destinations.isPublic);
    }


    @Then("The next time I retrieve all public destinations, my private destination is not among them")
    public void the_next_time_I_retrieve_all_public_destinations_my_private_destination_is_not_among_them()
        throws IOException {
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(nonAdminAuthCookie)
            .uri("/api/destination/getAllPublic");

        Result result = route(fakeApp, request);

        assertEquals(OK, result.status());

        // Deserialize result to list of destinations
        List<Destination> destinations = Arrays.asList(
            new ObjectMapper().readValue(Helpers.contentAsString(result), Destination[].class));

        assertFalse(destinations.stream().map(d -> d.id).collect(Collectors.toList())
            .contains(5L));
    }

    @Then("My private destination is automatically merged with the public one")
    public void my_private_destination_is_automatically_merged_with_the_public_one() {
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(nonAdminAuthCookie)
            .uri("/api/destination/" + 5);

        Result result = route(fakeApp, request);
        assertEquals(NOT_FOUND, result.status());
    }

    @Then("Any private information on my merged destination remains private")
    public void any_private_information_on_my_merged_destination_remains_private()
        throws IOException {
        //Check it was added correctly
        Http.RequestBuilder requestCheck = Helpers.fakeRequest()
            .method(GET)
            .cookie(nonAdminAuthCookie)
            .uri("/api/destination/" + 6);

        Result resultCheck = route(fakeApp, requestCheck);

        assertEquals(OK, resultCheck.status());
        Destination destinationCheck = new ObjectMapper()
            .readValue(Helpers.contentAsString(resultCheck), Destination.class);

        boolean found = false;
        for (TravellerTypeDefinition travellerType : destinationCheck.travellerTypes) {
            if (travellerType.id == 1L) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

}
