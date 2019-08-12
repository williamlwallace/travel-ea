package controllers.backend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.NOT_FOUND;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.DELETE;
import static play.test.Helpers.GET;
import static play.test.Helpers.POST;
import static play.test.Helpers.PUT;
import static play.test.Helpers.route;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import models.CountryDefinition;
import models.Destination;
import models.Trip;
import models.TripData;
import org.junit.Before;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;

public class TripControllerTest extends controllers.backend.ControllersTest {

    /**
     * Runs evolutions before each test These evolutions are found in conf/test/(whatever), and
     * should contain minimal sql data needed for tests
     */
    @Before
    public void runEvolutions() {
        applyEvolutions("test/trip/");
    }

    /**
     * Trip creator to generate trips to be used in tests
     *
     * @param isPublic Trip privacy status
     * @param destinations List of ID's of destinations to use when creating trip
     * @param arrivalTimes List of arrivalTimes to use when creating trip
     * @param departureTimes List of departureTimes to use when creating trip
     * @return Trip object created using given data
     */
    private Trip createTestTripObject(boolean isPublic, int[] destinations, String[] arrivalTimes,
        String[] departureTimes) {
        // Sets up datetime formatter and country definition object and empty trip data list
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        CountryDefinition countryDefinition = new CountryDefinition();
        countryDefinition.id = 1L;

        List<TripData> tripDataObjects = new ArrayList<>();

        // Iterates through destinations and creates tripData objects based on data inserted in evolutions
        for (int i = 0; i < destinations.length; i++) {
            Destination dest = new Destination();
            dest.id = (long) destinations[i];
            dest.country = countryDefinition;

            TripData tripData = new TripData();
            tripData.position = i + 1L;    // Ensures the positions iterate from 1 upwards
            tripData.destination = dest;

            // Try set tripData arrival and departure times
            try {
                tripData.arrivalTime = LocalDateTime.parse(arrivalTimes[i], formatter);
            } catch (Exception ex) {
                tripData.arrivalTime = null;
            }

            try {
                tripData.departureTime = LocalDateTime.parse(departureTimes[i], formatter);
            } catch (Exception ex) {
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
        int[] destinations = new int[]{1, 2};
        String[] arrivalTimes = new String[]{};
        String[] departureTimes = new String[]{};

        Trip trip = createTestTripObject(false, destinations, arrivalTimes, departureTimes);
        JsonNode node = Json.toJson(trip);

        // Create request to insert trip
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(POST)
            .bodyJson(node)
            .cookie(adminAuthCookie)
            .uri("/api/trip");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }

    @Test
    public void createTripNoDest() {
        int[] destinations = new int[]{};
        String[] arrivalTimes = new String[]{};
        String[] departureTimes = new String[]{};

        Trip trip = createTestTripObject(false, destinations, arrivalTimes, departureTimes);
        JsonNode node = Json.toJson(trip);

        // Create request to create a new trip
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(POST)
            .bodyJson(node)
            .cookie(adminAuthCookie)
            .uri("/api/trip");

        // Get result and check it was unsuccessful
        Result result = route(fakeApp, request);
        assertEquals(BAD_REQUEST, result.status());
    }

    @Test
    public void createTripSameDestTwiceAdjacent() {
        int[] destinations = new int[]{1, 1};
        String[] arrivalTimes = new String[]{};
        String[] departureTimes = new String[]{};

        Trip trip = createTestTripObject(false, destinations, arrivalTimes, departureTimes);
        JsonNode node = Json.toJson(trip);

        // Create request to create a new trip
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(POST)
            .bodyJson(node)
            .cookie(adminAuthCookie)
            .uri("/api/trip");

        // Get result and check it was unsuccessful
        Result result = route(fakeApp, request);
        assertEquals(BAD_REQUEST, result.status());
    }

    @Test
    public void createTripOneDest() {
        int[] destinations = new int[]{1};
        String[] arrivalTimes = new String[]{};
        String[] departureTimes = new String[]{};

        Trip trip = createTestTripObject(false, destinations, arrivalTimes, departureTimes);
        JsonNode node = Json.toJson(trip);

        // Create request to create a new trip
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(POST)
            .bodyJson(node)
            .cookie(adminAuthCookie)
            .uri("/api/trip");

        // Get result and check it was unsuccessful
        Result result = route(fakeApp, request);
        assertEquals(BAD_REQUEST, result.status());
    }

    @Test
    public void updateTrip() throws IOException {
        // Creates trip object
        int[] destinations = new int[]{1, 2};
        String[] arrivalTimes = new String[]{null, null};
        String[] departureTimes = new String[]{null, null};

        Trip trip = createTestTripObject(false, destinations, arrivalTimes, departureTimes);
        trip.id = 1L;    // Needs to be set to trip created in evolutions
        JsonNode node = Json.toJson(trip);

        // Update trip object inserted in evolutions script
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(PUT)
            .bodyJson(node)
            .cookie(adminAuthCookie)
            .uri("/api/trip");

        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        // Get trip and check data
        Http.RequestBuilder getRequest = Helpers.fakeRequest()
            .method(GET)
            .bodyJson(node)
            .cookie(adminAuthCookie)
            .uri("/api/trip/1");

        Result getResult = route(fakeApp, getRequest);
        assertEquals(OK, result.status());

        JsonNode json = new ObjectMapper()
            .readValue(Helpers.contentAsString(getResult), JsonNode.class);
        Trip retrieved = new ObjectMapper()
            .readValue(new ObjectMapper().treeAsTokens(json), new TypeReference<Trip>() {
            });

        for (int i = 0; i < retrieved.tripDataList.size(); i++) {
            TripData tripData = trip.tripDataList.get(i);

            assertEquals(Long.valueOf(destinations[i]), tripData.destination.id);
            assertNull(tripData.arrivalTime);
            assertNull(tripData.departureTime);
        }
    }

    @Test
    public void updateTripDatesAndTimes() throws IOException {
        int[] destinations = new int[]{1, 2};
        String[] arrivalTimes = new String[]{"2018-05-10 14:10:00", "2018-05-13 13:25:00"};
        String[] departureTimes = new String[]{"2018-05-13 09:00:00", "2018-05-27 23:15:00"};

        Trip trip = createTestTripObject(false, destinations, arrivalTimes, departureTimes);
        trip.id = 1L;    // Needs to be set to trip created in evolutions
        JsonNode node = Json.toJson(trip);

        // Update trip object inserted in evolutions script
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(PUT)
            .bodyJson(node)
            .cookie(adminAuthCookie)
            .uri("/api/trip");

        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        // Get trip and check data
        Http.RequestBuilder getRequest = Helpers.fakeRequest()
            .method(GET)
            .bodyJson(node)
            .cookie(adminAuthCookie)
            .uri("/api/trip/1");

        Result getResult = route(fakeApp, getRequest);
        assertEquals(OK, result.status());

        JsonNode json = new ObjectMapper()
            .readValue(Helpers.contentAsString(getResult), JsonNode.class);
        Trip retrieved = new ObjectMapper()
            .readValue(new ObjectMapper().treeAsTokens(json), new TypeReference<Trip>() {
            });

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (int i = 0; i < retrieved.tripDataList.size(); i++) {
            TripData tripData = trip.tripDataList.get(i);
            LocalDateTime actualArrTime = LocalDateTime.parse(arrivalTimes[i], formatter);
            LocalDateTime actualDepTime = LocalDateTime.parse(departureTimes[i], formatter);

            assertEquals(Long.valueOf(destinations[i]), tripData.destination.id);
            assertEquals(actualArrTime, tripData.arrivalTime);
            assertEquals(actualDepTime, tripData.departureTime);
        }
    }

    @Test
    public void updateTripInvalidDatesAndTimes() {
        int[] destinations = new int[]{1, 2};
        String[] arrivalTimes = new String[]{"2018-05-10 14:10:00", "2018-05-13 13:25:00"};
        String[] departureTimes = new String[]{"2018-05-13 13:26:00", "2018-05-27 23:15:00"};

        Trip trip = createTestTripObject(false, destinations, arrivalTimes, departureTimes);
        trip.id = 1L;    // Needs to be set to trip created in evolutions
        JsonNode node = Json.toJson(trip);

        // Update trip object inserted in evolutions script
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(PUT)
            .bodyJson(node)
            .cookie(adminAuthCookie)
            .uri("/api/trip");

        Result result = route(fakeApp, request);
        assertEquals(BAD_REQUEST, result.status());
    }

    @Test
    public void updateTripOrder() throws IOException {
        // Create a new trip with details
        int[] destinations = new int[]{1, 2};
        String[] arrivalTimes = new String[]{null, null};
        String[] departureTimes = new String[]{null, null};

        Trip trip = createTestTripObject(false, destinations, arrivalTimes, departureTimes);
        JsonNode node = Json.toJson(trip);

        // Create request to insert trip
        Http.RequestBuilder insertRequest = Helpers.fakeRequest()
            .method(POST)
            .bodyJson(node)
            .cookie(adminAuthCookie)
            .uri("/api/trip");

        // Get result, check it was successful and retrieve trip ID
        Result insertResult = route(fakeApp, insertRequest);
        assertEquals(OK, insertResult.status());

        Long tripId = new ObjectMapper()
            .readValue(Helpers.contentAsString(insertResult), Long.class);

        // Create new trip with modified destination order, set tripId to created trip ID
        destinations = new int[]{2, 1};

        Trip tripToUpdate = createTestTripObject(false, destinations, arrivalTimes, departureTimes);
        tripToUpdate.id = tripId;    // Needs to be set to trip created in evolutions
        node = Json.toJson(tripToUpdate);

        // Update trip object inserted in evolutions script
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(PUT)
            .bodyJson(node)
            .cookie(adminAuthCookie)
            .uri("/api/trip");

        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        // Get trip and check data
        Http.RequestBuilder getRequest = Helpers.fakeRequest()
            .method(GET)
            .bodyJson(node)
            .cookie(adminAuthCookie)
            .uri("/api/trip/" + tripId);

        Result getResult = route(fakeApp, getRequest);
        assertEquals(OK, result.status());

        JsonNode json = new ObjectMapper()
            .readValue(Helpers.contentAsString(getResult), JsonNode.class);
        Trip retrieved = new ObjectMapper()
            .readValue(new ObjectMapper().treeAsTokens(json), new TypeReference<Trip>() {
            });

        for (int i = 0; i < retrieved.tripDataList.size(); i++) {
            TripData tripData = tripToUpdate.tripDataList.get(i);

            assertEquals(Long.valueOf(destinations[i]), tripData.destination.id);
            assertNull(tripData.arrivalTime);
            assertNull(tripData.departureTime);
        }
    }

    @Test
    public void updateTripInvalidOrder() throws IOException {
        // Create a new trip with details
        int[] destinations = new int[]{1, 2, 1, 3};
        String[] arrivalTimes = new String[]{};
        String[] departureTimes = new String[]{};

        Trip trip = createTestTripObject(false, destinations, arrivalTimes, departureTimes);
        JsonNode node = Json.toJson(trip);

        // Create request to insert trip
        Http.RequestBuilder insertRequest = Helpers.fakeRequest()
            .method(POST)
            .bodyJson(node)
            .cookie(adminAuthCookie)
            .uri("/api/trip");

        // Get result, check it was successful and retrieve trip ID
        Result insertResult = route(fakeApp, insertRequest);
        assertEquals(OK, insertResult.status());

        Long tripId = new ObjectMapper()
            .readValue(Helpers.contentAsString(insertResult), Long.class);

        // Create new trip with modified destination order, set tripId to created trip ID
        destinations = new int[]{2, 1, 1, 3};

        Trip tripToUpdate = createTestTripObject(false, destinations, arrivalTimes, departureTimes);
        tripToUpdate.id = tripId;    // Needs to be set to trip created in evolutions
        node = Json.toJson(tripToUpdate);

        // Update trip object inserted in evolutions script
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(PUT)
            .bodyJson(node)
            .cookie(adminAuthCookie)
            .uri("/api/trip");

        Result result = route(fakeApp, request);
        assertEquals(BAD_REQUEST, result.status());
    }

    @Test
    public void updateTripInvalidId() {
        // Create trip object
        int[] destinations = new int[]{1, 2};
        String[] arrivalTimes = new String[]{};
        String[] departureTimes = new String[]{};

        Trip trip = createTestTripObject(false, destinations, arrivalTimes, departureTimes);
        trip.id = 100L;    // Set ID to value which doesn't exist
        JsonNode node = Json.toJson(trip);

        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(PUT)
            .bodyJson(node)
            .cookie(adminAuthCookie)
            .uri("/api/trip");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(NOT_FOUND, result.status());
    }

    @Test
    public void transferDestinationOwnershipByTrip() throws IOException {
        // Check destination 4 is public and owned by user 2
        Http.RequestBuilder initGetRequest = Helpers.fakeRequest()
            .method(GET)
            .uri("/api/destination/4");

        // Get result and check it was successful
        Result initGetResult = route(fakeApp, initGetRequest);
        assertEquals(OK, initGetResult.status());

        // Check destination privacy and owner
        JsonNode json = new ObjectMapper()
            .readValue(Helpers.contentAsString(initGetResult), JsonNode.class);
        Destination getDest = new ObjectMapper()
            .readValue(new ObjectMapper().treeAsTokens(json), new TypeReference<Destination>() {
            });
        assertEquals(Long.valueOf(2), getDest.user.id);
        assertTrue(getDest.isPublic);

        // Create new trip by user 3 using destination 4
        int[] destinations = new int[]{1, 2, 4};
        String[] arrivalTimes = new String[]{};
        String[] departureTimes = new String[]{};

        Trip trip = createTestTripObject(false, destinations, arrivalTimes, departureTimes);
        trip.userId = 3L;
        JsonNode node = Json.toJson(trip);

        // Create request to insert trip
        Http.RequestBuilder insertRequest = Helpers.fakeRequest()
            .method(POST)
            .bodyJson(node)
            .cookie(adminAuthCookie)
            .uri("/api/trip");

        // Get result and check it was successful
        Result insertResult = route(fakeApp, insertRequest);
        assertEquals(OK, insertResult.status());

        // Check destination 4 is now owned by master admin and is public
        Http.RequestBuilder finalGetRequest = Helpers.fakeRequest()
            .method(GET)
            .uri("/api/destination/4");

        // Get result and check it was successful
        Result finalGetResult = route(fakeApp, finalGetRequest);
        assertEquals(OK, finalGetResult.status());

        // Check destination privacy and owner
        JsonNode json2 = new ObjectMapper()
            .readValue(Helpers.contentAsString(finalGetResult), JsonNode.class);
        Destination getDest2 = new ObjectMapper()
            .readValue(new ObjectMapper().treeAsTokens(json2), new TypeReference<Destination>() {
            });
        assertEquals(Long.valueOf(1), getDest2.user.id);
        assertTrue(getDest2.isPublic);
    }

    @Test
    public void getTrip() {
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(adminAuthCookie)
            .uri("/api/trip/1");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }

    @Test
    public void getTripInvalidID() {
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(adminAuthCookie)
            .uri("/api/trip/100");

        // Get result and check it was not successful
        Result result = route(fakeApp, request);
        assertEquals(NOT_FOUND, result.status());
    }

    @Test
    public void getAllUserTrips() throws IOException {
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(adminAuthCookie)
            .uri("/api/user/trips/1");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        JsonNode trips = new ObjectMapper()
            .readValue(Helpers.contentAsString(result), JsonNode.class);
        assertEquals(1, trips.size());    // Because 1 trip inserted in evolutions
    }

    @Test
    public void getAllUserTripsInvalidUserID() throws IOException {
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(adminAuthCookie)
            .uri("/api/user/trips/100");

        // Get result and check no trips were returned
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        JsonNode trips = new ObjectMapper()
            .readValue(Helpers.contentAsString(result), JsonNode.class);
        assertEquals(0, trips.size());

    }

    @Test
    public void getAllUserTripsHasNoTrips() throws IOException {
        // Deletes trip added in evolutions
        Http.RequestBuilder deleteRequest = Helpers.fakeRequest()
            .method(PUT)
            .cookie(adminAuthCookie)
            .uri("/api/trip/1/delete");

        Result deleteResult = route(fakeApp, deleteRequest);
        assertEquals(OK, deleteResult.status());

        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(adminAuthCookie)
            .uri("/api/user/trips/1");

        // Get result and check no trips were returned
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        JsonNode trips = new ObjectMapper()
            .readValue(Helpers.contentAsString(result), JsonNode.class);
        assertEquals(0, trips.size());
    }

    @Test
    public void sortTripsByDateIsNewestToOldest() throws IOException {
        List<Trip> trips = new ArrayList<>();

        int[] destinations = new int[]{1, 2};
        String[] arrivalTimes = new String[]{"2019-03-25 00:00:00"};
        String[] departureTimes = new String[]{};

        Trip trip = createTestTripObject(false, destinations, arrivalTimes, departureTimes);
        trips.add(trip);

        destinations = new int[]{1, 2};
        arrivalTimes = new String[]{"2019-04-01 00:00:00"};
        departureTimes = new String[]{};

        trip = createTestTripObject(false, destinations, arrivalTimes, departureTimes);
        trips.add(trip);

        destinations = new int[]{1, 2};
        arrivalTimes = new String[]{"2019-03-29 00:00:00", "2019-10-10 00:00:00"};
        departureTimes = new String[]{};

        trip = createTestTripObject(false, destinations, arrivalTimes, departureTimes);
        trips.add(trip);

        // Insert trips into database
        assertTrue(insertTrips(trips));

        // Get trip from database and check success
        Http.RequestBuilder getRequest = Helpers.fakeRequest()
            .method(GET)
            .cookie(adminAuthCookie)
            .uri("/api/user/trips/1");

        Result getResult = route(fakeApp, getRequest);
        assertEquals(OK, getResult.status());

        JsonNode tripsJson = new ObjectMapper()
            .readValue(Helpers.contentAsString(getResult), JsonNode.class);
        trips = new ObjectMapper()
            .readValue(new ObjectMapper().treeAsTokens(tripsJson), new TypeReference<List<Trip>>() {
            });
        assertFalse(trips.isEmpty());

        for (int i = 0; i < trips.size() - 1; i++) {
            // If both not null, check index i date is after index i+1 date
            if (trips.get(i).findFirstTripDateAsDate() != null
                && trips.get(i + 1).findFirstTripDateAsDate() != null) {
                assertTrue(trips.get(i).findFirstTripDateAsDate()
                    .compareTo(trips.get(i).findFirstTripDateAsDate()) <= 0);
            }
            // Else ensure index i+1 date is null, if this is not null then index i will be null and it will not be ordered correctly
            else {
                assertNull(trips.get(i + 1).findFirstTripDateAsDate());
            }
        }
    }

    @Test
    public void sortTripsByDateIsNullLast() throws IOException {
        List<Trip> trips = new ArrayList<>();

        int[] destinations = new int[]{1, 2};
        String[] arrivalTimes = new String[]{};
        String[] departureTimes = new String[]{};

        Trip trip = createTestTripObject(false, destinations, arrivalTimes, departureTimes);
        trips.add(trip);

        destinations = new int[]{1, 2};
        arrivalTimes = new String[]{"2019-04-01 00:00:00"};
        departureTimes = new String[]{};

        trip = createTestTripObject(false, destinations, arrivalTimes, departureTimes);
        trips.add(trip);

        destinations = new int[]{1, 2};
        arrivalTimes = new String[]{};
        departureTimes = new String[]{};

        trip = createTestTripObject(false, destinations, arrivalTimes, departureTimes);
        trips.add(trip);

        // Insert trips into database
        assertTrue(insertTrips(trips));

        // Get trip from database and check success
        Http.RequestBuilder getRequest = Helpers.fakeRequest()
            .method(GET)
            .cookie(adminAuthCookie)
            .uri("/api/user/trips/1");

        Result getResult = route(fakeApp, getRequest);
        assertEquals(OK, getResult.status());

        JsonNode tripsJson = new ObjectMapper()
            .readValue(Helpers.contentAsString(getResult), JsonNode.class);
        trips = new ObjectMapper()
            .readValue(new ObjectMapper().treeAsTokens(tripsJson), new TypeReference<List<Trip>>() {
            });
        assertFalse(trips.isEmpty());

        for (int i = 0; i < trips.size() - 1; i++) {
            // If both not null, check index i date is after index i+1 date
            if (trips.get(i).findFirstTripDateAsDate() != null
                && trips.get(i + 1).findFirstTripDateAsDate() != null) {
                assertTrue(trips.get(i).findFirstTripDateAsDate()
                    .compareTo(trips.get(i).findFirstTripDateAsDate()) <= 0);
            }
            // Else ensure index i+1 date is null, if this is not null then index i will be null and it will not be ordered correctly
            else {
                assertNull(trips.get(i + 1).findFirstTripDateAsDate());
            }
        }
    }

    @Test
    public void deleteTrip() {
        // Create request to delete trip
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(PUT)
            .cookie(adminAuthCookie)
            .uri("/api/trip/1/delete");

        // Send request and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        // Create get request to check trip no longer exists
        Http.RequestBuilder checkDeletedRequest = Helpers.fakeRequest()
            .method(GET)
            .cookie(adminAuthCookie)
            .uri("/api/trip/1");

        // Send request and check trip wasn't found
        Result checkDeletedResult = route(fakeApp, checkDeletedRequest);
        assertEquals(NOT_FOUND, checkDeletedResult.status());
    }

    @Test
    public void deleteTripInvalidId() {
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(DELETE)
            .cookie(adminAuthCookie)
            .uri("/api/trip/10");

        // Get result and check it was not successful
        Result result = route(fakeApp, request);
        assertEquals(NOT_FOUND, result.status());
    }

    private boolean insertTrips(List<Trip> trips) {
        boolean statusesOk = true;

        for (Trip tripToInsert : trips) {
            JsonNode node = Json.toJson(tripToInsert);

            Http.RequestBuilder insertRequest = Helpers.fakeRequest()
                .method(POST)
                .bodyJson(node)
                .cookie(adminAuthCookie)
                .uri("/api/trip");

            Result insertResult = route(fakeApp, insertRequest);
            statusesOk = (insertResult.status() == OK);
        }

        return statusesOk;
    }
}
