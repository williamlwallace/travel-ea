package controllers.backend;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.CREATED;
import static play.mvc.Http.Status.NOT_FOUND;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.GET;
import static play.test.Helpers.POST;
import static play.test.Helpers.route;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import models.CountryDefinition;
import org.junit.Before;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;

public class CountryControllerTest extends controllers.backend.ControllersTest {

    private static final String COUNTRY_URL = "/api/country";
    private static final String ALL_COUNTRIES_URL = COUNTRY_URL + "/getCountries";
    private static final String COUNTRY_BY_ID_URL = COUNTRY_URL + "/getCountry/";

    /**
     * Runs trips before each test These trips are found in conf/test/(whatever), and should contain
     * minimal sql data needed for tests
     */
    @Before
    public void runEvolutions() {
        applyEvolutions("test/country/");
    }

    @Test
    public void getAllCountries() throws IOException {
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(nonAdminAuthCookie)
            .uri(ALL_COUNTRIES_URL);

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        List<CountryDefinition> countries = Arrays.asList(
            new ObjectMapper()
                .readValue(Helpers.contentAsString(result), CountryDefinition[].class));

        assertEquals(4, countries.size());

        CountryDefinition mongolia = countries.get(2);
        assertEquals(Long.valueOf(496), mongolia.id);
        assertEquals("Mongolia", mongolia.name);
    }

    @Test
    public void getCountryById() throws IOException {
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(nonAdminAuthCookie)
            .uri(COUNTRY_BY_ID_URL + "246");

        // Get result and check it was successful
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        CountryDefinition country =
            new ObjectMapper().readValue(Helpers.contentAsString(result), CountryDefinition.class);

        assertEquals(Long.valueOf(246), country.id);
        assertEquals("Finland", country.name);
    }

    @Test
    public void getCountryByIdFail() {
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(nonAdminAuthCookie)
            .uri(COUNTRY_BY_ID_URL + "1000");

        Result result = route(fakeApp, request);
        assertEquals(NOT_FOUND, result.status());
    }

    @Test
    public void addCountrySuccess() {
        CountryDefinition country = new CountryDefinition();
        country.id = 250L;
        country.name = "France";

        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(POST)
            .bodyJson(Json.toJson(country))
            .cookie(nonAdminAuthCookie)
            .uri(COUNTRY_URL);

        Result result = route(fakeApp, request);
        assertEquals(CREATED, result.status());
    }

    @Test
    public void addCountryMissingFields() {
        CountryDefinition country = new CountryDefinition();
        country.name = "France";

        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(POST)
            .bodyJson(Json.toJson(country))
            .cookie(nonAdminAuthCookie)
            .uri(COUNTRY_URL);

        Result result = route(fakeApp, request);
        assertEquals(BAD_REQUEST, result.status());
    }

    @Test
    public void addCountryNoFields() {
        CountryDefinition country = new CountryDefinition();

        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(POST)
            .bodyJson(Json.toJson(country))
            .cookie(nonAdminAuthCookie)
            .uri(COUNTRY_URL);

        Result result = route(fakeApp, request);
        assertEquals(BAD_REQUEST, result.status());
    }

    @Test
    public void addCountryAlreadyExists() {
        CountryDefinition country = new CountryDefinition();
        country.id = 643L;
        country.name = "Russian Federation";

        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(POST)
            .bodyJson(Json.toJson(country))
            .cookie(nonAdminAuthCookie)
            .uri(COUNTRY_URL);

        Result result = route(fakeApp, request);
        assertEquals(BAD_REQUEST, result.status());
    }


}
