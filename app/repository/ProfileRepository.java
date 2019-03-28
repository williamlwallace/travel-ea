package repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.ebean.*;
import models.*;
import models.dbOnly.Nationality;
import models.dbOnly.Passport;
import models.dbOnly.TravellerType;
import play.db.ebean.EbeanConfig;
import play.libs.Json;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
     * @param profile Profile to add
     * @return Ok on success
     */
    public CompletableFuture<Result> addProfile(Profile profile) {
        return supplyAsync(() -> {

            // Insert basic profile info
            ebeanServer.insert(new models.dbOnly.Profile(profile));

            // Insert all lists
            ebeanServer.insertAll(profile.getDBCompliantTravellerTypes());
            ebeanServer.insertAll(profile.getDBCompliantNationalities());
            ebeanServer.insertAll(profile.getDBCompliantPassports());

            return ok();
        }, executionContext);
    }

    /**
     * Gets the profile with some id from the database, or null if no such profile exists
     *
     * @param id Unique ID of profile (owning user's id) to retrieve
     * @return Profile object with given ID, or null if none found
     */
    public CompletableFuture<Profile> findID(Long id) {
        // Find the profile from the server, null if none found
        models.dbOnly.Profile dbProfile = ebeanServer.find(models.dbOnly.Profile.class)
                .where()
                .eq("user_id", id)
                .findOneOrEmpty()
                .orElse(null);

        // If profile was null, return null, otherwise get nationality, passport, and traveller type information
        if(dbProfile == null) {
            return CompletableFuture.completedFuture(null);
        }

        // Get nationality countries, running a sub-query to find the ids of countries to get based on this profiles nationality entries
        List<CountryDefinition> nationalityCountries = ebeanServer.find(CountryDefinition.class)
                .where()
                .in("id",
                    ebeanServer.find(Nationality.class)
                    .where()
                    .eq("user_id", id)
                    .findList().stream().map(n->n.countryId).collect(Collectors.toList()))
                .findList();

        // Get passport countries, running a sub-query to find the ids of countries to get based on this profiles nationality entries
        List<CountryDefinition> passportCountries = ebeanServer.find(CountryDefinition.class)
                .where()
                .in("id",
                    ebeanServer.find(Passport.class)
                    .where()
                    .eq("user_id", id)
                    .findList().stream().map(n->n.countryId).collect(Collectors.toList()))
                .findList();

        // Get traveller types, as above
        List<TravellerTypeDefinition> travellerTypeDefinitions = ebeanServer.find(TravellerTypeDefinition.class)
                .where()
                .in("id",
                    ebeanServer.find(TravellerType.class)
                    .where()
                    .eq("user_id", id)
                    .findList().stream().map(n->n.travellerTypeId).collect(Collectors.toList()))
                .findList();

        // Merge all found information into one object and return it
        return CompletableFuture.completedFuture(new Profile(dbProfile, nationalityCountries, passportCountries, travellerTypeDefinitions));
    }

    /**
     * Deletes the profile having some ID if it exists, returns false if no profile with that id was found
     * @param id ID of profile to delete
     * @return True if a profile was found with the ID and then deleted
     */
    public CompletableFuture<Boolean> deleteProfile(Long id) {
        return supplyAsync(() -> {
            // Delete all of the profiles nationalities, passports, traveller types
            deleteProfileNationalities(id);
            deleteProfilePassports(id);
            deleteProfileTravellerTypes(id);

            // Now delete profiles and return true if any were in fact deleted
            return (ebeanServer.find(models.dbOnly.Profile.class)
                    .where()
                    .eq("user_id", id)
                    .delete() > 0);

        }, executionContext);
    }

    public CompletableFuture<List<Profile>> getAllProfiles() {
        List<Profile> basicProfiles = ebeanServer.find(Profile.class).findList();
        List<Profile> filledInList = new ArrayList<>();
        return supplyAsync(() -> {
            for(Profile prof : basicProfiles) {
                findID(prof.userId).thenApply(result -> filledInList.add(result));
            }

            return filledInList;
        });
    }

    /**
     * Updates a profile on the database, ID must not have been changed though
     * @param profile New profile object
     * @return OK on success
     */
    public CompletableFuture<Result> updateProfile(Profile profile) {
        return supplyAsync(() -> {
            // Delete all of the profiles nationalities, passports, traveller types
            deleteProfileNationalities(profile.userId);
            deleteProfilePassports(profile.userId);
            deleteProfileTravellerTypes(profile.userId);

            // Now update the profile
            ebeanServer.update(new models.dbOnly.Profile(profile));

            // Insert all new nationalities, passports, traveller types
            ebeanServer.insertAll(profile.getDBCompliantTravellerTypes());
            ebeanServer.insertAll(profile.getDBCompliantNationalities());
            ebeanServer.insertAll(profile.getDBCompliantPassports());

            return ok();
        }, executionContext);
    }

    /**
     * Drops all nationalities of some profile, returning the number of rows that were deleted
     * @param id ID of profile to delete rows for
     * @return Number of rows deleted
     */
    private Integer deleteProfileNationalities(Long id) {
        // Delete nationalities
        return ebeanServer.find(Nationality.class)
                .where()
                .eq("user_id", id)
                .delete();
    }

    /**
     * Drops all passports of some profile, returning the number of rows that were deleted
     * @param id ID of profile to delete rows for
     * @return Number of rows deleted
     */
    private Integer deleteProfilePassports(Long id) {
        // Delete passports
        return ebeanServer.find(Passport.class)
                .where()
                .eq("user_id", id)
                .delete();
    }

    /**
     * Drops all travellerTypes of some profile, returning the number of rows that were deleted
     * @param id ID of profile to delete rows for
     * @return Number of rows deleted
     */
    private Integer deleteProfileTravellerTypes(Long id) {
        // Delete traveller types
        return ebeanServer.find(TravellerType.class)
                .where()
                .eq("user_id", id)
                .delete();
    }
}