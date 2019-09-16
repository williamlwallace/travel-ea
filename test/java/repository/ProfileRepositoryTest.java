package repository;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import models.Profile;
import org.junit.Before;
import org.junit.Test;

public class ProfileRepositoryTest extends repository.RepositoryTest {

    private static ProfileRepository profileRepository;

    @Before
    public void runEvolutions() {
        applyEvolutions("test/profile/");
    }

    @Before
    public void instantiateRepository() {
        profileRepository = fakeApp.injector().instanceOf(ProfileRepository.class);
    }

    @Test
    public void getDeletedProfile() {
        Profile profile = profileRepository.getDeletedProfile(6L).join();
        assertNotNull(profile);
        assertTrue(profile.deleted);
    }

    @Test
    public void getProfileFollowerCounts() {
        Profile profile = profileRepository.getProfileFollowerCounts(1L).join();
        assertEquals(Long.valueOf(2), profile.followerUsersCount);
        assertEquals(Long.valueOf(1), profile.followingUsersCount);
        assertEquals(Long.valueOf(3), profile.followingDestinationsCount);
    }
}
