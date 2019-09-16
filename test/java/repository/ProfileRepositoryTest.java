package repository;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import io.ebean.PagedList;
import java.util.Arrays;
import java.util.List;
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

    @Test
    public void getUsersFollowingDestination() {
        List<Profile> profiles = profileRepository.getUsersFollowingDestination(1L, null, 1, 20).join().getList();
        assertEquals(3, profiles.size());

        // Test that profiles are ordered by follower count, should be 2 last as 1 and 4 have more than 2
        assertEquals(Long.valueOf(2), profiles.get(profiles.size()-1).userId);
    }
}
