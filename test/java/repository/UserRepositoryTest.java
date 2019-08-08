package repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CompletionException;
import models.Destination;
import models.Tag;
import models.UsedTag;
import models.User;
import org.junit.Before;
import org.junit.Test;

public class UserRepositoryTest extends repository.RepositoryTest {

    private static UserRepository userRepository;


    @Before
    public void runEvolutions() {
        applyEvolutions("test/user/");
    }

    @Before
    public void instantiateRepository() {
        userRepository = fakeApp.injector().instanceOf(UserRepository.class);
    }

    @Test
    public void findUserById() {
        User user = userRepository.findID(1L).join();

        assertNotNull(user);
        assertEquals((Long) 1L, user.id);
        assertTrue(user.admin);
        assertEquals("dave@gmail.com", user.username);
        assertEquals(3, user.usedTags.size());
        assertEquals((Long) 2L, user.usedTags.get(1).tag.id);
        assertEquals("#TravelEA", user.usedTags.get(0).tag.name);
    }

    @Test
    public void findUserByIdInvalidID() {
        User user = userRepository.findID(99999L).join();

        assertNull(user);
    }

    @Test
    public void findDeletedUserById() {
        User user = userRepository.findDeletedID(3L).join();

        assertNotNull(user);
        assertEquals((Long) 3L, user.id);
        assertFalse(user.admin);
        assertTrue(user.deleted);
        assertEquals("deleted@gmail.com", user.username);
    }

    @Test
    public void findDeletedUserByIdInvalidId() {
        User user = userRepository.findDeletedID(99999L).join();

        assertNull(user);
    }

    @Test
    public void findUserByUsername() {
        User user = userRepository.findUserName("bob@gmail.com").join();

        assertNotNull(user);
        assertEquals("bob@gmail.com", user.username);
        assertEquals((Long) 2L, user.id);
    }

    @Test
    public void findUserByUsernameInvalidUsername() {
        User user = userRepository.findUserName("bob@gmWHOOPSIEDOOPSIEail.com").join();

        assertNull(user);
    }

    @Test
    public void updateUser() {
        User user = userRepository.findID(2L).join();
        assertNotNull(user);
        assertEquals(1, user.usedTags.size());

        user.username = "New username";
        user.password = "Sick";

        Tag tag = new Tag();
        tag.id = 1L;

        UsedTag usedTag = new UsedTag();
        usedTag.tag = tag;
        usedTag.user = user;

        tag.usedTags.add(usedTag);
        user.usedTags.add(usedTag);

        for (UsedTag yeet : user.usedTags) {
            System.out.println(yeet.tag.id + " " + yeet.tag.name);
        }

        assertEquals((Long) 2L, userRepository.updateUser(user).join());

        User updatedUser = userRepository.findID(2L).join();
        assertNotNull(updatedUser);
        assertEquals("New username", updatedUser.username);
        assertEquals("Sick", updatedUser.password);
        assertEquals(2, updatedUser.usedTags.size());
        assertEquals("Russia", updatedUser.usedTags.get(0).tag.name);
    }

    @Test(expected = CompletionException.class)
    public void updateUserInvalidReferencedId() {
        User user = userRepository.findID(2L).join();
        assertNotNull(user);
        user.usedTags.get(0).tag.id = 99999L;

        userRepository.updateUser(user).join();
    }

    @Test
    public void insertUser() {
        assertNull(userRepository.findID(4L).join());
        User user = new User();
        user.username = "test@email.com";
        user.password = "123";
        user.salt = "456";

        Tag tag = new Tag();
        tag.id = 3L;
        UsedTag usedTag = new UsedTag();
        usedTag.tag = tag;
        usedTag.user = user;

        user.usedTags.add(usedTag);

        userRepository.insertUser(user).join();

        User insertedUser = userRepository.findID(4L).join();
        assertNotNull(insertedUser);
        assertEquals("test@email.com", insertedUser.username);
        assertEquals(1, insertedUser.usedTags.size());
        assertEquals((Long) 3L, insertedUser.usedTags.get(0).tag.id);
        assertEquals("#TravelEA", insertedUser.usedTags.get(0).tag.name);
    }

    @Test
    public void insertUserInvalidReferencedId() {
        assertNull(userRepository.findID(4L).join());
        User user = new User();
        user.username = "test@email.com";
        user.password = "123";
        user.salt = "456";

        Tag tag = new Tag();
        tag.id = 99999L;

        UsedTag usedTag = new UsedTag();
        usedTag.tag = tag;
        usedTag.user = user;
        user.usedTags.add(usedTag);

        userRepository.insertUser(user).join();
    }

    @Test
    public void deleteUser() {
        int rowsDeleted = userRepository.deleteUser(1L).join();
        assertEquals(1, rowsDeleted);

        assertNull(userRepository.findID(1L).join());
        assertNotNull(userRepository.findDeletedID(1L).join());
    }

    @Test
    public void deleteUserInvalidId() {
        int rowsDeleted = userRepository.deleteUser(99999L).join();
        assertEquals(0, rowsDeleted);
    }

//    @Test
//    public void updateUserTags() {
//        User originalUser = userRepository.findID(3L).join();
//
//        assertEquals(0, originalUser.usedTags.size());
//
//        Destination originalDestination = new Destination();
//        Tag originalTag = new Tag();
//        originalTag.id = 1L;
//        originalDestination.tags.add(originalTag);
//
//        Destination newDestination = new Destination();
//        Tag newTag = new Tag();
//        newTag.id = 2L;
//        newDestination.tags.add(originalTag);
//        newDestination.tags.add(newTag);
//
//        originalUser.updateUserTags(originalDestination, newDestination);
//        userRepository.updateUser(originalUser);
//
//        User updatedUser = userRepository.findID(3L).join();
//        assertEquals(1, updatedUser.usedTags.size());
//        assertEquals((Long) 2L, updatedUser.usedTags.get(0).id);
//
//    }

}