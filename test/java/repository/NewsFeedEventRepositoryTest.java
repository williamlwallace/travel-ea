package repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionException;
import models.User;
import models.NewsFeedEvent;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import io.ebean.PagedList;

public class NewsFeedEventRepositoryTest extends repository.RepositoryTest {

    private static NewsFeedEventRepository newsFeedEventRepository;

    @Before
    public void runEvolutions() {
        applyEvolutions("test/destination/");
    }

    @BeforeClass
    public static void instantiateRepository() {
        newsFeedEventRepository = fakeApp.injector().instanceOf(NewsFeedEventRepository.class);
    }

    private boolean checkFirstEvent(NewsFeedEvent newsFeedEvent) {
        assertEquals(Long.valueOf(1), newsFeedEvent.guid);
        assertEquals(Long.valueOf(1), newsFeedEvent.userId);
        assertNull(newsFeedEvent.destId);
        assertEquals("NEW_PROFILE_PHOTO", newsFeedEvent.eventType);
        assertEquals(Long.valueOf(1), newsFeedEvent.refId);

        return true;
    }

    private NewsFeedEvent createEvent() {
        User user = new User();
        user.id = 1L;

        NewsFeedEvent newsFeedEvent = new NewsFeedEvent();
        newsFeedEvent.userId = 1L;
        newsFeedEvent.destId = 1L;
        newsFeedEvent.eventType = "LINK_DESTINATION_PHOTO";
        newsFeedEvent.refId = 2L;

        return newsFeedEvent;
    }

    @Test
    public void getNewsFeedEventById() {
        NewsFeedEvent newsFeedEvent = newsFeedEventRepository.getEvent(1L).join();

        assertTrue(checkFirstEvent(newsFeedEvent));
    }

    @Test
    public void getNewsFeedEventByIdDoesNotExist() {
        NewsFeedEvent newsFeedEvent = newsFeedEventRepository.getEvent(99999L).join();

        assertNull(newsFeedEvent);
    }

    @Test
    public void addEvent() {
        assertEquals((Long) 4L, newsFeedEventRepository.addNewsFeedEvent(createEvent()).join());
    }

    @Test(expected = CompletionException.class)
    public void addEventPrimaryKeyError() {
        NewsFeedEvent newsFeedEvent = createEvent();
        newsFeedEvent.guid = 1L;

        newsFeedEventRepository.addNewsFeedEvent(newsFeedEvent).join();
    }

    @Test
    public void personalEventFeed() {
        List<Long> users = new ArrayList<>();
        users.add(2L);
        PagedList<NewsFeedEvent> eventFeed = newsFeedEventRepository.getPagedEvents(users, null, 1, 10).join();
        assertEquals(2, eventFeed.getList().size());
        for (NewsFeedEvent event : eventFeed.getList()) {
           assertEquals(Long.valueOf(2), event.userId);
        }
    }

    @Test
    public void destEventFeed() {
        List<Long> trips = new ArrayList<>();
        trips.add(2L);
        PagedList<NewsFeedEvent> eventFeed = newsFeedEventRepository.getPagedEvents(null, trips, 1, 10).join();
        assertEquals(1, eventFeed.getList().size());
        for (NewsFeedEvent event : eventFeed.getList()) {
           assertEquals(Long.valueOf(2), event.destId);
        }
    }

    @Test
    public void homeEventFeed() {
        List<Long> dests = new ArrayList<>();
        dests.add(2L);
        List<Long> users = new ArrayList<>();
        users.add(2L);
        users.add(1L);
        PagedList<NewsFeedEvent> eventFeed = newsFeedEventRepository.getPagedEvents(users, dests, 1, 10).join();
        assertEquals(3, eventFeed.getList().size());
        assertTrue(checkFirstEvent(eventFeed.getList().get(2)));
    }

    @Test
    public void ExploreEventFeed() {
        PagedList<NewsFeedEvent> eventFeed = newsFeedEventRepository.getPagedEvents(null, null, 1, 10).join();
        assertEquals(3, eventFeed.getList().size());
        assertTrue(checkFirstEvent(eventFeed.getList().get(2)));
    }
}
