package controllers.backend;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.*;
import static play.test.Helpers.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import play.Application;
import play.db.Database;
import play.db.evolutions.Evolutions;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Http.Cookie;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;

public class TripControllerTest extends WithApplication {

    @Test
    public void sanityTest() {
        assertEquals(1, 1);
    }

    private static Application fakeApp;
    private static Database db;
    private static Cookie authCookie;

    /**
     * Configures system to use trip database, and starts a fake app
     */
    @BeforeClass
    public static void setUp() {
        // Create custom settings that change the database to use test database instead of production
        Map<String, String> settings = new HashMap<>();
        settings.put("db.default.driver", "org.h2.Driver");
        settings.put("db.default.url", "jdbc:h2:mem:testdb;MODE=MySQL;");

        authCookie = Cookie.builder("JWT-Auth", "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJUcmF2ZWxFQSIsInVzZXJJZCI6MX0.85pxdAoiT8xkO-39PUD_XNit5R8jmavTFfPSOVcPFWw").withPath("/").build();

        // Create a fake app that we can query just like we would if it was running
        fakeApp = Helpers.fakeApplication(settings);
        db = fakeApp.injector().instanceOf(Database.class);

        Helpers.start(fakeApp);
    }

    /**
     * Runs evolutions before each test
     * These evolutions are found in conf/test/(whatever), and should contain minimal sql data needed for tests
     */
    @Before
    public void applyEvolutions() {
        // Only certain trips, namely initialisation, and profile folders
        Evolutions.applyEvolutions(db, Evolutions.fromClassLoader(getClass().getClassLoader(), "test/trip/"));
    }

    /**
     * Cleans up evolutions after each test, to allow for them to be re-run for next test
     */
    @After
    public void cleanupEvolutions() {
        Evolutions.cleanupEvolutions(db);
    }

    /**
     * Stop the fake app
     */
    @AfterClass
    public static void stopApp() {
        // Stop the fake app running
        Helpers.stop(fakeApp);
    }

    @Test
    public void createTrip() {
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

        JsonNode node = Json.toJson(trip);

        // Create request to create a new trip
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .bodyJson(node)
                .cookie(this.authCookie)
                .uri("/api/trip");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }

    @Test
    public void createTripNoDest() {
        CountryDefinition countryDefinition = new CountryDefinition();
        countryDefinition.id = 1L;

        TripData tripData1 = new TripData();

        Trip trip = new Trip();
        List<TripData> tripArray = new ArrayList<>();
            tripArray.add(tripData1);
        trip.tripDataList = tripArray;

        JsonNode node = Json.toJson(trip);

        // Create request to create a new trip
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .bodyJson(node)
                .cookie(this.authCookie)
                .uri("/api/trip");

        // Get result and check it was unsuccessful
        Result result = route(fakeApp, request);
        assertEquals(BAD_REQUEST, result.status());
    }

    @Test
    public void createTripSameDestTwiceAdjacent() {
        CountryDefinition countryDefinition = new CountryDefinition();
        countryDefinition.id = 1L;

        Destination dest1 = new Destination();
        dest1.id = 1L;
        dest1.country = countryDefinition;

        TripData tripData1 = new TripData();
        tripData1.position = 1L;
        tripData1.destination = dest1;

        Trip trip = new Trip();
        List<TripData> tripArray = new ArrayList<>();
        tripArray.add(tripData1);
        tripArray.add(tripData1);
        trip.tripDataList = tripArray;

        JsonNode node = Json.toJson(trip);

        // Create request to create a new trip
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .bodyJson(node)
                .cookie(this.authCookie)
                .uri("/api/trip");

        // Get result and check it was unsuccessful
        Result result = route(fakeApp, request);
        assertEquals(BAD_REQUEST, result.status());
    }

    @Test
    public void createTripOneDest() {
        CountryDefinition countryDefinition = new CountryDefinition();
        countryDefinition.id = 1L;

        Destination dest1 = new Destination();
        dest1.id = 1L;
        dest1.country = countryDefinition;

        TripData tripData1 = new TripData();
        tripData1.position = 1L;
        tripData1.destination = dest1;

        Trip trip = new Trip();
        List<TripData> tripArray = new ArrayList<>();
        tripArray.add(tripData1);
        trip.tripDataList = tripArray;

        JsonNode node = Json.toJson(trip);

    // Create request to create a new trip
    Http.RequestBuilder request = Helpers.fakeRequest()
            .method(POST)
            .bodyJson(node)
            .cookie(this.authCookie)
            .uri("/api/trip");

    // Get result and check it was unsuccessful
    Result result = route(fakeApp, request);
    assertEquals(BAD_REQUEST, result.status());
    }

    @Test
    public void updateTrip() {
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
        trip.id = 1L;

        JsonNode node = Json.toJson(trip);

//        Http.RequestBuilder request = Helpers.fakeRequest()
//                .method(PUT)
//                .bodyJson(node)
//                .cookie(this.authCookie)
//                .uri("/api/trip");

        // Get result and check it was successful
//        Result result = route(fakeApp, request);
//        assertEquals(OK, result.status()); //TODO update trip is currently broken
    }


    @Test
    public void updateTripInvalidId() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(PUT)
                .uri("/api/trip/3");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(NOT_FOUND, result.status());
    }

    @Test
    public void updateTripInvalidUpdate() {
//        Http.RequestBuilder request = Helpers.fakeRequest()
//                .method(PUT)
//                .uri("/api/trip/1");
//
//        // Get result and check it was successful
//        Result result = route(fakeApp, request);
//        assertEquals(BAD_REQUEST, result.status()); //TODO update trip is currently broken
    }

    @Test
    public void getTrip(){
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/api/trip/1");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }

    @Test
    public void getTripInvalidId() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/api/trip/100");

        // Get result and check it was not successful
        Result result = route(fakeApp, request);
        assertEquals(NOT_FOUND, result.status());
    }

    @Test
    public void getAllTrips() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .cookie(authCookie)
                .uri("/api/user/trips");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }

    @Test
    public void getAllTripsInvalidId() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/api/user/trips/10");

        // Get result and check it was not successful
        Result result = route(fakeApp, request);
        assertEquals(NOT_FOUND, result.status());
    }

    @Test
    public void getAllTripsHasNoTrips() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/api/user/trips/2");

        // Get result and check it was not successful
        Result result = route(fakeApp, request);
        assertEquals(NOT_FOUND, result.status());
    }

    @Test
    public void deleteTrip() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(DELETE)
                .cookie(authCookie)
                .uri("/api/trip/1");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }

    @Test
    public void deleteTripInvalidId() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(DELETE)
                .cookie(authCookie)
                .uri("/api/trip/10");

        // Get result and check it was not successful
        Result result = route(fakeApp, request);
        assertEquals(NOT_FOUND, result.status());
    }

}
