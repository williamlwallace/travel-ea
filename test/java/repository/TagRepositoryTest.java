package repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import models.Tag;
import org.junit.Before;
import org.junit.Test;

public class TagRepositoryTest extends repository.RepositoryTest {

    private static TagRepository tagRepository;


    @Before
    public void runEvolutions() {
        applyEvolutions("test/tag/");
    }

    @Before
    public void instantiateRepository() {
        tagRepository = fakeApp.injector().instanceOf(TagRepository.class);
    }

    private ResultSet getAllTagsFromDatabase() throws SQLException {
        PreparedStatement statement = connection
            .prepareStatement("SELECT * FROM Tag;");

        return statement.executeQuery();
    }

    private int getTagRowCount() throws SQLException {
        ResultSet resultSet = getAllTagsFromDatabase();

        int count = 0;
        while (resultSet.next()) {
            count++;
        }

        return count;
    }

    @Test
    public void addTagsAllNew() throws SQLException {
        assertEquals(3, getTagRowCount());

        Set<Tag> tags = new HashSet<>();
        tags.add(new Tag("These"));
        tags.add(new Tag("Don't"));
        tags.add(new Tag("Exist"));

        Set<Tag> returnedTags = tagRepository.addTags(tags).join();

        assertEquals(3, returnedTags.size());
        assertTrue(returnedTags.contains(new Tag("These")));
        assertTrue(returnedTags.contains(new Tag("Don't")));
        assertTrue(returnedTags.contains(new Tag("Exist")));

        ResultSet checkSet = getAllTagsFromDatabase();

        int checkCount = 0;
        boolean theseFound = false;
        boolean dontFound = false;
        boolean existFound = false;

        while (checkSet.next()) {
            if (checkSet.getString("name").equals("These")) {
                theseFound = true;
            }

            if (checkSet.getString("name").equals("Don't")) {
                dontFound = true;
            }

            if (checkSet.getString("name").equals("Exist")) {
                existFound = true;
            }
            checkCount++;
        }

        assertEquals(6, checkCount);
        assertTrue(theseFound);
        assertTrue(dontFound);
        assertTrue(existFound);
    }

    @Test
    public void addTagsSomeNew() throws SQLException {
        assertEquals(3, getTagRowCount());

        Set<Tag> tags = new HashSet<>();
        tags.add(new Tag("sports"));
        tags.add(new Tag("Russia"));
        tags.add(new Tag("New"));
        tags.add(new Tag("Tag"));

        Set<Tag> returnedTags = tagRepository.addTags(tags).join();

        assertEquals(4, returnedTags.size());
        assertTrue(returnedTags.contains(new Tag("New")));
        assertTrue(returnedTags.contains(new Tag("Tag")));

        ResultSet checkSet = getAllTagsFromDatabase();

        int checkCount = 0;
        boolean newFound = false;
        boolean tagFound = false;

        while (checkSet.next()) {
            if (checkSet.getString("name").equals("New")) {
                newFound = true;
            }

            if (checkSet.getString("name").equals("Tag")) {
                tagFound = true;
            }
            checkCount++;
        }

        assertEquals(5, checkCount);
        assertTrue(newFound);
        assertTrue(tagFound);
    }

    @Test
    public void addTagsNoTags() throws SQLException {
        assertEquals(3, getTagRowCount());

        Set<Tag> tags = new HashSet<>();

        Set<Tag> returnedTags = tagRepository.addTags(tags).join();

        assertEquals(0, returnedTags.size());

        assertEquals(3, getTagRowCount());
    }

    @Test
    public void addTagsOldTagsOnly() throws SQLException {
        assertEquals(3, getTagRowCount());

        Set<Tag> tags = new HashSet<>();
        tags.add(new Tag("sports"));
        tags.add(new Tag("#TravelEA"));

        Set<Tag> returnedTags = tagRepository.addTags(tags).join();

        assertEquals(2, returnedTags.size());
        assertTrue(returnedTags.contains(new Tag("sports")));
        assertTrue(returnedTags.contains(new Tag("#TravelEA")));

        assertEquals(3, getTagRowCount());
    }

    @Test
    public void addTagsNoTagData() throws SQLException {
        assertEquals(3, getTagRowCount());

        Set<Tag> tags = new HashSet<>();
        tags.add(new Tag("sports", 2L));
        tags.add(new Tag("Russia", 1L));
        tags.add(new Tag("#TravelEA", 99999L));

        Set<Tag> returnedTags = tagRepository.addTags(tags).join();

        assertEquals(3, returnedTags.size());
        assertTrue(returnedTags.contains(new Tag("sports")));
        assertTrue(returnedTags.contains(new Tag("Russia")));
        assertTrue(returnedTags.contains(new Tag("#TravelEA")));

        assertEquals(3, getTagRowCount());
        PreparedStatement checkStatement = connection
            .prepareStatement("SELECT * FROM Tag WHERE name = '#TravelEA';");

        ResultSet resultSet = checkStatement.executeQuery();

        int count = 0;

        while (resultSet.next()) {
            assertEquals("#TravelEA", resultSet.getString("name"));
            assertEquals(3, resultSet.getInt("id"));
            count++;
        }

        assertEquals(1, count);
    }

}