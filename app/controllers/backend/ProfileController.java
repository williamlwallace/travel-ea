package controllers.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.*;
import play.data.FormFactory;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import repository.*;
import util.CryptoManager;
import util.validation.UserValidator;
import util.validation.ErrorResponse;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.*;

import static java.lang.Math.max;

/**
 * Manage a database of users
 */
public class ProfileController extends Controller {

    private final ProfileRepository profileRepository;
    private final CountryDefinitionRepository countryDefinitionRepository;
    private final NationalityRepository nationalityRepository;
    private final PassportRepository passportRepository;
    private final TravellerTypeRepository travellerTypeRepository;
    private final TravellerTypeDefinitionRepository travellerTypeDefinitionRepository;
    private final FormFactory formFactory;
    private final HttpExecutionContext httpExecutionContext;

    @Inject
    public ProfileController(FormFactory formFactory,
                          ProfileRepository profileRepository,
                          CountryDefinitionRepository countryDefinitionRepository,
                          NationalityRepository nationalityRepository,
                          PassportRepository passportRepository,
                          HttpExecutionContext httpExecutionContext,
                          TravellerTypeRepository travellerTypeRepository,
                          TravellerTypeDefinitionRepository travellerTypeDefinitionRepository) {
        this.profileRepository = profileRepository;
        this.countryDefinitionRepository = countryDefinitionRepository;
        this.nationalityRepository = nationalityRepository;
        this.passportRepository = passportRepository;
        this.formFactory = formFactory;
        this.httpExecutionContext = httpExecutionContext;
        this.travellerTypeRepository = travellerTypeRepository;
        this.travellerTypeDefinitionRepository = travellerTypeDefinitionRepository;
    }

    /**
     * Adds a new profile received as body of a post request to database
     *
     * @param request Contains the HTTP request info
     * @return        Returns CompletionStage type: ok if profile created and added succesfully, badRequest if profile
     *                already exists
     */
    public CompletionStage<Result> addNewProfile(Http.Request request) {
        // Get json parameters
        JsonNode json = request.body().asJson();

        // Run the Validator
        ErrorResponse errorResponse = new UserValidator(json).profile();

        if (errorResponse.error()) {
            return CompletableFuture.supplyAsync(() -> badRequest(errorResponse.toJson()));
        }

        // Converts json to Profile object, sets uid to link profile and user
        Profile profile = new Profile();
        profile.userId = json.get("userId").asLong();
        profile.firstName = json.get("firstName").asText();
        profile.lastName = json.get("lastName").asText();
        profile.middleName = json.get("middleName").asText();
        profile.gender = json.get("gender").asText();
        profile.dateOfBirth = json.get("dateOfBirth").asText();

        // Converts users nationalities, passports and traveller types to arrays of strings
        String[] nationalityStrings = json.get("nationalities").asText().split(",");
        String[] passportStrings = json.get("passports").asText().split(",");
        String[] travellerTypeStrings = json.get("travellerTypes").asText().split(",");

        // Creates lists of nationality, passport and traveller type objects from CountryDefinition and TravellerTypeDefinition tables in database
        List<Nationality> nationalities;
        List<Passport> passports;

        try {
            nationalities = getValidNationalities(nationalityStrings, profile.userId);
            passports = getValidPassports(passportStrings, profile.userId);
            profile.travellerTypes = getValidTravellerTypes(travellerTypeStrings, profile.userId);
        }
        catch (ExecutionException ex) {
            return CompletableFuture.supplyAsync(() -> {
                errorResponse.map("Database Exception", "other");
                return internalServerError(errorResponse.toJson());
            
            });
        }
        catch (InterruptedException ex) {
            return CompletableFuture.supplyAsync(() -> {
                errorResponse.map("Thread exception", "other");
                return internalServerError(errorResponse.toJson());
            });
        }

        Profile foundProfile;

        // Check if profile already exists
        try {
            foundProfile = profileRepository.findID(profile.userId).get();
        }
        catch (ExecutionException ex) {
            return CompletableFuture.supplyAsync(() -> {
                errorResponse.map("Databse Exception", "other");
                return internalServerError(errorResponse.toJson());
            
            });
        }
        catch (InterruptedException ex) {
            return CompletableFuture.supplyAsync(() -> {
                errorResponse.map("Thread exception", "other");
                return internalServerError(errorResponse.toJson());
            });
        }

        // If userId was already in use
        if (foundProfile != null) {
            return CompletableFuture.supplyAsync(() -> {
                errorResponse.map("Profile already created for this user id","other");
                return badRequest(errorResponse.toJson());
            });
        }
        // If userId is free, add to database along with nationalities and passports and return ok
        else {
            try {
                profileRepository.addProfile(profile).get();
            } catch (Exception e) {
                return CompletableFuture.supplyAsync(() -> internalServerError("Failed to add profile to database"));
            }

            // Adds all of the users nationalities in the database
            for (Nationality nationality : nationalities) {
                nationalityRepository.insertNationality(nationality);
            }

            // Stores all the users passports in the database
            for (Passport passport : passports) {
                passportRepository.insertPassport(passport);
            }

            return CompletableFuture.supplyAsync(() -> ok("Successfully added new profile to database"));
        }
    }

    /**
     * Gets a profile based on the userID specified in the request
     * @param userId The user ID to return data for
     * @return Ok with profile json object if profile found, badRequest if request malformed or profile not found
     */
    public CompletionStage<Result> getProfile(Long userId) {
        ErrorResponse errorResponse = new ErrorResponse();
        Profile profile;
        try {
            profile = profileRepository.findID(userId).get();
            //profile = profileRepository.findIDModelBridging(userId).get();
        }
        catch (ExecutionException ex) {
            return CompletableFuture.supplyAsync(() -> {
                errorResponse.map("Database Exception", "other");
                return internalServerError(errorResponse.toJson());
            
            });
        }
        catch (InterruptedException ex) {
            return CompletableFuture.supplyAsync(() -> {
                errorResponse.map("Thread exception", "other");
                return internalServerError(errorResponse.toJson());
            });
        }

        if (profile != null) {
            // Converts profile to json and adds nationality, passport and traveller type properties
            JsonNode profileJson = Json.toJson(profile);
            ObjectNode customProfileJson = (ObjectNode)profileJson;

            return CompletableFuture.supplyAsync(() -> ok(customProfileJson));
        }
        else {
            return CompletableFuture.supplyAsync(() -> {
                errorResponse.map("Could not find profile in database", "other");
                return badRequest(errorResponse.toJson());
            });
        }
    }

    /**
     * Updates the profile received in the body of the request as well as the related nationalities, passports
     * and traveller types
     * @param request Contains the HTTP request info
     * @return Ok if updated successfully, badRequest if profile json malformed
     */
    public CompletionStage<Result> updateProfile(Http.Request request, Long userId) {
        // Get json parameters
        JsonNode json = request.body().asJson();

        // Run the Validator
        ErrorResponse errorResponse = new UserValidator(json).profile();

        if (errorResponse.error()) {
            return CompletableFuture.supplyAsync(() -> badRequest(errorResponse.toJson()));
        }

        // Gets current profile from database
        Profile profile;

        try {
            profile = profileRepository.findID(userId).get();
        }
        catch (ExecutionException ex) {
            return CompletableFuture.supplyAsync(() -> {
                errorResponse.map("Database Exception", "other");
                return internalServerError(errorResponse.toJson());
            
            });
        }
        catch (InterruptedException ex) {
            return CompletableFuture.supplyAsync(() -> {
                errorResponse.map("Thread exception", "other");
                return internalServerError(errorResponse.toJson());
            });
        }

        if (profile != null) {
            // Updates all profile fields
            profile.firstName = json.get("firstName").asText();
            profile.lastName = json.get("lastName").asText();
            profile.middleName = json.get("middleName").asText();
            profile.gender = json.get("gender").asText();
            profile.dateOfBirth = json.get("dateOfBirth").asText();

            // Converts users nationalities, passports and traveller types to arrays of strings
            String[] nationalityStrings = json.get("nationalities").asText().split(",");
            String[] passportStrings = json.get("passports").asText().split(",");
            String[] travellerTypeStrings = json.get("travellerTypes").asText().split(",");

            // Creates lists of nationality, passport and traveller type objects from CountryDefinition and TravellerTypeDefinition tables in database
            List<Nationality> nationalities;
            List<Passport> passports;
            List<TravellerType> travellerTypes;

            // Creates lists of nationality, passport and traveller type objects to be deleted from database
            List<Nationality> nationalitiesToDelete;
            List<Passport> passportsToDelete;

            try {
                // Gets objects to add
                nationalities = getValidNationalities(nationalityStrings, profile.userId);
                passports = getValidPassports(passportStrings, profile.userId);
                profile.travellerTypes = getValidTravellerTypes(travellerTypeStrings, profile.userId);

                // Gets objects to delete
                nationalitiesToDelete = nationalityRepository.getAllNationalitiesOfUser(profile.userId).get();
                passportsToDelete = passportRepository.getAllPassportsOfUser(profile.userId).get();
            }
            catch (ExecutionException ex) {
                return CompletableFuture.supplyAsync(() -> {
                    errorResponse.map("Database Exception", "other");
                    return internalServerError(errorResponse.toJson());
                
                });
            }
            catch (InterruptedException ex) {
                return CompletableFuture.supplyAsync(() -> {
                    errorResponse.map("Thread exception", "other");
                    return internalServerError(errorResponse.toJson());
                });
            }

            // Updates profile in database
            profileRepository.updateProfile(profile);

            // Deletes old nationality, passport and traveller type objects from the database
            for (Nationality nationality : nationalitiesToDelete) {
                nationalityRepository.deleteNationality(nationality);
            }

            for (Passport passport : passportsToDelete) {
                passportRepository.deletePassport(passport);
            }

            // Adds new nationality, passport and traveller type objects to the database
            for (Nationality nationality : nationalities) {
                nationalityRepository.insertNationality(nationality);
            }

            for (Passport passport : passports) {
                passportRepository.insertPassport(passport);
            }


            return CompletableFuture.supplyAsync(() -> ok("Successfully updated profile in database"));
        }
        else {
            return CompletableFuture.supplyAsync(() -> {
                errorResponse.map("Could not find profile to update", "other");
                 return badRequest(errorResponse.toJson());
            });
        }
    }

    public CompletableFuture<Result> deleteProfile(Long id) {
        return profileRepository.deleteProfile(id).thenApplyAsync(rowsDeleted ->
            (!rowsDeleted) ? badRequest(Json.toJson("No such Profile")) : ok(Json.toJson("Profile Deleted")));
    }

    // Private Methods

    /**
     * Retrieves IDs from country definition table and creates nationality objects to be stored in nationality table
     * @param nationalityStrings Array of country names from which IDs will be retrieved from database
     * @param userID Id of the profile which the nationality objects created will use
     * @return List of Nationality objects to be inserted into database
     * @throws ExecutionException Thrown when database cannot be accessed
     * @throws InterruptedException Thrown when connection with database interrupted
     */
    private List<Nationality> getValidNationalities(String[] nationalityStrings, long userID) throws ExecutionException, InterruptedException {
        ArrayList<Nationality> nationalities = new ArrayList<>();

        // Iterates through nationality array and checks database for matching country,
        // if found checks nationality table for matching entry, if not found attempts to insert new user nationality
        for (String nationalityString : nationalityStrings) {
            CountryDefinition foundCountry;

            try {
                foundCountry = countryDefinitionRepository.findCountryByExactName(nationalityString).get();
            }
            catch (ExecutionException | InterruptedException ex) {
                throw ex;
            }

            // If country found in database
            if (foundCountry != null) {
                // Creates found nationality object and stores in list
                Nationality nationality = new Nationality();
                nationality.userId = userID;
                nationality.countryId = foundCountry.id;

                nationalities.add(nationality);
            }
        }

        return nationalities;
    }

    /**
     * Retrieves IDs from country definition table and creates passport objects to be stored in passport table
     * @param passportStrings Array of country names from which IDs will be retrieved from database
     * @param userID Id of the profile which the passport objects created will use
     * @return List of Passport objects to be inserted into database
     * @throws ExecutionException Thrown when database cannot be accessed
     * @throws InterruptedException Thrown when connection with database interrupted
     */
    private List<Passport> getValidPassports(String[] passportStrings, long userID) throws ExecutionException, InterruptedException {
        ArrayList<Passport> passports = new ArrayList<>();

        // Iterates through passportStrings array and checks database for matching country, stores country if found
        for (String passportString : passportStrings) {
            CountryDefinition foundCountry;

            try {
                foundCountry = countryDefinitionRepository.findCountryByExactName(passportString).get();
            }
            catch (ExecutionException | InterruptedException ex) {
                throw ex;
            }

            if (foundCountry != null) {
                // Creates found passport object and stores in list
                Passport passport = new Passport();
                passport.userId = userID;
                passport.countryId = foundCountry.id;

                passports.add(passport);
            }
        }

        return passports;
    }

    /**
     * Retrieves IDs from traveller type definition table and creates traveller type objects to be stored in traveller type table
     * @param travellerTypeStrings Array of traveller type descriptions from which IDs will be retrieved from database
     * @param userID Id of the profile which the traveller type objects created will use
     * @return List of TravellerType objects to be inserted into database
     * @throws ExecutionException Thrown when database cannot be accessed
     * @throws InterruptedException Thrown when connection with database interrupted
     */
    private List<TravellerTypeDefinition> getValidTravellerTypes(String[] travellerTypeStrings, long userID) throws ExecutionException, InterruptedException {
        ArrayList<TravellerTypeDefinition> travellerTypes = new ArrayList<>();

        // Iterates through travellerTypeStrings array and checks database for matching traveller types, stores traveller type if found
        for (String travellerTypeString : travellerTypeStrings) {
            TravellerTypeDefinition foundTravellerType;
            foundTravellerType = travellerTypeDefinitionRepository.getTravellerTypeDefinitionByDescription(travellerTypeString).get();
            if (foundTravellerType != null) {
                travellerTypes.add(foundTravellerType);
            }
        }

        return travellerTypes;
    }
}