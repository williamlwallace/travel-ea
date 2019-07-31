package steps;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.GET;
import static play.test.Helpers.POST;
import static play.test.Helpers.PUT;
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
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import models.Destination;
import models.TreasureHunt;
import models.User;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;

public class TreasureHuntSteps extends WithApplication {

    @When("I create a treasure hunt")
    public void i_create_a_treasure_hunt() {

        TreasureHunt treasureHunt = new TreasureHunt();
        User user = new User();
        Destination destination = new Destination();

        destination.id = 1L;
        user.id = 2L;

        treasureHunt.user = user;
        treasureHunt.destination = destination;

        treasureHunt.startDate = LocalDate.of(2019, 07, 31);
        treasureHunt.endDate = LocalDate.of(2020, 07, 31);
        treasureHunt.riddle = "This is a riddle";

        JsonNode node =  Json.toJson(treasureHunt);

        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(POST)
            .bodyJson(node)
            .cookie(nonAdminAuthCookie)
            .uri("/api/treasurehunt");

        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

    }

    @Then("I can view my treasure hunt")
    public void i_can_view_my_treasure_hunt() throws IOException {
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(nonAdminAuthCookie)
            .uri("/api/treasurehunt");

        Result result  = route(fakeApp, request);
        assertEquals(OK, result.status());
        JsonNode treasureHunts = new ObjectMapper()
            .readValue(Helpers.contentAsString(result),  JsonNode.class);
        assertNotNull(treasureHunts.get(0));
        assertEquals("\"This is a riddle\"", treasureHunts.get(0).get("riddle").toString());
    }

    private List<TreasureHunt> getTreasureHunts() throws IOException  {
        Http.RequestBuilder getRequest =  Helpers.fakeRequest()
            .method(GET)
            .cookie(nonAdminAuthCookie)
            .uri("/api/treasurehunt");

        Result getResult = route(fakeApp, getRequest);

        if (getResult.status() != OK) {
            return null;
        } else {
            return Arrays.asList(
                new ObjectMapper().readValue(Helpers.contentAsString(getResult), TreasureHunt[].class));
        }
    }

    @Then("I can edit my treasure hunt")
    public void i_can_edit_my_treasure_hunt() throws IOException {
        List<TreasureHunt> treasureHunts = getTreasureHunts();
        assertNotNull(treasureHunts);

        treasureHunts.get(0).riddle = "A New Riddle";
        Long treasureHuntId = treasureHunts.get(0).id;

        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(PUT)
            .bodyJson(Json.toJson(treasureHunts.get(0)))
            .cookie(nonAdminAuthCookie)
            .uri("/api/treasurehunt/" + treasureHuntId);

        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());
    }

    @Then("the details are updated")
    public void the_details_are_updated() throws IOException {
        List<TreasureHunt> treasureHunts = getTreasureHunts();
        assertNotNull(treasureHunts);
        assertEquals("A New Riddle", treasureHunts.get(0).riddle);
    }

    @Given("A treasure hunt has been created by someone else")
    public void a_treasure_hunt_has_been_created_by_someone_else() {
        TreasureHunt treasureHunt = new TreasureHunt();
        User user = new User();
        Destination destination = new Destination();

        destination.id = 1L;
        user.id = 1L;

        treasureHunt.user = user;
        treasureHunt.destination = destination;

        treasureHunt.startDate = LocalDate.of(2019, 07, 31);
        treasureHunt.endDate = LocalDate.of(2020, 07, 31);
        treasureHunt.riddle = "An admins riddle";

        JsonNode node =  Json.toJson(treasureHunt);

        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(POST)
            .bodyJson(node)
            .cookie(adminAuthCookie)
            .uri("/api/treasurehunt");

        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

    }

    @Then("I can view a list of treasure hunts with both my hunt and the other hunt")
    public void i_can_view_a_list_of_treasure_hunts_with_both_my_hunt_and_the_other_hunt() throws IOException {
        List<TreasureHunt> treasureHunts = getTreasureHunts();
        assertNotNull(treasureHunts);

        assertEquals(2, treasureHunts.size());

    }

}
