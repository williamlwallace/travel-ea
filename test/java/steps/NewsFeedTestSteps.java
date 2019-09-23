package java.steps;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.GET;
import static play.test.Helpers.route;
import static steps.GenericTestSteps.fakeApp;
import static steps.GenericTestSteps.nonAdminAuthCookie;

import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import java.io.IOException;
import models.Destination;
import models.NewsFeedResponseItem;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import util.objects.PagingResponse;

public class NewsFeedTestSteps extends steps.GenericTestSteps {

    private PagingResponse<NewsFeedResponseItem> newsFeedPagingResponse;

    @When("I get the news feed events for profile {int}")
    public void i_get_the_news_feed_events_for_profile(Integer int1) throws IOException {
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(nonAdminAuthCookie)
            .uri("/api/user/" + int1 + "/newsfeed");

        // Check destination is public
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        // Deserialize result to list of destinations
        newsFeedPagingResponse = new ObjectMapper()
            .readValue(Helpers.contentAsString(result), PagingResponse.class);

    }

    @Then("There will be {int} news feed event")
    public void there_will_be_news_feed_event(Integer int1) {
        // Write code here that turns the phrase above into concrete actions
        throw new cucumber.api.PendingException();
    }

    @Then("The first news feed event will have type {string}")
    public void the_first_news_feed_event_will_have_type(String string) {
        // Write code here that turns the phrase above into concrete actions
        throw new cucumber.api.PendingException();
    }

}
