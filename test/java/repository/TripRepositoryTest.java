package repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.CompletionException;
import models.Destination;
import models.Tag;
import models.Trip;
import org.junit.Before;
import org.junit.Test;

public class TripRepositoryTest extends repository.RepositoryTest {

    private static TripRepository tripRepository;


    @Before
    public void runEvolutions() {
        applyEvolutions("test/trip/");
    }

    @Before
    public void instantiateRepository() {
        tripRepository = fakeApp.injector().instanceOf(TripRepository.class);
    }

    private boolean checkSecondTrip(Trip trip) {
        assertNotNull(trip);

        assertEquals((Long) 2L, trip.id);
        assertTrue(trip.isPublic);
        assertEquals((Long) 2L, trip.userId);

        assertEquals(2, trip.tripDataList.size());
        assertEquals((Long) 1L, trip.tripDataList.get(0).destination.id);
        assertEquals((Long) 4L, trip.tripDataList.get(1).destination.id);

        assertEquals(1, trip.tags.size());
        assertTrue(trip.tags.contains(new Tag("sports")));

        return true;
    }

    @Test
    public void insertTrip() {
        Trip trip = new Trip();
        trip.userId = 1L;

        assertEquals((Long) 4L, tripRepository.insertTrip(trip).join());
    }

    @Test(expected = CompletionException.class)
    public void insertTripInvalidUserReference() {
        Trip trip = new Trip();
        trip.userId = 99999L;

        tripRepository.insertTrip(trip).join();
    }

    @Test
    public void updateTrip() {
        Trip trip = tripRepository.getTripById(2L).join();

        assertTrue(checkSecondTrip(trip));

        trip.userId = 2L;
        Destination destination = new Destination();
        destination.id = 1L;
        trip.tripDataList.get(1).destination = destination;

        assertTrue(tripRepository.updateTrip(trip).join());

        Trip updatedTrip = tripRepository.getTripById(2L).join();
        assertNotNull(updatedTrip);
        assertEquals((Long) 2L, updatedTrip.id);
        assertEquals(2, updatedTrip.tripDataList.size());
        assertEquals((Long) 1L, updatedTrip.tripDataList.get(1).destination.id);
        assertEquals((Long) 2L, trip.userId);
    }

    @Test(expected = CompletionException.class)
    public void updateTripInvalidReferenceId() {
        Trip trip = tripRepository.getTripById(2L).join();

        assertNotNull(trip);
        assertEquals((Long) 2L, trip.id);
        trip.userId = 99999L;

        assertFalse(tripRepository.updateTrip(trip).join());
    }

    @Test
    public void updateTripInvalidTripId() {
        Trip trip = new Trip();
        trip.id = 99999L;

        assertFalse(tripRepository.updateTrip(trip).join());
    }

    @Test
    public void deleteTrip() {
        assertEquals((Integer) 1, tripRepository.deleteTrip(1L).join());
        assertNull(tripRepository.getTripById(1L).join());
        assertNotNull(tripRepository.getDeletedTrip(1L).join());
    }

    @Test
    public void deleteTripInvalidId() {
        assertEquals((Integer) 0, tripRepository.deleteTrip(99999L).join());
    }

    @Test
    public void getAllUsersTrips() {
        List<Trip> trips = tripRepository.getAllUserTrips(2L).join();

        assertEquals(1, trips.size());
        assertTrue(checkSecondTrip(trips.get(0)));
    }

    @Test
    public void getAllUsersTripsInvalidUser() {
        List<Trip> trips = tripRepository.getAllUserTrips(99999L).join();

        assertEquals(0, trips.size());
    }

    @Test
    public void getAllUsersTripsNoTrips() {
        List<Trip> trips = tripRepository.getAllUserTrips(3L).join();

        assertEquals(0, trips.size());
    }

    @Test
    public void getAllPublicTripsOrUsersTrips() {
        List<Trip> trips = tripRepository.searchTrips(1L, 1L, "", true, 1, 10, true, true).join().getList();

        assertEquals(2, trips.size());
        assertTrue(checkSecondTrip(trips.get(1)));
        assertEquals((Long) 1L, trips.get(0).id);
        assertEquals((Long) 1L, trips.get(0).userId);
    }

    @Test
    public void getAllPublicTripsOrUsersTripsWithSearch() {
        List<Trip> trips = tripRepository.searchTrips(1L, 1L, "london", true, 1, 10, true, true).join().getList();

        assertEquals(1, trips.size());
        assertTrue(checkSecondTrip(trips.get(0)));
    }

    @Test
    public void getAllPublicTripsOrUsersTripsDescPaged() {
        List<Trip> trips = tripRepository.searchTrips(1L, 1L, "", false, 1, 1, true, true).join().getList();

        assertEquals(1, trips.size());
        assertTrue(checkSecondTrip(trips.get(0)));
    }

    @Test
    public void getAllPublicTripsOrUsersTripsSecondUser() {
        List<Trip> trips = tripRepository.searchTrips(2L, 2L, "", true, 1, 10, true, true).join().getList();

        assertEquals(1, trips.size());
        assertTrue(checkSecondTrip(trips.get(0)));
    }

    @Test
    public void getAllPublicTripsOrUsersTripsInvalidUserId() {
        List<Trip> trips = tripRepository.searchTrips(99999L, 999999l, "", true, 1, 10, true, true).join().getList();

        assertEquals(1, trips.size());
        assertTrue(checkSecondTrip(trips.get(0)));
    }

    @Test
    public void getAllUsersPublicTrips() {
        List<Trip> trips = tripRepository.getAllPublicUserTrips(2L).join();

        assertEquals(1, trips.size());
        assertTrue(checkSecondTrip(trips.get(0)));
    }

    @Test
    public void getAllUsersPublicTripsNoTrips() {
        List<Trip> trips = tripRepository.getAllPublicUserTrips(1L).join();

        assertEquals(0, trips.size());
    }

    @Test
    public void getAllUsersPublicTripsInvalidUser() {
        List<Trip> trips = tripRepository.getAllPublicUserTrips(99999L).join();

        assertEquals(0, trips.size());
    }

    @Test
    public void getAllTrips() {
        List<Trip> trips = tripRepository.searchTrips(1L, 1L, "", true, 1, 10, false, true).join().getList();;

        assertEquals(2, trips.size());
        assertTrue(checkSecondTrip(trips.get(1)));
        assertEquals((Long) 1L, trips.get(0).id);
        assertEquals((Long) 1L, trips.get(0).userId);
    }

    @Test
    public void getTripById() {
        Trip trip = tripRepository.getTripById(2L).join();

        assertTrue(checkSecondTrip(trip));
    }

    @Test
    public void getTripByIdInvalidId() {
        Trip trip = tripRepository.getTripById(99999L).join();

        assertNull(trip);
    }

    @Test
    public void getDeletedTrip() {
        Trip trip = tripRepository.getDeletedTrip(3L).join();

        assertNotNull(trip);
    }

    @Test
    public void getDeletedTripNotDeleted() {
        Trip trip = tripRepository.getTripById(2L).join();

        assertTrue(checkSecondTrip(trip));
    }

    @Test
    public void getDeletedTripInvalidId() {
        Trip trip = tripRepository.getTripById(99999L).join();

        assertNull(trip);
    }
}