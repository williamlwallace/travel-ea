package controllers.backend;

import actions.ActionState;
import actions.Authenticator;
import actions.roles.Everyone;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import models.NewsFeedEvent;
import models.User;
import models.enums.NewsFeedEventType;
import models.strategies.NewsFeedStrategy;
import models.strategies.photos.destination.concrete.LinkDestinationPhotoStrategy;
import models.strategies.photos.destination.concrete.NewPrimaryDestinationPhotoStrategy;
import models.strategies.photos.user.concrete.NewProfilePhotoStrategy;
import models.strategies.photos.user.concrete.UploadedUserPhotoStrategy;
import org.apache.commons.lang3.NotImplementedException;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;

public class NewsFeedController extends TEABackController {

    /**
     * Endpoint to fetch all personal news feed items for the currently logged in user
     *
     * @param request HTTP request containing user auth
     * @param pageNum Page number to retrieve
     * @param pageSize Number of results to give per page
     * @param requestOrder The order of the request we are showing
     * @return Paging response with all JsonNode values needed to create cards for each event
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> getPersonalNewsFeed(Http.Request request, Integer pageNum, Integer pageSize, Integer requestOrder) {
        // Get the user who is currently logged in
        User loggedInUser = request.attrs().get(ActionState.USER);

        // Create list to store all strategies so that they can all be completed in parallel
        List<CompletableFuture<JsonNode>> completableStrategies = new ArrayList<>();
        ArrayList<NewsFeedEvent> events = new ArrayList<>();
        for(NewsFeedEvent event : events) { // TODO: Replace events with repo call
            completableStrategies.add(getStrategyForEvent(event).execute());
        }

        // Execute strategies in parallel and return response
        return CompletableFuture.supplyAsync(() ->
            ok(Json.toJson(CompletableFuture.allOf(completableStrategies.toArray(
                new CompletableFuture[0])).thenApply(v ->
                completableStrategies.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList()))
            )));
    }

    /**
     * When given some news feed event, the correct strategy will be time and returned
     * From there all that needs to happen is to call strategy.execute() and handle the response asynchronously
     * @param event Object representing the news feed event
     * @return Strategy applicable to specific event
     */
    private NewsFeedStrategy getStrategyForEvent(NewsFeedEvent event) {
        switch (NewsFeedEventType.valueOf(event.type)) {
            case NEW_PROFILE_PHOTO:
                return new NewProfilePhotoStrategy(event.refId, event.userId);

            case UPLOADED_USER_PHOTO:
                return new UploadedUserPhotoStrategy(event.refId, event.userId);

            case LINK_DESTINATION_PHOTO:
                return new LinkDestinationPhotoStrategy(event.refId, event.destId);

            case NEW_PRIMARY_DESTINATION_PHOTO:
                return new NewPrimaryDestinationPhotoStrategy(event.refId, event.destId);

            default:
                throw new NotImplementedException("Event type to specified in strategy pattern selector.");
        }
    }

}
