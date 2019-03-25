package repository;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.ebean.*;
import models.CountryDefinition;
import models.Nationality;
import models.Profile;
import models.TravellerTypeDefinition;
import play.db.ebean.EbeanConfig;
import play.libs.Json;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static play.mvc.Results.ok;

public class ProfileRepository {

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public ProfileRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    /**
     * Adds a new profile to the database
     *
     * This method should be simpler, but at the moment it can't be.
     * Because play is brain-dead, and is broken.
     * We have working bridging code, that play fails on, except when we run app as production.
     *
     * @param profile Profile to add
     * @return Ok on success
     */
    public CompletableFuture<Result> addProfile(Profile profile) {
        return supplyAsync(() -> {

            // Insert basic profile info
            SqlUpdate insert = Ebean.createSqlUpdate(
                    "INSERT INTO Profile " +
                    "(user_id, first_name, last_name, middle_name, date_of_birth, gender) " +
                    "VALUES (:userId, :firstName, :lastName, :middleName, :dateOfBirth, :gender");

            // Set parameters
            insert.setParameter("userId", profile.userId);
            insert.setParameter("firstName", profile.firstName);
            insert.setParameter("lastName", profile.lastName);
            insert.setParameter("middleName", profile.middleName);
            insert.setParameter("dateOfBirth", profile.dateOfBirth);
            insert.setParameter("gender", profile.gender);

            insert.execute();

            // Insert all lists
            ebeanServer.insertAll(profile.travellerTypes);
            ebeanServer.insertAll(profile.nationalities);
            ebeanServer.insertAll(profile.passports);

            return ok();
        }, executionContext);
    }

    /**
     * Gets the profile with some id from the database, or null if no such profile exists
     *
     * This method should be far, far simpler, but at the moment it can't be.
     * Because play is brain-dead, and is broken.
     * We have working bridging code, that play fails on, except when we run app as production.
     *
     * @param id Unique ID of profile (owning user's id) to retrieve
     * @return Profile object with given ID, or null if none found
     */
    public CompletableFuture<Profile> findID(Long id) {
        // Find the profile from the server
        RawSql rawSql = RawSqlBuilder.parse("SELECT user_id, first_name, last_name, middle_name, date_of_birth, gender " +
                "FROM Profile " +
                "WHERE user_id = :userId;").create();

        Query<Profile> query = Ebean.find(Profile.class);
        query.setRawSql(rawSql);
        query.setParameter("userId", id);

        Profile profile = query.findOne();

        // If profile equals null, return null
        if(profile == null) { return CompletableFuture.completedFuture(null); }

        // Get nationalities of the profile
        rawSql = RawSqlBuilder
                .parse("SELECT C.id, C.name FROM Nationality N JOIN CountryDefinition C ON N.country_id=C.id WHERE N.user_id = :userId;")
                .columnMapping("C.id", "id")
                .columnMapping("C.name", "name")
                .create();

        Query<CountryDefinition> query2 = Ebean.find(CountryDefinition.class);
        query2.setRawSql(rawSql);
        query2.setParameter("userId", id);

        List<CountryDefinition> nationalities = query2.findList();

        // Get passports of the profile
        rawSql = RawSqlBuilder
                .parse("SELECT C.id, C.name FROM Passport P JOIN CountryDefinition C ON P.country_id=C.id WHERE P.user_id = :userId;")
                .columnMapping("C.id", "id")
                .columnMapping("C.name", "name")
                .create();

        Query<CountryDefinition> query3 = Ebean.find(CountryDefinition.class);
        query3.setRawSql(rawSql);
        query3.setParameter("userId", id);

        List<CountryDefinition> passports = query3.findList();

        // Get travellers types of the profile
        rawSql = RawSqlBuilder
                .parse("SELECT TD.id, TD.description FROM TravellerType T JOIN TravellerTypeDefinition TD ON T.traveller_type_id=TD.id WHERE T.user_id = :userId;")
                .columnMapping("TD.id", "id")
                .columnMapping("TD.description", "description")
                .create();

        Query<TravellerTypeDefinition> query4 = Ebean.find(TravellerTypeDefinition.class);
        query4.setRawSql(rawSql);
        query4.setParameter("userId", id);

        List<TravellerTypeDefinition> travellerTypes = query4.findList();

        // Turning the object into a json object, and manually adding the fields is necessary because play
        // won't let me set a new value to them. that's right, trying to do the following gives null pointer exceptions:
        //      profile.nationalities = <whatever>
        ObjectNode node = Json.newObject();
        node.put("userId", profile.userId);
        node.put("firstName", profile.firstName);
        node.put("lastName", profile.lastName);
        node.put("middleName", profile.middleName);
        node.put("dateOfBirth", profile.dateOfBirth);
        node.put("gender", profile.gender);

        // Add lists to object
        ObjectMapper mapper = new ObjectMapper();
        node.putArray("nationalities").addAll((ArrayNode)mapper.valueToTree(nationalities));
        node.putArray("passports").addAll((ArrayNode)mapper.valueToTree(passports));
        node.putArray("travellerTypes").addAll((ArrayNode)mapper.valueToTree(travellerTypes));

        try {
            // Deserialize object into profile object and return
            return CompletableFuture.completedFuture(Json.fromJson(node, Profile.class));
        }
        catch (Exception e) {
            return null;
        }
    }


    public CompletableFuture<Profile> findIDModelBridging(Long id) {
        return supplyAsync(() ->
                ebeanServer.find(Profile.class)
                .where()
                .eq("user_id", id)
                .findOneOrEmpty()
                .orElse(null),
            executionContext);
    }

    /**
     * Deletes the profile having some ID if it exists, returns false if no profile with that id was found
     * @param id ID of profile to delete
     * @return True if a profile was found with the ID and then deleted
     */
    public CompletableFuture<Boolean> deleteProfile(Long id) {
        SqlUpdate delete = Ebean.createSqlUpdate(
                "DELETE FROM Nationality WHERE user_id=:userId;" +
                "DELETE FROM Passport WHERE user_id=:userId;" +
                "DELETE FROM TravellerType WHERE user_id=:userId;" +
                "DELETE FROM Profile WHERE user_id=:userId");
        delete.setParameter("userId", id);

        return CompletableFuture.completedFuture(delete.execute() > 0);
    }

    /**
     * Updates a profile on the database, ID must not have been changed though
     * @param profile New profile object
     * @return OK on success
     */
    public CompletableFuture<Result> updateProfile(Profile profile) {
        return supplyAsync(() -> {

            // Insert basic profile info
            SqlUpdate update = Ebean.createSqlUpdate(
                    "UPDATE Profile SET user_id=:userId, first_name=:firstName, last_name=:lastName, middle_name=:middleName, date_of_birth=:dateOfBirth, gender=:gender;");

            // Set parameters
            update.setParameter("userId", profile.userId);
            update.setParameter("firstName", profile.firstName);
            update.setParameter("lastName", profile.lastName);
            update.setParameter("middleName", profile.middleName);
            update.setParameter("dateOfBirth", profile.dateOfBirth);
            update.setParameter("gender", profile.gender);

            // Update profile information
            update.execute();

            // Delete all information regarding nationalities, types, passports
            SqlUpdate delete = Ebean.createSqlUpdate(
                "DELETE FROM Nationality WHERE user_id=:userId;" +
                "DELETE FROM Passport WHERE user_id=:userId;" +
                "DELETE FROM TravellerType WHERE user_id=:userId;");
            delete.setParameter("userId", profile.userId);

            // Now insert back into the foreign tables
            ebeanServer.insertAll(profile.travellerTypes);
            ebeanServer.insertAll(profile.nationalities);
            ebeanServer.insertAll(profile.passports);

            return ok();
        }, executionContext);
    }
}