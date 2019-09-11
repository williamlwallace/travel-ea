package controllers.backend;

import actions.ActionState;
import actions.Authenticator;
import actions.roles.Everyone;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.inject.Inject;
import models.NewsFeedEvent;
import models.NewsFeedResponseItem;
import models.Photo;
import models.User;
import models.enums.NewsFeedEventType;
import models.strategies.NewsFeedStrategy;
import models.strategies.photos.destination.concrete.LinkDestinationPhotoStrategy;
import models.strategies.photos.destination.concrete.NewPrimaryDestinationPhotoStrategy;
import models.strategies.photos.user.concrete.NewCoverPhotoStrategy;
import models.strategies.photos.user.concrete.NewProfilePhotoStrategy;
import models.strategies.photos.user.concrete.UploadedUserPhotoStrategy;
import models.strategies.trips.concrete.CreateTripStrategy;
import org.apache.commons.lang3.NotImplementedException;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import play.routing.JavaScriptReverseRouter;
import repository.DestinationRepository;
import repository.NewsFeedEventRepository;
import repository.PhotoRepository;
import repository.ProfileRepository;
import repository.TripRepository;
import util.objects.PagingResponse;

public class NewsFeedController extends TEABackController {

    // Repository for interacting with NewsFeedEvents table
    private NewsFeedEventRepository newsFeedEventRepository;

    // All other repositories required by strategies (they get injected here then passed down)
    DestinationRepository destinationRepository;
    ProfileRepository profileRepository;
    TripRepository tripRepository;
    PhotoRepository photoRepository;

    /**
     *  Constructor which handles injecting of all needed dependencies
     * @param newsFeedEventRepository Instance of NewsFeedRepository
     * @param destinationRepository Instance of DestinationRepository
     * @param profileRepository Instance of ProfileRepository
     * @param tripRepository Instance of TripRepository
     * @param photoRepository Instance of PhotoRepository
     */
    @Inject
    public NewsFeedController(NewsFeedEventRepository newsFeedEventRepository,
        DestinationRepository destinationRepository, ProfileRepository profileRepository,
        TripRepository tripRepository, PhotoRepository photoRepository) {
        this.newsFeedEventRepository = newsFeedEventRepository;
        this.destinationRepository = destinationRepository;
        this.profileRepository = profileRepository;
        this.tripRepository = tripRepository;
        this.photoRepository = photoRepository;
    }

    /**
     * Endpoint to fetch all profile news feed data
     *
     * @param request HTTP request containing user auth
     * @param pageNum Page number to retrieve
     * @param pageSize Number of results to give per page
     * @param requestOrder The order of the request we are showing
     * @return Paging response with all JsonNode values needed to create cards for each event
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> getProfileNewsFeed(Http.Request request, Long userId, Integer pageNum, Integer pageSize, Integer requestOrder) {
        return getNewsFeedData(Collections.singletonList(userId), null, pageNum, pageSize, requestOrder);
    }

    /**
     * Gets the news feed data
     * 
     * @param userIds list of userIds
     * @param destIds list of destIds
     * @param pageNum page number
     * @param pageSize page size
     */
    private CompletableFuture<Result> getNewsFeedData(List<Long> userIds, // Possibly null
                                                        List<Long> destIds,
                                                        Integer pageNum,
                                                        Integer pageSize,
                                                        Integer requestOrder) {
        // Perform repository call
        return newsFeedEventRepository.getPagedEvents(userIds, destIds, pageNum, pageSize)
        .thenComposeAsync(events -> {
            // Collect a list of the completable strategies created for each event
            List<CompletableFuture<NewsFeedResponseItem>> completableStrategies = events.getList().stream()
                .map(x -> getStrategyForEvent(x).execute())
                .collect(Collectors.toList());
                // Wait until all strategies have executed then return paging response
                return CompletableFuture.allOf(completableStrategies.toArray(new CompletableFuture[0]))
                .thenApplyAsync(v -> {
                    // Append the correct created time and event type to each complete event
                    List<NewsFeedResponseItem> completedStrategies = completableStrategies.stream().map(CompletableFuture::join).collect(Collectors.toList());
                    for(int i = 0; i < events.getList().size(); i++) {
                        completedStrategies.get(i).created = events.getList().get(i).created;
                        completedStrategies.get(i).eventType = events.getList().get(i).eventType;
                    }

                    // Serialize and return a paging response with all created NewsFeedResponseItems
                    return ok(Json.toJson(new PagingResponse<>(
                        completedStrategies,
                        requestOrder,
                        events.getTotalPageCount())));
                });
            }
        );
        
    }

    /**
     * When given some news feed event, the correct strategy will be time and returned
     * From there all that needs to happen is to call strategy.execute() and handle the response asynchronously
     * @param event Object representing the news feed event
     * @return Strategy applicable to specific event
     */
    private NewsFeedStrategy getStrategyForEvent(NewsFeedEvent event) {
        switch (NewsFeedEventType.valueOf(event.eventType)) {
            case NEW_PROFILE_PHOTO:
                return new NewProfilePhotoStrategy(event.refId, event.userId, photoRepository,profileRepository);

            case UPLOADED_USER_PHOTO:
                return new UploadedUserPhotoStrategy(event.refId, event.userId, photoRepository, profileRepository);

            case NEW_PROFILE_COVER_PHOTO:
                return new NewCoverPhotoStrategy(event.refId, event.userId, photoRepository, profileRepository);

            case LINK_DESTINATION_PHOTO:
                return new LinkDestinationPhotoStrategy(event.refId, event.destId, event.userId, photoRepository, destinationRepository, profileRepository);

            case NEW_PRIMARY_DESTINATION_PHOTO:
                return new NewPrimaryDestinationPhotoStrategy(event.refId, event.destId, photoRepository, destinationRepository);

            case CREATED_NEW_TRIP:
                return new CreateTripStrategy(event.refId, event.userId, profileRepository, tripRepository);

            default:
                throw new NotImplementedException("Event type to specified in strategy pattern selector.");
        }
    }


    /**
     * Lists routes to put in JS router for use from frontend.
     *
     * @return JSRouter Play result
     */
    public Result newsFeedRoutes(Http.Request request) {
        return ok(
            JavaScriptReverseRouter.create("newsFeedRouter", "jQuery.ajax", request.host(),
                controllers.backend.routes.javascript.NewsFeedController.getProfileNewsFeed()
            )
        ).as(Http.MimeTypes.JAVASCRIPT);
    }
}
