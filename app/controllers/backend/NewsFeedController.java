package controllers.backend;

import actions.Authenticator;
import actions.roles.Everyone;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.inject.Inject;
import models.BaseNewsFeedEvent;
import models.GroupedNewsFeedEvent;
import models.NewsFeedEvent;
import models.NewsFeedResponseItem;
import models.enums.NewsFeedEventType;
import models.strategies.NewsFeedStrategy;
import models.strategies.destinations.user.concrete.CreateDestinationStrategy;
import models.strategies.destinations.user.concrete.UpdateDestinationStrategy;
import models.strategies.photos.destination.concrete.GroupedLinkDestinationPhotoStrategy;
import models.strategies.photos.destination.concrete.NewPrimaryDestinationPhotoStrategy;
import models.strategies.photos.user.concrete.GroupedUserProfilePhotoStrategy;
import models.strategies.photos.user.concrete.NewCoverPhotoStrategy;
import models.strategies.photos.user.concrete.NewProfilePhotoStrategy;
import models.strategies.trips.concrete.CreateTripStrategy;
import models.strategies.trips.concrete.MultipleUpdateTripStrategy;
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
import util.objects.Pair;

public class NewsFeedController extends TEABackController {

    // Repository for interacting with NewsFeedEvents table
    private NewsFeedEventRepository newsFeedEventRepository;

    // All other repositories required by strategies (they get injected here then passed down)
    DestinationRepository destinationRepository;
    ProfileRepository profileRepository;
    TripRepository tripRepository;
    PhotoRepository photoRepository;

    private static final List<NewsFeedEventType> GROUP_EVENT_TYPES = Arrays.asList(
        NewsFeedEventType.UPLOADED_USER_PHOTO,
        NewsFeedEventType.UPDATED_EXISTING_TRIP,
        NewsFeedEventType.LINK_DESTINATION_PHOTO);

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
        .thenComposeAsync(pagedEvents -> {
            // Modify returned events list to only include singular events, and create a new list of grouped events
            Pair<List<NewsFeedEvent>, List<GroupedNewsFeedEvent>> pair = filterOutGroupedEvents(pagedEvents.getList());
            List<NewsFeedEvent> events = pair.getKey();
            List<GroupedNewsFeedEvent> groupedEvents = pair.getValue();

            // Collect a list of the completable strategies created for each event, singular and grouped
            List<CompletableFuture<NewsFeedResponseItem>> completableStrategies = events.stream()
                .map(x -> getStrategyForEvent(x).execute())
                .collect(Collectors.toList());

            completableStrategies.addAll(groupedEvents.stream()
                .map(x -> getStrategyForEvent(x).execute())
                .collect(Collectors.toList()));


            // Wait until all strategies have executed then return paging response
            return CompletableFuture.allOf(completableStrategies.toArray(new CompletableFuture[0]))
                .thenApplyAsync(v -> {
                    // Append the correct created time and event type to each complete event
                    List<NewsFeedResponseItem> completedStrategies = completableStrategies.stream().map(CompletableFuture::join).collect(Collectors.toList());
                    // Single events
                    for(int i = 0; i < events.size(); i++) {
                        completedStrategies.get(i).created = events.get(i).created;
                        completedStrategies.get(i).eventType = events.get(i).eventType;
                    }
                    // Grouped events
                    for(int i = events.size(); i < groupedEvents.size() + events.size(); i++){
                        completedStrategies.get(i).created = groupedEvents.get(i - events.size()).created;
                        completedStrategies.get(i).eventType = groupedEvents.get(i - events.size()).eventType;
                    }

                    // Sort all completed strategies by creation date (most recent first)
                    completedStrategies.sort(Collections.reverseOrder(Comparator.comparing(cs -> cs.created)));

                    // Serialize and return a paging response with all created NewsFeedResponseItems
                    return ok(Json.toJson(new PagingResponse<>(
                        completedStrategies,
                        requestOrder,
                        pagedEvents.getTotalPageCount())));
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
    private NewsFeedStrategy getStrategyForEvent(BaseNewsFeedEvent event) {
        // Singular events
        if(event instanceof NewsFeedEvent) {
            NewsFeedEvent singleEvent = (NewsFeedEvent)event;
            switch (NewsFeedEventType.valueOf(event.eventType)) {
                case NEW_PROFILE_PHOTO:
                    return new NewProfilePhotoStrategy(singleEvent.refId, singleEvent.userId, photoRepository,profileRepository, Arrays.asList(singleEvent.guid));

                case NEW_PROFILE_COVER_PHOTO:
                    return new NewCoverPhotoStrategy(singleEvent.refId, singleEvent.userId, photoRepository, profileRepository, Arrays.asList(singleEvent.guid));

                case NEW_PRIMARY_DESTINATION_PHOTO:
                    return new NewPrimaryDestinationPhotoStrategy(singleEvent.refId, singleEvent.destId, photoRepository, destinationRepository, Arrays.asList(singleEvent.guid));

                case CREATED_NEW_TRIP:
                    return new CreateTripStrategy(singleEvent.refId, singleEvent.userId, profileRepository, tripRepository, Arrays.asList(singleEvent.guid));

                case CREATED_NEW_DESTINATION:
                    return new CreateDestinationStrategy(singleEvent.refId, destinationRepository, singleEvent.userId, profileRepository, Arrays.asList(singleEvent.guid));

                case UPDATED_EXISTING_DESTINATION:
                    return new UpdateDestinationStrategy(singleEvent.refId, destinationRepository, singleEvent.userId, profileRepository, Arrays.asList(singleEvent.guid));

                default:
                    throw new NotImplementedException("Event type not specified in strategy pattern selector.");
            }
        }
        // Grouped events
        else {
            GroupedNewsFeedEvent groupedEvent = (GroupedNewsFeedEvent)event;
            switch (NewsFeedEventType.valueOf(groupedEvent.eventType)) {
                case MULTIPLE_GALLERY_PHOTOS:
                    return new GroupedUserProfilePhotoStrategy(groupedEvent.userId, photoRepository, profileRepository, groupedEvent.refIds, groupedEvent.eventIds);

                case GROUPED_TRIP_UPDATES:
                    return new MultipleUpdateTripStrategy(groupedEvent.tripId, groupedEvent.userId, profileRepository, tripRepository, groupedEvent.refIds, groupedEvent.eventIds);

                case MULTIPLE_DESTINATION_PHOTO_LINKS:
                    return new GroupedLinkDestinationPhotoStrategy(groupedEvent.destId, groupedEvent.userId, photoRepository,
                        destinationRepository, profileRepository, groupedEvent.refIds, groupedEvent.eventIds);

                default:
                    throw new NotImplementedException("Event type not specified in strategy pattern selector.");
            }
        }
    }

    /**
     * Sorts through a list, and where a group of like events are found, removes them from the eventList,
     * and adds them to a new list. This new list is made of groupedEvents, where the created values and
     * referenced ids are decided from the events they are made up of
     *
     * The creation date assigned to a grouped event is the latest of the creation dates of all grouped events.
     *
     * Only objects within a 12 hour range (from most recent) will be grouped
     *
     * @param eventList List of events to filter
     * @return Pair containing the singular events list and grouped events list
     */
    private Pair<List<NewsFeedEvent>, List<GroupedNewsFeedEvent>> filterOutGroupedEvents(List<NewsFeedEvent> eventList) {
        List<NewsFeedEvent> singularEventList = new ArrayList<>();
        List<GroupedNewsFeedEvent> groupedEventsList = new ArrayList<>();
        for (final NewsFeedEvent event : eventList) {
            final NewsFeedEventType eventType = NewsFeedEventType.valueOf(event.eventType);
            if (!GROUP_EVENT_TYPES.contains(eventType)) {
                singularEventList.add(event);
            } else {
                switch (NewsFeedEventType.valueOf(event.eventType)) {
                    case UPDATED_EXISTING_TRIP:
                        final Optional<GroupedNewsFeedEvent> matchedTripUpdates = groupedEventsList.stream()
                            .filter(x -> x.eventType.equals(NewsFeedEventType.GROUPED_TRIP_UPDATES.name()))
                            .filter(x -> x.created.minusHours(12).isBefore(event.created))
                            .filter(x -> x.tripId.equals(event.refId))
                            .findFirst();

                        if(matchedTripUpdates.isPresent()) {
                            int eventIndex = groupedEventsList.indexOf(matchedTripUpdates.get());
                            groupedEventsList.get(eventIndex).refIds.add(event.destId);
                            groupedEventsList.get(eventIndex).eventIds.add(event.destId);
                        } else {
                            GroupedNewsFeedEvent newGroupEvent = new GroupedNewsFeedEvent();
                            newGroupEvent.refIds = new ArrayList<>();
                            newGroupEvent.eventIds = new ArrayList<>();
                            newGroupEvent.refIds.add(event.destId);
                            newGroupEvent.eventIds.add(event.guid);
                            newGroupEvent.created = event.created;
                            newGroupEvent.tripId = event.refId;
                            newGroupEvent.userId = event.userId;
                            newGroupEvent.eventType = NewsFeedEventType.GROUPED_TRIP_UPDATES.name();
                            groupedEventsList.add(newGroupEvent);
                        }
                        break;

                    case UPLOADED_USER_PHOTO:
                        final Optional<GroupedNewsFeedEvent> matchedUserPhotos = groupedEventsList.stream()
                            .filter(x -> x.eventType.equals(NewsFeedEventType.MULTIPLE_GALLERY_PHOTOS.name()))
                            .filter(x -> x.created.minusHours(12).isBefore(event.created))
                            .filter(x -> x.userId.equals(event.userId))
                            .findFirst();

                        if(matchedUserPhotos.isPresent()) {
                            int eventIndex = groupedEventsList.indexOf(matchedUserPhotos.get());
                            groupedEventsList.get(eventIndex).refIds.add(event.destId);
                            groupedEventsList.get(eventIndex).eventIds.add(event.destId);
                        } else {
                            GroupedNewsFeedEvent newGroupEvent = new GroupedNewsFeedEvent();
                            newGroupEvent.refIds = new ArrayList<>();
                            newGroupEvent.eventIds = new ArrayList<>();
                            newGroupEvent.refIds.add(event.destId);
                            newGroupEvent.eventIds.add(event.guid);
                            newGroupEvent.created = event.created;
                            newGroupEvent.userId = event.userId;
                            newGroupEvent.eventType = NewsFeedEventType.MULTIPLE_GALLERY_PHOTOS.name();
                            groupedEventsList.add(newGroupEvent);
                        }
                        break;

                    case LINK_DESTINATION_PHOTO:
                        final Optional<GroupedNewsFeedEvent> matchedDestinationPhotoLinks = groupedEventsList.stream()
                            .filter(x -> x.eventType.equals(NewsFeedEventType.MULTIPLE_DESTINATION_PHOTO_LINKS.name()))
                            .filter(x -> x.created.minusHours(12).isBefore(event.created))
                            .filter(x -> x.userId.equals(event.userId))
                            .filter(x -> x.destId.equals(event.destId))
                            .findFirst();

                        if(matchedDestinationPhotoLinks.isPresent()) {
                            int eventIndex = groupedEventsList.indexOf(matchedDestinationPhotoLinks.get());
                            groupedEventsList.get(eventIndex).refIds.add(event.destId);
                            groupedEventsList.get(eventIndex).eventIds.add(event.destId);
                        } else {
                            GroupedNewsFeedEvent newGroupEvent = new GroupedNewsFeedEvent();
                            newGroupEvent.refIds = new ArrayList<>();
                            newGroupEvent.eventIds = new ArrayList<>();
                            newGroupEvent.refIds.add(event.destId);
                            newGroupEvent.eventIds.add(event.guid);
                            newGroupEvent.created = event.created;
                            newGroupEvent.destId = event.destId;
                            newGroupEvent.userId = event.userId;
                            newGroupEvent.eventType = NewsFeedEventType.MULTIPLE_DESTINATION_PHOTO_LINKS.name();
                            groupedEventsList.add(newGroupEvent);
                        }
                        break;
                }
            }
        }

        return new Pair<>(singularEventList, groupedEventsList);
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
