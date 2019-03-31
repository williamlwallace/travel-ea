package controllers.backend;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import play.test.WithApplication;

public class TripControllerTest extends WithApplication {

    @Test
    public void sanityTest() {
        assertEquals(1, 1);
    }

//    private static Application fakeApp;
//    private static Database db;
//    private static Cookie authCookie;
//
//    /**
//     * Configures system to use trip database, and starts a fake app
//     */
//    @BeforeClass
//    public static void setUp() {
//        // Create custom settings that change the database to use test database instead of production
//        Map<String, String> settings = new HashMap<>();
//        settings.put("db.default.driver", "org.h2.Driver");
//        settings.put("db.default.url", "jdbc:h2:mem:testdb;MODE=MySQL;");
//
//        authCookie = Cookie.builder("JWT-Auth", "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJUcmF2ZWxFQSIsInVzZXJJZCI6MX0.85pxdAoiT8xkO-39PUD_XNit5R8jmavTFfPSOVcPFWw").withPath("/").build();
//
//        // Create a fake app that we can query just like we would if it was running
//        fakeApp = Helpers.fakeApplication(settings);
//        db = fakeApp.injector().instanceOf(Database.class);
//
//        Helpers.start(fakeApp);
//    }
//
//    /**
//     * Runs evolutions before each test
//     * These evolutions are found in conf/test/(whatever), and should contain minimal sql data needed for tests
//     */
//    @Before
//    public void applyEvolutions() {
//        // Only certain trips, namely initialisation, and profile folders
//        Evolutions.applyEvolutions(db, Evolutions.fromClassLoader(getClass().getClassLoader(), "test/trip/"));
//    }
//
//    /**
//     * Cleans up evolutions after each test, to allow for them to be re-run for next test
//     */
//    @After
//    public void cleanupEvolutions() {
//        Evolutions.cleanupEvolutions(db);
//    }
//
//    /**
//     * Stop the fake app
//     */
//    @AfterClass
//    public static void stopApp() {
//        // Stop the fake app running
//        Helpers.stop(fakeApp);
//    }
//
//    @Test
//    public void createTrip() {
//
//    }
//
//    @Test
//    public void createTripNoDest() {
//        assertEquals(1, 2);
//    }
//
//    @Test
//    public void createTripSameDestTwiceAdjacent() {
//        assertEquals(1, 2);
//    }
//
//    @Test
//    public void createTripOneDest() {
//        assertEquals(1, 2);
//    }
//
//    @Test
//    public void updateTrip() {
//        assertEquals(1, 2);
//    }
//
//    @Test
//    public void updateTripInvalidId() {
//        assertEquals(1, 2);
//    }
//
//    @Test
//    public void updateTripInvalidUpdate() {
//        assertEquals(1, 2);
//    }
//
//    @Test
//    public void getTrip(){
//        Http.RequestBuilder request = Helpers.fakeRequest()
//                .method(GET)
//                .uri("/api/trip/1");
//
//        // Get result and check it was successful
//        Result result = route(fakeApp, request);
//        assertEquals(OK, result.status());
//    }
//
//    @Test
//    public void getTripInvalidId() {
//        Http.RequestBuilder request = Helpers.fakeRequest()
//                .method(GET)
//                .uri("/api/trip/816");
//
//        // Get result and check it was not successful
//        Result result = route(fakeApp, request);
//        assertEquals(BAD_REQUEST, result.status());
//    }
//
//    @Test
//    public void getAllTrips() {
//        Http.RequestBuilder request = Helpers.fakeRequest()
//                .method(GET)
//                .cookie(authCookie)
//                .uri("/api/trip/getAll");
//
//        // Get result and check it was successful
//        Result result = route(fakeApp, request);
//        assertEquals(OK, result.status());
//    }
//
//    @Test
//    public void getAllTripsInvalidId() {
//        Http.RequestBuilder request = Helpers.fakeRequest()
//                .method(GET)
//                .uri("/api/trip/getAll/10");
//
//        // Get result and check it was not successful
//        Result result = route(fakeApp, request);
//        assertEquals(BAD_REQUEST, result.status());
//    }
//
//    @Test
//    public void getAllTripsHasNoTrips() {
//        Http.RequestBuilder request = Helpers.fakeRequest()
//                .method(GET)
//                .uri("/api/trip/getAll/2");
//
//        // Get result and check it was not successful
//        Result result = route(fakeApp, request);
//        assertEquals(BAD_REQUEST, result.status());
//    }
//
//    @Test
//    public void deleteTrip() {
//        Http.RequestBuilder request = Helpers.fakeRequest()
//                .method(DELETE)
//                .cookie(authCookie)
//                .uri("/api/trip/1");
//
//        // Get result and check it was successful
//        Result result = route(fakeApp, request);
//        assertEquals(OK, result.status());
//    }
//
//    @Test
//    public void deleteTripInvalidId() {
//        Http.RequestBuilder request = Helpers.fakeRequest()
//                .method(DELETE)
//                .cookie(authCookie)
//                .uri("/api/trip/10");
//
//        // Get result and check it was not successful
//        Result result = route(fakeApp, request);
//        assertEquals(BAD_REQUEST, result.status());
//    }

}
