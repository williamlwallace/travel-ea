package repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionException;
import models.CountryDefinition;
import models.Destination;
import models.Tag;
import models.User;
import org.junit.Before;
import org.junit.Test;

public class DestinationRepositoryTest extends repository.RepositoryTest {

    private static DestinationRepository destinationRepository;


    @Before
    public void runEvolutions() {
        applyEvolutions("test/destination/");
    }

    @Before
    public void instantiateRepository() {
        destinationRepository = fakeApp.injector().instanceOf(DestinationRepository.class);
    }

    private boolean checkFirstDestination(Destination destination) {
        assertEquals("Eiffel Tower", destination.name);
        assertEquals("Monument", destination.destType);
        assertEquals("Paris", destination.district);
        assertEquals(Long.valueOf(1), destination.country.id);
        assertEquals(Long.valueOf(1), destination.id);
        assertEquals(2, destination.travellerTypes.size());
        assertEquals(2, destination.travellerTypesPending.size());

        Tag testTag = new Tag("sports");
        Tag testTag2 = new Tag("music");
        assertEquals(2, destination.tags.size());
        assertTrue(destination.tags.contains(testTag));
        assertTrue(destination.tags.contains(testTag2));

        return true;
    }

    private boolean checkFirstPublicDestination(Destination destination) {
        assertEquals("Public dest one", destination.name);
        assertEquals("Monument", destination.destType);
        assertEquals("Paris", destination.district);
        assertEquals(Long.valueOf(1), destination.country.id);
        assertEquals(Long.valueOf(9), destination.id);
        assertEquals(0, destination.travellerTypes.size());
        assertEquals(0, destination.travellerTypesPending.size());
        assertEquals(0, destination.tags.size());

        return true;
    }

    private Destination createDestination() {
        User user = new User();
        user.id = 1L;

        CountryDefinition countryDefinition = new CountryDefinition();
        countryDefinition.id = 1L;

        Destination destination = new Destination();
        destination.name = "My destination";
        destination.user = user;
        destination.country = countryDefinition;
        destination.destType = "Sick spot";
        destination.district = "Merv";
        destination.longitude = -1.0;
        destination.latitude = 1.0;
        destination.isPublic = true;

        return destination;
    }

    @Test
    public void getDestinationById() {
        Destination destination = destinationRepository.getDestination(1L).join();

        assertTrue(checkFirstDestination(destination));
    }

    @Test
    public void getDestinationByIdDoesNotExist() {
        Destination destination = destinationRepository.getDestination(99999L).join();

        assertNull(destination);
    }

    @Test
    public void getDestinationByIdDeleted() {
        Destination destination = destinationRepository.getDestination(11L).join();

        assertNull(destination);
    }

    @Test
    public void getDestinationDeletedDestination() {
        Destination destination = destinationRepository.getDeletedDestination(11L).join();

        assertNotNull(destination);
        assertEquals((Long) 11L, destination.id);
        assertEquals("Deleted dest one", destination.name);
    }

    @Test
    public void getDestinationDeletedDestinationNotDeleted() {
        Destination destination = destinationRepository.getDeletedDestination(3L).join();

        assertNotNull(destination);
        assertEquals((Long) 3L, destination.id);
        assertEquals("The Eiffel Tower", destination.name);
    }

    @Test
    public void getDestinationDeletedDestinationDoesNotExist() {
        Destination destination = destinationRepository.getDeletedDestination(99999L).join();

        assertNull(destination);
    }

    @Test
    public void getAllDestinationsForUser() {
        List<Destination> destinations = destinationRepository.getAllDestinations(1L).join();

        assertEquals(8, destinations.size());

        Destination destination = destinations.get(0);

        assertTrue(checkFirstDestination(destination));
        assertTrue(checkFirstPublicDestination(destinations.get(6)));
    }

    @Test
    public void getAllPublicDestinations() {
        List<Destination> destinations = destinationRepository.getAllPublicDestinations().join();

        assertEquals(2, destinations.size());
        assertTrue(checkFirstPublicDestination(destinations.get(0)));
    }

    @Test
    public void getAllDestinationsAdmin() {
        List<Destination> destinations = destinationRepository.getAllDestinationsAdmin().join();

        assertEquals(10, destinations.size());
        assertTrue(checkFirstDestination(destinations.get(0)));
        assertTrue(checkFirstPublicDestination(destinations.get(8)));
    }

    @Test
    public void addDestination() {
        assertEquals((Long) 12L, destinationRepository.addDestination(createDestination()).join());
    }

    @Test(expected = CompletionException.class)
    public void addDestinationPrimaryKeyError() {
        Destination destination = createDestination();
        destination.id = 1L;

        destinationRepository.addDestination(destination).join();
    }

    @Test
    public void deleteDestination() {
        assertEquals((Integer) 1, destinationRepository.deleteDestination(1L).join());
    }

    @Test
    public void deleteDestinationDoesNotExist() {
        assertEquals((Integer) 0, destinationRepository.deleteDestination(99999L).join());
    }

    @Test
    public void updateDestination() {
        Destination destination = createDestination();
        destination.id = 1L;

        assertEquals(destination, destinationRepository.updateDestination(destination).join());

        Destination updatedDestination = destinationRepository.getDestination(1L).join();

        assertEquals(destination.id, updatedDestination.id);
        assertEquals(destination.name, updatedDestination.name);
        assertEquals(destination.country.id, updatedDestination.country.id);
        assertEquals(destination.tags.size(), destination.tags.size());
    }

    @Test(expected = CompletionException.class)
    public void updateDestinationInvalidId() {
        Destination destination = createDestination();
        destination.id = 99999L;

        destinationRepository.updateDestination(destination).join();
    }

    @Test
    public void changeDestinationOwner() {
        assertEquals((Integer) 1, destinationRepository.changeDestinationOwner(1L, 2L).join());

        Destination destination = destinationRepository.getDestination(1L).join();

        assertEquals((Long) 2L, destination.user.id);
    }

    @Test
    public void changeDestinationOwnerInvalidDestination() {
        assertEquals((Integer) 0, destinationRepository.changeDestinationOwner(99999L, 2L).join());
    }

    @Test(expected = CompletionException.class)
    public void changeDestinationOwnerInvalidUser() {
        destinationRepository.changeDestinationOwner(1L, 99999L).join();
    }

    @Test
    public void setDestinationToPublic() {
        assertFalse(destinationRepository.getDestination(1L).join().isPublic);
        destinationRepository.setDestinationToPublicInDatabase(1L);
        assertTrue(destinationRepository.getDestination(1L).join().isPublic);
    }

    @Test
    public void getSimilarDestinations() {
        Destination destination = destinationRepository.getDestination(1L).join();
        assertNotNull(destination);

        List<Destination> similarDestinations = destinationRepository
            .getSimilarDestinations(destination);

        assertEquals(3, similarDestinations.size());
    }

    @Test
    public void updateDestinationNewTag() {
        Destination destination = destinationRepository.getDestination(1L).join();
        assertNotNull(destination);

        Tag newTag = new Tag("New Tag");

        destination.tags.add(newTag);

        destinationRepository.updateDestination(destination);

        Destination updatedDestination = destinationRepository.getDestination(1L).join();
        for (Tag tag : updatedDestination.tags) {
        }
    }

    @Test
    public void getDestinationTravellerTypeRequest() {
        List<Destination> destinations = destinationRepository.getAllDestinationsWithRequests().join();

        assertEquals(2, destinations.size());
        assertEquals(2, destinations.get(0).travellerTypesPending.size());
    }

    @Test
    public void getDestinationsById() {
        Set<Long> destinationIds = new HashSet<>(Arrays.asList(1L, 3L, 5L));
        List<Destination> destinations = destinationRepository.getDestinationsById(destinationIds).join();

        assertEquals(3, destinations.size());

        for (Destination dest : destinations) {
            assertTrue(destinationIds.contains(dest.id));
        }
    }

    @Test
    public void getDestinationsByIdEmpty() {
        Set<Long> destinationIds = new HashSet<>();
        List<Destination> destinations = destinationRepository.getDestinationsById(destinationIds).join();

        assertTrue(destinations.isEmpty());
    }

    @Test
    public void getDestinationFollowerCount() {
        Long count = destinationRepository.getDestinationFollowerCount(1L).join();
        assertEquals(Long.valueOf(2), count);
    }
}
