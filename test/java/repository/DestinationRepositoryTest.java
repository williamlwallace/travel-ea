package repository;

import static org.junit.Assert.assertEquals;

import models.Destination;
import org.junit.Before;
import org.junit.Test;

public class DestinationRepositoryTest extends repository.RepositoryTest {

    @Before
    public void runEvolutions() {
        applyEvolutions("");
    }

    @Test
    public void getDestination() {
        Destination destination = destinationRepository.getDestination(1L).join();

        assertEquals("Russia", destination.tags.get(0).name);
    }
}
