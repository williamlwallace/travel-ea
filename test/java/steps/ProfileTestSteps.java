package steps;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.GET;
import static play.test.Helpers.POST;
import static play.test.Helpers.route;
import static steps.GenericTestSteps.appendUri;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import models.Profile;
import org.junit.Assert;
import play.Application;
import play.db.Database;
import play.db.evolutions.Evolutions;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Http.Cookie;
import play.mvc.Result;
import play.test.Helpers;
import util.objects.PagingResponse;

public class ProfileTestSteps {

    URI searchURI;

    PagingResponse<Profile> response;

    protected static Application fakeApp;
    static Database db;
    public static Http.Cookie adminAuthCookie;
    public static Http.Cookie nonAdminAuthCookie;

    static Long userId = 1L;

    /**
     * Configures system to use trip database, and starts a fake app
     */
    @Before
    public static void setUp() {
        // Create custom settings that change the database to use test database instead of production
        Map<String, String> settings = new HashMap<>();
        settings.put("db.default.driver", "org.h2.Driver");
        settings.put("db.default.url", "jdbc:h2:mem:testdb;MODE=MySQL;");

        adminAuthCookie = Cookie.builder("JWT-Auth",
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJUcmF2ZWxFQSIsInVzZXJJZCI6MX0.85pxdAoiT8xkO-39PUD_XNit5R8jmavTFfPSOVcPFWw")
            .withPath("/").build();
        nonAdminAuthCookie = Cookie.builder("JWT-Auth",
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJUcmF2ZWxFQSIsInVzZXJJZCI6Mn0.sGyO22MrNoNrH928NpSK8PJXmE88_DhivVWgCl3faJ4")
            .withPath("/").build();

        // Create a fake app that we can query just like we would if it was running
        fakeApp = Helpers.fakeApplication(settings);
        db = fakeApp.injector().instanceOf(Database.class);

        Helpers.start(fakeApp);
    }

    /**
     * Stop the fake app
     */
    private static void stopApp() {
        // Stop the fake app running
        Helpers.stop(fakeApp);
    }

    /**
     * Cleans up trips after each test, to allow for them to be re-run for next test
     */
    @After
    public void cleanupEvolutions() {
        Evolutions.cleanupEvolutions(db);
        stopApp();
    }

    @Given("I am logged in and some profiles exist")
    public void i_am_logged_in_and_some_profiles_exist() throws URISyntaxException {
        Evolutions.applyEvolutions(db,
            Evolutions.fromClassLoader(getClass().getClassLoader(), "test/profilesPagination/"));

        searchURI = new URI("/api/profile/search");

        // Create new user, so password is hashed
        ObjectNode node = Json.newObject();
        node.put("username", "dave@gmail.com");
        node.put("password", "cats");

        // Create request to login
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(POST)
            .bodyJson(node)
            .uri("/api/login");

        // Get result and check OK was sent back
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }

    @When("I search profiles")
    public void i_search_profiles() throws IOException {
        // Write code here that turns the phrase above into concrete actions
        System.out.println(searchURI);
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(adminAuthCookie)
            .uri(searchURI);

        Result result = route(fakeApp, request);
        ObjectMapper mapper = new ObjectMapper();

        response =  mapper.convertValue(mapper.readTree(Helpers.contentAsString(result)),
            new TypeReference<PagingResponse<Profile>>(){});
    }

    @Given("Get page number {int}")
    public void get_page_number(Integer int1) throws URISyntaxException {
        searchURI = appendUri(searchURI.toASCIIString(), "pageNum=" + int1);
    }

    @Given("Use page size {int}")
    public void use_page_size(Integer int1) throws URISyntaxException {
        searchURI = appendUri(searchURI.toASCIIString(), "pageSize=" + int1);
    }

    @Given("Get nationalities with ids {string}")
    public void get_nationalities_with_ids(String string) throws URISyntaxException {
        for(String part : string.split(",")) {
            searchURI = appendUri(searchURI.toASCIIString(), "nationalityIds=" + part);
        }
    }

    @Given("Get traveller types with ids {string}")
    public void get_traveller_types_with_ids(String string) throws URISyntaxException {
        for(String part : string.split(",")) {
            searchURI = appendUri(searchURI.toASCIIString(), "travellerTypeIds=" + part);
        }
    }

    @Given("Get genders {string}")
    public void get_genders(String string) throws URISyntaxException {
        for(String part : string.split(",")) {
            searchURI = appendUri(searchURI.toASCIIString(), "genders=" + part);
        }
    }

    @Given("Set minimum age to {int} years old")
    public void set_minimum_age_to_years_old(Integer int1) throws URISyntaxException {
        searchURI = appendUri(searchURI.toASCIIString(), "minAge=" + int1);
    }

    @When("Set maximum age to {int} years old")
    public void set_maximum_age_to_years_old(Integer int1) throws URISyntaxException {
        searchURI = appendUri(searchURI.toASCIIString(), "maxAge=" + int1);
    }

    @Given("Use the search query {string}")
    public void use_the_search_query(String string) throws URISyntaxException {
        searchURI = appendUri(searchURI.toASCIIString(), "searchQuery=" + string);
    }

    @Given("Sort by {string}")
    public void sort_by(String string) throws URISyntaxException {
        searchURI = appendUri(searchURI.toASCIIString(), "sortBy=" + string);
    }

    @Given("Set ascending order of results to {string}")
    public void set_ascending_order_of_results_to(String string) throws URISyntaxException {
        searchURI = appendUri(searchURI.toASCIIString(), "ascending=" + string);
    }

    @Then("I get {int} results")
    public void i_get_results(Integer int1) {
        Assert.assertEquals((int)int1, response.data.size());
    }

    @Then("There are {int} total pages")
    public void there_are_total_pages(Integer int1) {
        Assert.assertEquals(int1, response.totalNumberPages);
    }

    @Then("The first profile has first name {string}, last name {string}")
    public void the_first_profile_has_first_name_last_name(String string, String string2) {
        System.out.println(Json.toJson(response.data.get(0)));
        Assert.assertEquals(string, response.data.get(0).firstName);
        Assert.assertEquals(string2, response.data.get(0).lastName);
    }

}
