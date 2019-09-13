package repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import io.ebean.DataIntegrityException;
import io.ebean.PagedList;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
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

    private ResultSet getTagsForUserFromDatabase(long id) throws SQLException {
        PreparedStatement statement = connection
            .prepareStatement("SELECT * FROM UsedTag WHERE user_id = ?;");

        statement.setLong(1, id);

        return statement.executeQuery();
    }

    private int countUsedTagsForUser(long id) throws SQLException {
        ResultSet resultSet = getTagsForUserFromDatabase(id);

        int count = 0;

        while (resultSet.next()) {
            count++;
        }

        return count;
    }

    @Test
    public void findUserById() {
        User user = userRepository.findID(1L).join();

        assertNotNull(user);
        assertEquals((Long) 1L, user.id);
        assertTrue(user.admin);
        assertEquals("dave@gmail.com", user.username);
        assertEquals(3, user.usedTags.size());
        boolean found = false;

        for (UsedTag usedTag : user.usedTags) {
            if (usedTag.tag.name.equals("#TravelEA")) {
                found = true;
            }
        }

        assertTrue(found);
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

        Tag tag = new Tag("Russia", 1L);

        UsedTag usedTag = new UsedTag();
        usedTag.tag = tag;
        usedTag.user = user;

        tag.usedTags.add(usedTag);
        user.usedTags.add(usedTag);
        usedTag.timeUsed = LocalDateTime.now();

        assertEquals((Long) 2L, userRepository.updateUser(user).join());

        User updatedUser = userRepository.findID(2L).join();
        assertNotNull(updatedUser);
        assertEquals("New username", updatedUser.username);
        assertEquals("Sick", updatedUser.password);

        assertEquals(2, updatedUser.usedTags.size());
        boolean found = false;

        for (UsedTag usedTag1 : updatedUser.usedTags) {
            if (usedTag1.tag.name.equals("Russia")) {
                found = true;
            }
        }

        assertTrue(found);
    }

    @Test
    public void updateUserInvalidReferencedId() throws SQLException {
        User user = userRepository.findID(2L).join();
        assertNotNull(user);

        for (UsedTag usedTag : user.usedTags) {
            usedTag.tag.id = 99999L;
        }

        userRepository.updateUser(user).join();

        PreparedStatement statement = connection
            .prepareStatement("SELECT * FROM UsedTag WHERE user_id = 2;");

        ResultSet resultSet = statement.executeQuery();

        boolean invalidTagFound = false;

        while (resultSet.next()) {
            if (resultSet.getInt("tag_id") == 99999) {
                invalidTagFound = true;
            }
        }

        assertFalse(invalidTagFound);
    }

    @Test
    public void insertUser() {
        assertNull(userRepository.findID(4L).join());
        User user = new User();
        user.username = "test@email.com";
        user.password = "123";
        user.salt = "456";

        Tag tag = new Tag("#TravelEA", 3L);
        UsedTag usedTag = new UsedTag();
        usedTag.tag = tag;
        usedTag.user = user;

        user.usedTags.add(usedTag);

        userRepository.insertUser(user).join();

        User insertedUser = userRepository.findID(4L).join();
        assertNotNull(insertedUser);
        assertEquals("test@email.com", insertedUser.username);
        assertEquals(1, insertedUser.usedTags.size());

        boolean found = false;
        for (UsedTag usedTag1 : insertedUser.usedTags) {
            if (usedTag1.tag.name.equals("#TravelEA") && usedTag1.tag.id == 3L) {
                found = true;
            }
        }

        assertTrue(found);
    }

    @Test(expected = CompletionException.class)
    public void insertUserInvalidReferencedId() {
        assertNull(userRepository.findID(4L).join());
        User user = new User();
        user.username = "test@email.com";
        user.password = "123";
        user.salt = "456";

        Tag tag = new Tag("", 99999L);

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

    @Test
    public void updateUserTags() throws SQLException {
        User originalUser = userRepository.findID(2L).join();

        assertNotNull(originalUser);
        assertEquals(1, originalUser.usedTags.size());

        assertEquals(1, countUsedTagsForUser(2L));

        Destination originalDestination = new Destination();
        Tag originalTag = new Tag("Russia");
        originalDestination.tags.add(originalTag);

        Destination newDestination = new Destination();
        Tag newTag = new Tag("#TravelEA");
        newDestination.tags.add(originalTag);
        newDestination.tags.add(newTag);

        userRepository.updateUsedTags(originalUser, originalDestination, newDestination);

        User updatedUser = userRepository.findID(2L).join();
        assertEquals(2, updatedUser.usedTags.size());

        boolean found = false;

        for (UsedTag usedTag : updatedUser.usedTags) {
            if (usedTag.tag.equals(newTag)) {
                found = true;
            }
        }

        assertTrue(found);

        assertEquals(2, countUsedTagsForUser(2L));
    }

    @Test
    public void updateUserTagsNoChanges() throws SQLException {
        User originalUser = userRepository.findID(2L).join();

        assertNotNull(originalUser);
        assertEquals(1, originalUser.usedTags.size());

        assertEquals(1, countUsedTagsForUser(2L));

        Destination originalDestination = new Destination();
        Tag originalTag = new Tag("Russia");
        originalDestination.tags.add(originalTag);

        userRepository.updateUsedTags(originalUser, originalDestination, originalDestination);

        User updatedUser = userRepository.findID(2L).join();
        assertEquals(1, updatedUser.usedTags.size());

        assertEquals(1, countUsedTagsForUser(2L));
    }

    @Test
    public void updateUserTagsNoNewTags() throws SQLException, InterruptedException {
        User originalUser = userRepository.findID(2L).join();

        assertNotNull(originalUser);
        assertEquals(1, originalUser.usedTags.size());

        assertEquals(1, countUsedTagsForUser(2L));

        ResultSet originalResultSet = getTagsForUserFromDatabase(2L);
        originalResultSet.next();
        Timestamp originalTimestamp = originalResultSet.getTimestamp("time_used");

        // Sleep for one second to ensure timestamp changes
        Thread.sleep(1000);

        Destination originalDestination = new Destination();
        Tag originalTag = new Tag("Russia");
        originalDestination.tags.add(originalTag);

        Destination newDestination = new Destination();
        Tag newTag = new Tag("sports");
        newDestination.tags.add(originalTag);
        newDestination.tags.add(newTag);

        userRepository.updateUsedTags(originalUser, originalDestination, newDestination);

        User updatedUser = userRepository.findID(2L).join();
        assertEquals(1, updatedUser.usedTags.size());

        assertEquals(1, countUsedTagsForUser(2L));
        ResultSet updatedResultSet = getTagsForUserFromDatabase(2L);
        updatedResultSet.next();
        Timestamp updatedTimeStamp = updatedResultSet.getTimestamp("time_used");

        assertTrue(updatedTimeStamp.after(originalTimestamp));
    }

    @Test
    public void updateUserTagsNoInput() throws SQLException {
        User originalUser = userRepository.findID(2L).join();

        assertNotNull(originalUser);
        assertEquals(1, originalUser.usedTags.size());

        assertEquals(1, countUsedTagsForUser(2L));

        ResultSet originalResultSet = getTagsForUserFromDatabase(2L);
        originalResultSet.next();
        Timestamp originalTimestamp = originalResultSet.getTimestamp("time_used");

        Destination originalDestination = new Destination();
        Destination newDestination = new Destination();

        userRepository.updateUsedTags(originalUser, originalDestination, newDestination);

        User updatedUser = userRepository.findID(2L).join();
        assertEquals(1, updatedUser.usedTags.size());

        assertEquals(1, countUsedTagsForUser(2L));
        ResultSet updatedResultSet = getTagsForUserFromDatabase(2L);
        updatedResultSet.next();
        Timestamp updatedTimeStamp = updatedResultSet.getTimestamp("time_used");

        assertTrue(updatedTimeStamp.equals(originalTimestamp));
    }

    @Test(expected = DataIntegrityException.class)
    public void updateUserTagsTagDoesNotExist() throws SQLException {
        User originalUser = userRepository.findID(2L).join();

        assertNotNull(originalUser);
        assertEquals(1, originalUser.usedTags.size());

        assertEquals(1, countUsedTagsForUser(2L));

        Destination originalDestination = new Destination();
        Tag originalTag = new Tag("Russia");
        originalDestination.tags.add(originalTag);

        Destination newDestination = new Destination();
        Tag newTag = new Tag("blah");
        newDestination.tags.add(originalTag);
        newDestination.tags.add(newTag);

        userRepository.updateUsedTags(originalUser, originalDestination, newDestination);
    }

    @Test
    public void updateUserTagsNewTaggable() throws SQLException {
        User originalUser = userRepository.findID(2L).join();

        assertNotNull(originalUser);
        assertEquals(1, originalUser.usedTags.size());

        assertEquals(1, countUsedTagsForUser(2L));

        ResultSet originalResultSet = getTagsForUserFromDatabase(2L);
        originalResultSet.next();
        Timestamp originalTimestamp = originalResultSet.getTimestamp("time_used");

        Destination newDestination = new Destination();
        Tag newTag = new Tag("sports");
        newDestination.tags.add(new Tag("Russia"));
        newDestination.tags.add(newTag);

        userRepository.updateUsedTags(originalUser, newDestination);

        User updatedUser = userRepository.findID(2L).join();
        assertEquals(2, updatedUser.usedTags.size());

        assertEquals(2, countUsedTagsForUser(2L));
        ResultSet updatedResultSet = getTagsForUserFromDatabase(2L);
        updatedResultSet.next();
        Timestamp updatedTimeStamp = updatedResultSet.getTimestamp("time_used");

        assertTrue(updatedTimeStamp.after(originalTimestamp));
    }

    @Test
    public void userSearch() {
        PagedList<User> userList = userRepository.search(1L, "", null, true, 1, 10).join();
        assertEquals(1, userList.getList().size());
        boolean foundBob = false;
        for (User user : userList.getList()) {
            if (user.username.equals("bob@gmail.com")) {
                foundBob = true;
            }
        }
        assertTrue(foundBob);
    }

    @Test
    public void userSearchUsername() {
        PagedList<User> userList = userRepository.search(3L, "@gmail.com", null, true, 1, 10).join();
        assertEquals(2, userList.getList().size());
        boolean foundDave = false;
        boolean foundBob = false;
        for (User user : userList.getList()) {
            if (user.username.equals("dave@gmail.com")) {
                foundDave = true;
            }
            if (user.username.equals("bob@gmail.com")) {
                foundBob = true;
            }
        }
        assertTrue(foundDave);
        assertTrue(foundBob);
    }

    @Test
    public void userSearchSortByAsc() {
        PagedList<User> userList = userRepository.search(3L, "", "username", true, 1, 10).join();
        assertEquals(2, userList.getList().size());
        assertEquals("bob@gmail.com", userList.getList().get(0).username);
        assertEquals("dave@gmail.com", userList.getList().get(1).username);
    }

    @Test
    public void userSearchSortByDsc() {
        PagedList<User> userList = userRepository.search(3L, "", "username", false, 1, 10).join();
        assertEquals(2, userList.getList().size());
        assertEquals("bob@gmail.com", userList.getList().get(1).username);
        assertEquals("dave@gmail.com", userList.getList().get(0).username);
    }


}