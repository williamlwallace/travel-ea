package repository;

import models.Destination;
import org.junit.Before;
import org.junit.Test;

public class DestinationRepositoryTest extends repository.RepositoryTest {

    @Before
    public void runEvolutions() {
        applyEvolutions("test/destination/");
    }

    @Test
    public void getDestination() {
        Destination destination = destinationRepository.getDestination(1L).join();

        System.out.println(destination.id + " " + destination.name);
        System.out.println(destination.destinationTags.get(0).id + " " + destination.destinationTags.get(0).name);

        assert(destination.destinationTags.get(0).name == "sports");
    }
}
