package controllers.backend;

import static org.junit.Assert.*;
import static play.mvc.Http.Status.*;
import static play.test.Helpers.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.*;
import org.junit.*;
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

    /**
     * Trip creator to generate trips to be used in tests
     * @param isPublic Trip privacy status
     * @param destinations List of ID's of destinations to use when creating trip
     * @param arrivalTimes List of arrivalTimes to use when creating trip
     * @param departureTimes List of departureTimes to use when creating trip
     * @return Trip object created using given data
     */
    private Trip createTestTripObject(boolean isPublic, int[] destinations, String[] arrivalTimes, String[] departureTimes) {
        // Sets up datetime formatter and country definition object and empty trip data list
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        CountryDefinition countryDefinition = new CountryDefinition();
        countryDefinition.id = 1L;

        List<TripData> tripDataObjects = new ArrayList<>();

        // Iterates through destinations and creates tripData objects based on data inserted in evolutions
        for (int i = 0; i < destinations.length; i++) {
            Destination dest = new Destination();
            dest.id = Long.valueOf(destinations[i]);
            dest.country = countryDefinition;

            TripData tripData = new TripData();
            tripData.position = Long.valueOf(i + 1);    // Ensures the positions iterate from 1 upwards
            tripData.destination = dest;

            // Try set tripData arrival and departure times
            try {
                tripData.arrivalTime = LocalDateTime.parse(arrivalTimes[i], formatter);
            }
            catch (Exception ex) {
                tripData.arrivalTime = null;
            }

            try {
                tripData.departureTime = LocalDateTime.parse(departureTimes[i], formatter);
            }
            catch (Exception ex) {
                tripData.departureTime = null;
            }

            tripDataObjects.add(tripData);
        }

        // Creates trip object and sets fields
        Trip trip = new Trip();
        trip.userId = 1L;
        trip.tripDataList = tripDataObjects;

        trip.isPublic = isPublic;

        return trip;
    }

    @Test
    public void createTrip() {
        boolean isPublic = false;
        int[] destinations = new int[] {1, 2};
        String[] arrivalTimes = new String[] {};
        String[] departureTimes = new String[] {};

        Trip trip = createTestTripObject(isPublic, destinations, arrivalTimes, departureTimes);
        JsonNode node = Json.toJson(trip);
        System.out.println(node);

        // Create request to insert trip
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
        boolean isPublic = false;
        int[] destinations = new int[] {};
        String[] arrivalTimes = new String[] {};
        String[] departureTimes = new String[] {};

        Trip trip = createTestTripObject(isPublic, destinations, arrivalTimes, departureTimes);
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
        boolean isPublic = false;
        int[] destinations = new int[] {1, 1};
        String[] arrivalTimes = new String[] {};
        String[] departureTimes = new String[] {};

        Trip trip = createTestTripObject(isPublic, destinations, arrivalTimes, departureTimes);
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
        boolean isPublic = false;
        int[] destinations = new int[] {1};
        String[] arrivalTimes = new String[] {};
        String[] departureTimes = new String[] {};

        Trip trip = createTestTripObject(isPublic, destinations, arrivalTimes, departureTimes);
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
        // Creates trip object
        boolean isPublic = false;
        int[] destinations = new int[] {1, 2};
        String[] arrivalTimes = new String[] {};
        String[] departureTimes = new String[] {};

        Trip trip = createTestTripObject(isPublic, destinations, arrivalTimes, departureTimes);
        trip.id = 1L;    // Needs to be set to trip created in evolutions
        JsonNode node = Json.toJson(trip);

        // Update trip object inserted in evolutions script
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(PUT)
                .bodyJson(node)
                .cookie(this.authCookie)
                .uri("/api/trip");

        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }

    // TODO: Write tests for updateTrip (change dest order and change arrival departure times

    @Test
    public void updateTripInvalidId() {
        // Create trip object
        boolean isPublic = false;
        int[] destinations = new int[] {1, 2};
        String[] arrivalTimes = new String[] {};
        String[] departureTimes = new String[] {};

        Trip trip = createTestTripObject(isPublic, destinations, arrivalTimes, departureTimes);
        trip.id = 100L;    // Set ID to value which doesn't exist
        JsonNode node = Json.toJson(trip);

        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(PUT)
                .bodyJson(node)
                .cookie(this.authCookie)
                .uri("/api/trip");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(BAD_REQUEST, result.status());
    }

    @Test
    public void getTrip(){
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .cookie(authCookie)
                .uri("/api/trip/1");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }

    @Test
    public void getTripInvalidID() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .cookie(authCookie)
                .uri("/api/trip/100");

        // Get result and check it was not successful
        Result result = route(fakeApp, request);
        assertEquals(NOT_FOUND, result.status());
    }

    @Test
    public void getAllUserTrips() throws IOException {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .cookie(authCookie)
                .uri("/api/user/trips/1");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        JsonNode trips = new ObjectMapper().readValue(Helpers.contentAsString(result), JsonNode.class);
        assertEquals(1, trips.size());    // Because 1 trip inserted in evolutions
    }

    @Test
    public void getAllUserTripsInvalidUserID() throws IOException {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .cookie(authCookie)
                .uri("/api/user/trips/100");

        // Get result and check no trips were returned
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        JsonNode trips = new ObjectMapper().readValue(Helpers.contentAsString(result), JsonNode.class);
        assertEquals(0, trips.size());

    }

    @Test
    public void getAllUserTripsHasNoTrips() throws IOException {
        // Deletes trip added in evolutions
        Http.RequestBuilder deleteRequest = Helpers.fakeRequest()
                .method(DELETE)
                .cookie(authCookie)
                .uri("/api/trip/1");

        Result deleteResult = route(fakeApp, deleteRequest);
        assertEquals(OK, deleteResult.status());

        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .cookie(authCookie)
                .uri("/api/user/trips/1");

        // Get result and check no trips were returned
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        JsonNode trips = new ObjectMapper().readValue(Helpers.contentAsString(result), JsonNode.class);
        assertEquals(0, trips.size());
    }

    @Test
    public void sortTripsByDateIsNewestToOldest() throws IOException {
        List<Trip> trips = new ArrayList<>();

        boolean isPublic = false;
        int[] destinations = new int[] {1, 2};
        String[] arrivalTimes = new String[] {"2019-03-25 00:00:00"};
        String[] departureTimes = new String[] {};

        Trip trip = createTestTripObject(isPublic, destinations, arrivalTimes, departureTimes);
        trips.add(trip);

        isPublic = false;
        destinations = new int[] {1, 2};
        arrivalTimes = new String[] {"2019-04-01 00:00:00"};
        departureTimes = new String[] {};

        trip = createTestTripObject(isPublic, destinations, arrivalTimes, departureTimes);
        trips.add(trip);

        isPublic = false;
        destinations = new int[] {1, 2};
        arrivalTimes = new String[] {"2019-03-29 00:00:00", "2019-10-10 00:00:00"};
        departureTimes = new String[] {};

        trip = createTestTripObject(isPublic, destinations, arrivalTimes, departureTimes);
        trips.add(trip);

        // Insert trips into database
        for (Trip tripToInsert : trips) {
            JsonNode node = Json.toJson(tripToInsert);
            System.out.println(node);

            Http.RequestBuilder insertRequest = Helpers.fakeRequest()
                    .method(POST)
                    .bodyJson(node)
                    .cookie(this.authCookie)
                    .uri("/api/trip");

            Result insertResult = route(fakeApp, insertRequest);
            assertEquals(OK, insertResult.status());
        }

        // Get trip from database and check success
        Http.RequestBuilder getRequest = Helpers.fakeRequest()
                .method(GET)
                .cookie(authCookie)
                .uri("/api/user/trips/1");

        Result getResult = route(fakeApp, getRequest);
        assertEquals(OK, getResult.status());

        JsonNode tripsJson = new ObjectMapper().readValue(Helpers.contentAsString(getResult), JsonNode.class);
        trips = new ObjectMapper().readValue(new ObjectMapper().treeAsTokens(tripsJson), new TypeReference<List<Trip>>() {});
        assertFalse(trips.isEmpty());

        for (int i = 0; i < trips.size() - 1; i++) {
            // If both not null, check index i date is after index i+1 date
            if (trips.get(i).findFirstTripDate() != null && trips.get(i+1).findFirstTripDate() != null) {
                assertTrue(trips.get(i).findFirstTripDate().compareTo(trips.get(i).findFirstTripDate()) <= 0);
            }
            // Else ensure index i+1 date is null, if this is not null then index i will be null and it will not be ordered correctly
            else {
                assertNull(trips.get(i+1).findFirstTripDate());
            }
        }
    }

    @Test
    public void sortTripsByDateIsNullLast() throws IOException {
        List<Trip> trips = new ArrayList<>();

        boolean isPublic = false;
        int[] destinations = new int[] {1, 2};
        String[] arrivalTimes = new String[] {};
        String[] departureTimes = new String[] {};

        Trip trip = createTestTripObject(isPublic, destinations, arrivalTimes, departureTimes);
        trips.add(trip);

        isPublic = false;
        destinations = new int[] {1, 2};
        arrivalTimes = new String[] {"2019-04-01 00:00:00"};
        departureTimes = new String[] {};

        trip = createTestTripObject(isPublic, destinations, arrivalTimes, departureTimes);
        trips.add(trip);

        isPublic = false;
        destinations = new int[] {1, 2};
        arrivalTimes = new String[] {};
        departureTimes = new String[] {};

        trip = createTestTripObject(isPublic, destinations, arrivalTimes, departureTimes);
        trips.add(trip);

        // Insert trips into database
        for (Trip tripToInsert : trips) {
            JsonNode node = Json.toJson(tripToInsert);

            Http.RequestBuilder insertRequest = Helpers.fakeRequest()
                    .method(POST)
                    .bodyJson(node)
                    .cookie(this.authCookie)
                    .uri("/api/trip");

            Result insertResult = route(fakeApp, insertRequest);
            assertEquals(OK, insertResult.status());
        }

        // Get trip from database and check success
        Http.RequestBuilder getRequest = Helpers.fakeRequest()
                .method(GET)
                .cookie(authCookie)
                .uri("/api/user/trips/1");

        Result getResult = route(fakeApp, getRequest);
        assertEquals(OK, getResult.status());

        JsonNode tripsJson = new ObjectMapper().readValue(Helpers.contentAsString(getResult), JsonNode.class);
        trips = new ObjectMapper().readValue(new ObjectMapper().treeAsTokens(tripsJson), new TypeReference<List<Trip>>() {});
        assertFalse(trips.isEmpty());

        for (int i = 0; i < trips.size() - 1; i++) {
            // If both not null, check index i date is after index i+1 date
            if (trips.get(i).findFirstTripDate() != null && trips.get(i+1).findFirstTripDate() != null) {
                assertTrue(trips.get(i).findFirstTripDate().compareTo(trips.get(i).findFirstTripDate()) <= 0);
            }
            // Else ensure index i+1 date is null, if this is not null then index i will be null and it will not be ordered correctly
            else {
                assertNull(trips.get(i+1).findFirstTripDate());
            }
        }
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
