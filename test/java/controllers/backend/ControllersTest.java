package controllers.backend;

import java.util.HashMap;
import java.util.Map;
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
    static Database db;
    static Cookie adminAuthCookie;
    static Cookie nonAdminAuthCookie;
    static DestinationRepository destinationRepository;

    /**
     * Configures system to use dest database, and starts a fake app
     */
    @BeforeClass
    public static void setUp() {
        // Create custom settings that change the database to use test database instead of production
        Map<String, String> settings = new HashMap<>();
        settings.put("db.default.driver", "org.h2.Driver");
        settings.put("db.default.url", "jdbc:h2:mem:testdb;MODE=MySQL;");

        // Create a fake app that we can query just like we would if it was running
        fakeApp = Helpers.fakeApplication(settings);
        db = fakeApp.injector().instanceOf(Database.class);
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

}
