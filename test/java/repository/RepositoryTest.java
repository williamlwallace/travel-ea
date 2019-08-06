package repository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import play.Application;
import play.db.Database;
import play.db.evolutions.Evolutions;
import play.test.Helpers;
import play.test.WithApplication;


public abstract class RepositoryTest extends WithApplication {

    static Application fakeApp;
    static Connection connection;
    static DestinationRepository destinationRepository;
    private static Database db;

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
        if (connection != null) {
            connection.close();
        }
        connection = db.getConnection();

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
}

