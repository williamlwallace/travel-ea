package repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionException;
import models.CountryDefinition;
import models.Destination;
import models.Tag;
import models.User;
import models.NewsFeedEvent;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import io.ebean.ExpressionList;
import io.ebean.PagedList;
import io.ebean.Expression;

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
        assertEquals(null, newsFeedEvent.destId);
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
        List<Long> users = new ArrayList<Long>();
        users.add(2L);
        PagedList<NewsFeedEvent> eventFeed = newsFeedEventRepository.getPagedEvents(users, null, 1, 10).join();
        assertEquals(2, eventFeed.getList().size());
        for (NewsFeedEvent event : eventFeed.getList()) {
           assertEquals(Long.valueOf(1), event.userId);
        }
    }

    @Test
    public void ExploreEventFeed() {
        PagedList<NewsFeedEvent> eventFeed = newsFeedEventRepository.getPagedEvents(null, null, 1, 10).join();
        assertEquals(3, eventFeed.getList().size());
        assertTrue(checkFirstEvent(eventFeed.getList().get(2)));
    }
}
