package controllers.backend;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Singleton;
import models.CountryDefinition;
import models.Destination;
import models.TreasureHunt;
import models.Trip;
import models.TripData;
import models.User;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import play.Application;
import play.db.Database;
import play.db.evolutions.Evolutions;
import play.mvc.Http.Cookie;
import play.test.Helpers;
import play.test.WithApplication;
import repository.DestinationRepository;

public abstract class ControllersTest extends WithApplication {

    static Application fakeApp;
    private static Database db;
    static Connection connection;
    // Belongs to admin, userID = 1
    static Cookie adminAuthCookie;
    // Belongs to non-admin, userID = 2
    static Cookie nonAdminAuthCookie;
    static DestinationRepository destinationRepository;

    /**
     * Configures system to use dest database, and starts a fake app
     */
    @BeforeClass
    public static void setUp() throws SQLException {
        // Create custom settings that change the database to use test database instead of production
        Map<String, String> settings = new HashMap<>();
        settings.put("db.default.driver", "org.h2.Driver");
        settings.put("db.default.url", "jdbc:h2:mem:testdb;MODE=MySQL;");

        // Create a fake app that we can query just like we would if it was running
        fakeApp = Helpers.fakeApplication(settings);
        db = fakeApp.injector().instanceOf(Database.class);
        if(connection != null) connection.close();
        connection = db.getConnection();
        adminAuthCookie = Cookie.builder("JWT-Auth",
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJUcmF2ZWxFQSIsInVzZXJJZCI6MX0.85pxdAoiT8xkO-39PUD_XNit5R8jmavTFfPSOVcPFWw")
            .withPath("/").build();
        nonAdminAuthCookie = Cookie.builder("JWT-Auth",
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJUcmF2ZWxFQSIsInVzZXJJZCI6Mn0.sGyO22MrNoNrH928NpSK8PJXmE88_DhivVWgCl3faJ4")
            .withPath("/").build();

        destinationRepository = fakeApp.injector().instanceOf(DestinationRepository.class);

        Helpers.start(fakeApp);
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
     * Runs evolutions before each test These evolutions are found in conf/test/(whatever), and
     * should contain minimal sql data needed for tests
     *
     * @param evolutionsRoute The route of the evolutions to apply
     */
    void applyEvolutions(String evolutionsRoute) {
        Evolutions.applyEvolutions(db,
            Evolutions.fromClassLoader(getClass().getClassLoader(), evolutionsRoute));
    }

    /**
     * Cleans up trips after each test, to allow for them to be re-run for next test
     */
    @After
    public void cleanupEvolutions() {
        Evolutions.cleanupEvolutions(db);
    }

    /**
     * Converts a result set from a query for rows from destination table into java collection
     *
     * Note: Does not join full objects where foreign keys are given, but rather just creates such an object with foreign key ID set
     *
     * @param rs Result set
     * @return Collection of destinations read from result set
     */
    Collection<Destination> destinationsFromResultSet(ResultSet rs) throws SQLException {
        Collection<Destination> destinations = new ArrayList<>();
        while (rs.next()) {
            Destination destination = new Destination();
            destination.id = rs.getLong("id");
            destination.destType = rs.getString("type");
            destination.country = new CountryDefinition();
            destination.country.id = rs.getLong("country_id");
            destination.district = rs.getString("district");
            destination.isPublic = rs.getBoolean("is_public");
            destination.latitude = rs.getDouble("latitude");
            destination.longitude = rs.getDouble("longitude");
            destination.user = new User();
            destination.user.id = rs.getLong("user_id");

            destinations.add(destination);
        }

        return destinations;
    }

    /**
     * Converts a result set from a query for rows from TripData table into java collection
     *
     * Note: Does not join full objects where foreign keys are given, but rather just creates such an object with foreign key ID set
     *
     * @param rs Result set
     * @return Collection of trip data read from result set
     */
    Collection<TripData> tripDataFromResultSet(ResultSet rs) throws SQLException {
        Collection<TripData> tripDataCollection = new ArrayList<>();
        while(rs.next()) {
            TripData tripData = new TripData();

            tripData.trip = new Trip();
            tripData.trip.id = rs.getLong("trip_id");
            tripData.guid = rs.getLong("guid");
            tripData.arrivalTime = (rs.getTimestamp("arrival_time") == null) ? null : rs.getTimestamp("arrival_time").toLocalDateTime();
            tripData.departureTime = (rs.getTimestamp("departure_time") == null) ? null : rs.getTimestamp("departure_time").toLocalDateTime();
            tripData.position = rs.getLong("position");
            tripData.destination = new Destination();
            tripData.destination.id = rs.getLong("destination_id");

            tripDataCollection.add(tripData);
        }
        return tripDataCollection;
    }

    /**
     * Converts a result set from a query for rows from TreasureHunt table into java collection
     *
     * Note: Does not join full objects where foreign keys are given, but rather just creates such an object with foreign key ID set
     *
     * @param rs Result set
     * @return Collection of treasure hunts read from result set
     */
    Collection<TreasureHunt> treasureHuntsFromResultSet(ResultSet rs) throws SQLException {
        Collection<TreasureHunt> treasureHuntCollection = new ArrayList<>();
        while(rs.next()) {
            TreasureHunt treasureHunt = new TreasureHunt();

            treasureHunt.destination = new Destination();
            treasureHunt.destination.id = rs.getLong("destination_id");
            treasureHunt.id = rs.getLong("id");
            treasureHunt.user = new User();
            treasureHunt.user.id = rs.getLong("user_id");
            treasureHunt.endDate = rs.getString("end_date");
            treasureHunt.startDate = rs.getString("start_date");
            treasureHunt.riddle = rs.getString("riddle");
            treasureHuntCollection.add(treasureHunt);
        }
        return treasureHuntCollection;
    }
}
