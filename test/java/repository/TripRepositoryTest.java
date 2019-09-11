package repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import io.ebean.PagedList;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletionException;
import models.Destination;
import models.Tag;
import models.Trip;
import models.TripData;
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

    private List<Trip> getTripsFromDatabase(List<Long> tripIds) throws SQLException {
        List<Trip> trips = new ArrayList<>();

        for (Long tripId : tripIds) {
            PreparedStatement tripStatement = connection
                .prepareStatement("SELECT * FROM Trip WHERE id = ?;");
            tripStatement.setLong(1, tripId);

            ResultSet tripResultSet = tripStatement.executeQuery();

            Trip trip = null;

            while (tripResultSet.next()) {
                trip = new Trip();
                trip.id = tripResultSet.getLong("id");
                trip.userId = tripResultSet.getLong("user_id");
                trip.isPublic = tripResultSet.getBoolean("is_public");
                trip.deleted = tripResultSet.getBoolean("deleted");
                trip.tripDataList = new ArrayList<>();
                trips.add(trip);
            }

            if (trip != null) {
                PreparedStatement tripDataStatement = connection
                    .prepareStatement("SELECT * FROM TripData WHERE trip_id = ?;");
                tripDataStatement.setLong(1, tripId);

                ResultSet tripDataResultSet = tripDataStatement.executeQuery();

                while (tripDataResultSet.next()) {
                    TripData tripData = new TripData();
                    tripData.guid = tripDataResultSet.getLong("guid");
                    tripData.trip = trip;
                    tripData.position = tripDataResultSet.getLong("position");
                    Destination destination = new Destination();
                    destination.id = tripDataResultSet.getLong("destination_id");
                    tripData.destination = destination;
                    trip.tripDataList.add(tripData);
                }
            }
        }
        return trips;
    }

    @Test
    public void insertTrip() {
        Trip trip = new Trip();
        trip.userId = 1L;

        assertEquals((Long) 5L, tripRepository.insertTrip(trip).join());
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
        trip.tags = new HashSet<>();

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
        PagedList<Trip> trips = tripRepository.searchTrips(2L, 2L, "", true, 1, 10, true, false)
            .join();

        assertEquals(1, trips.getTotalCount());
        assertTrue(checkSecondTrip(trips.getList().get(0)));
    }

    @Test
    public void getAllUsersTripsInvalidUser() {
        PagedList<Trip> trips = tripRepository
            .searchTrips(99999L, 99999L, "", true, 1, 10, true, false).join();

        assertEquals(0, trips.getTotalCount());
    }

    @Test
    public void getAllUsersTripsNoTrips() {
        PagedList<Trip> trips = tripRepository.searchTrips(3L, 3L, "", true, 1, 10, true, false)
            .join();

        assertEquals(0, trips.getTotalCount());
    }

    @Test
    public void getAllPublicTripsOrUsersTrips() {
        List<Trip> trips = tripRepository.searchTrips(1L, 1L, "", true, 1, 10, true, true).join()
            .getList();

        assertEquals(3, trips.size());
        assertTrue(checkSecondTrip(trips.get(1)));
        assertEquals((Long) 1L, trips.get(0).id);
        assertEquals((Long) 1L, trips.get(0).userId);
    }

    @Test
    public void getAllPublicTripsOrUsersTripsWithSearch() {
        List<Trip> trips = tripRepository.searchTrips(1L, 1L, "london", true, 1, 10, true, true)
            .join().getList();

        assertEquals(1, trips.size());
        assertTrue(checkSecondTrip(trips.get(0)));
    }

    @Test
    public void getAllPublicTripsOrUsersTripsDescPaged() {
        List<Trip> trips = tripRepository.searchTrips(1L, 1L, "", false, 1, 10, true, true).join()
            .getList();

        assertEquals(3, trips.size());
        assertTrue(checkSecondTrip(trips.get(1)));
    }

    @Test
    public void getAllPublicTripsOrUsersTripsSecondUser() {
        List<Trip> trips = tripRepository.searchTrips(2L, 2L, "", true, 1, 10, true, true).join()
            .getList();

        assertEquals(3, trips.size());
        assertTrue(checkSecondTrip(trips.get(1)));
    }

    @Test
    public void getAllPublicTripsOrUsersTripsInvalidUserId() {
        List<Trip> trips = tripRepository.searchTrips(99999L, 2L, "", true, 1, 10, true, true)
            .join().getList();

        assertEquals(3, trips.size());
        assertTrue(checkSecondTrip(trips.get(1)));
    }

    @Test
    public void getAllUsersPublicTrips() {
        List<Trip> trips = tripRepository.searchTrips(2L, 2L, "", true, 1, 10, false, false).join()
            .getList();

        assertEquals(1, trips.size());
        assertTrue(checkSecondTrip(trips.get(0)));
    }

    @Test
    public void getAllUsersPublicTripsNoTrips() {
        List<Trip> trips = tripRepository.searchTrips(1L, 1L, "", true, 1, 10, false, false).join()
            .getList();

        assertEquals(0, trips.size());
    }

    @Test
    public void getAllUsersPublicTripsInvalidUser() {
        List<Trip> trips = tripRepository.searchTrips(99999L, 99999L, "", true, 1, 10, false, false)
            .join().getList();

        assertEquals(0, trips.size());
    }

    @Test
    public void getAllTrips() {
        List<Trip> trips = tripRepository.searchTrips(1L, 1L, "", true, 1, 10, false, true).join()
            .getList();

        assertEquals(3, trips.size());
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
        Trip trip = tripRepository.getDeletedTrip(2L).join();

        assertTrue(checkSecondTrip(trip));
    }

    @Test
    public void getDeletedTripInvalidId() {
        Trip trip = tripRepository.getDeletedTrip(99999L).join();

        assertNull(trip);
    }

    @Test
    public void copyTrip() throws SQLException {
        List<Long> ids = new ArrayList<>();
        ids.add(1L);
        List<Trip> trips = getTripsFromDatabase(ids);
        assertEquals(1, trips.size());
        Trip trip = trips.get(0);
        assertEquals((Long) 1L, trip.userId);
        assertEquals(3, trip.tripDataList.size());
        Long firstTripData = trip.tripDataList.get(0).guid;

        Long newTripId = tripRepository.copyTrip(trip, 3L).join();
        assertEquals((Long) 5L, newTripId);

        List<Long> copiedIds = new ArrayList<>();
        copiedIds.add(newTripId);
        List<Trip> copiedTrips = getTripsFromDatabase(copiedIds);
        assertEquals(1, copiedTrips.size());
        Trip copiedTrip = copiedTrips.get(0);

        assertEquals((Long) 5L, copiedTrip.id);
        assertEquals((Long) 3L, copiedTrip.userId);
        assertEquals(trip.isPublic, copiedTrip.isPublic);
        assertEquals(trip.tripDataList.size(), copiedTrip.tripDataList.size());
        for (TripData tripData : copiedTrip.tripDataList) {
            assert (tripData.guid > firstTripData);
        }
    }

    @Test(expected = CompletionException.class)
    public void copyTripInvalidUser() throws SQLException {
        List<Long> ids = new ArrayList<>();
        ids.add(1L);
        List<Trip> trips = getTripsFromDatabase(ids);
        assertEquals(1, trips.size());
        Trip trip = trips.get(0);

        tripRepository.copyTrip(trip, 99999L).join();
    }

    @Test(expected = CompletionException.class)
    public void copyTripInvalidTrip() {
        Trip trip = new Trip();
        trip.tripDataList = new ArrayList<>();
        TripData tripData = new TripData();
        Destination destination = new Destination();
        destination.id = 99999L;
        tripData.destination = destination;
        trip.tripDataList.add(tripData);
        tripRepository.copyTrip(trip, 1L).join();
    }
}