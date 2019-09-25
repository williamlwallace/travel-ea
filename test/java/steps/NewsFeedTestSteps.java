package steps;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.HttpVerbs.PUT;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.GET;
import static play.test.Helpers.route;
import static steps.GenericTestSteps.adminAuthCookie;
import static steps.GenericTestSteps.fakeApp;
import static steps.GenericTestSteps.nonAdminAuthCookie;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import java.io.IOException;
import models.NewsFeedResponseItem;
import org.junit.Assert;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import util.objects.PagingResponse;

public class NewsFeedTestSteps {

    private PagingResponse<NewsFeedResponseItem> profileNewsFeedResponse;
    private PagingResponse<NewsFeedResponseItem> destinationNewsFeedResponse;

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
        profileNewsFeedResponse = new ObjectMapper().readValue(Helpers.contentAsString(result), new TypeReference<PagingResponse<NewsFeedResponseItem>>(){});

    }

    @When("I get the news feed events for destination {int}")
    public void i_get_the_news_feed_events_for_destination(Integer int1) throws IOException {
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(GET)
            .cookie(nonAdminAuthCookie)
            .uri("/api/destination/" + int1 + "/newsfeed");

        // Check destination is public
        Result result = route(fakeApp, request);
        assertEquals(OK, result.status());

        // Deserialize result to list of destinations
        destinationNewsFeedResponse = new ObjectMapper().readValue(Helpers.contentAsString(result), new TypeReference<PagingResponse<NewsFeedResponseItem>>(){});

    }

    @Given("I make the photo with id {int} public")
    public void i_make_the_photo_with_id_public(Integer int1) {
        Http.RequestBuilder request = Helpers.fakeRequest()
            .method(PUT)
            .cookie(adminAuthCookie)
            .uri("/api/photo/" + int1 + "/privacy");

        Result result = route(fakeApp, request);
        Assert.assertEquals(OK, result.status());
    }

    @Then("There will be {int} news feed event for the profile")
    public void there_will_be_news_feed_event_for_the_profile(Integer int1) {
        assertEquals(int1, (Integer)profileNewsFeedResponse.data.size());
    }

    @Then("The first profile news feed event will have type {string}")
    public void the_first_news_feed_event_will_have_type(String string) {
        assertEquals(string, profileNewsFeedResponse.data.get(0).eventType);
    }

    @Then("The profile news feed event at index {int} will have type {string}")
    public void the_news_feed_event_at_index_will_have_type(Integer int1, String string) {
        assertEquals(string, profileNewsFeedResponse.data.get(int1).eventType);
    }

    @Then("There will be {int} news feed event for the destination")
    public void there_will_be_news_feed_event_for_the_destination(Integer int1) {
        assertEquals(int1, (Integer)destinationNewsFeedResponse.data.size());
    }

    @Then("The first destination news feed event will have type {string}")
    public void the_first_dest_news_feed_event_will_have_type(String string) {
        assertEquals(string, destinationNewsFeedResponse.data.get(0).eventType);
    }

    @Then("The destination news feed event at index {int} will have type {string}")
    public void the_dest_news_feed_event_at_index_will_have_type(Integer int1, String string) {
        assertEquals(string, destinationNewsFeedResponse.data.get(int1).eventType);
    }

}
