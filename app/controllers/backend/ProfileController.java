package controllers.backend;

import actions.ActionState;
import actions.Authenticator;
import actions.roles.Everyone;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import models.CountryDefinition;
import models.Nationality;
import models.Passport;
import models.Profile;
import models.TravellerTypeDefinition;
import models.User;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import repository.CountryDefinitionRepository;
import repository.ProfileRepository;
import repository.TravellerTypeDefinitionRepository;
import util.validation.ErrorResponse;
import util.validation.UserValidator;

/**
 * Manage a database of users
 */
public class ProfileController extends Controller {

    private final ProfileRepository profileRepository;
    private final CountryDefinitionRepository countryDefinitionRepository;
    private final TravellerTypeDefinitionRepository travellerTypeDefinitionRepository;

    @Inject
    public ProfileController(ProfileRepository profileRepository,
        CountryDefinitionRepository countryDefinitionRepository,
        TravellerTypeDefinitionRepository travellerTypeDefinitionRepository) {
        this.profileRepository = profileRepository;
        this.countryDefinitionRepository = countryDefinitionRepository;
        this.travellerTypeDefinitionRepository = travellerTypeDefinitionRepository;
    }

    /**
     * Gets all possible traveller types currently stored in db
     *
     * @return JSON list of traveller types
     */
    public CompletableFuture<Result> getAllTravellerTypes() {
        return travellerTypeDefinitionRepository.getAllTravellerTypeDefinitions()
            .thenApplyAsync(allTravellerTypes -> ok(Json.toJson(allTravellerTypes)));
    }

    /**
     * Adds a new profile received as body of a post request to database
     *
     * @param request Contains the HTTP request info
     * @return Returns CompletionStage type: ok if profile created and added succesfully, badRequest
     * if profile already exists
     */
    public CompletableFuture<Result> addNewProfile(Http.Request request) {
        // Get json parameters
        JsonNode json = request.body().asJson();

        // Run the Validator
        ErrorResponse errorResponse = new UserValidator(json).profile();

        if (errorResponse.error()) {
            return CompletableFuture.supplyAsync(() -> badRequest(errorResponse.toJson()));
        }

        // Converts json to Profile object, sets uid to link profile and user
        Profile profile = Json.fromJson(json, Profile.class);

        Profile foundProfile;

        // Check if profile already exists
        try {
            foundProfile = profileRepository.findID(profile.userId).get();
        } catch (ExecutionException ex) {
            return CompletableFuture.supplyAsync(() -> {
                errorResponse.map("Database Exception", "other");
                return internalServerError(errorResponse.toJson());

            });
        } catch (InterruptedException ex) {
            return CompletableFuture.supplyAsync(() -> {
                errorResponse.map("Thread exception", "other");
                return internalServerError(errorResponse.toJson());
            });
        }

        // If userId was already in use
        if (foundProfile != null) {
            return CompletableFuture.supplyAsync(() -> {
                errorResponse.map("Profile already created for this user id", "other");
                return status(409, errorResponse.toJson());
            });
        }

        // If userId is free, add to database
        else {
            try {
                profileRepository.addProfile(profile).get();
                return CompletableFuture.supplyAsync(
                    () -> created(Json.toJson("Successfully added new profile to database")));
            } catch (Exception e) {
                return CompletableFuture
                    .supplyAsync(() -> internalServerError("Failed to add profile to database"));
            }
        }
    }

    /**
     * Gets a profile based on the userID specified in auth
     *
     * @return Ok with profile json object if profile found, badRequest if request malformed or
     * profile not found
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> getMyProfile(Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        return this.getProfile(request, user.id);
    }

    /**
     * Gets a profile based on the userID specified in the request
     *
     * @param userId The user ID to return data for
     * @return Ok with profile json object if profile found, badRequest if request malformed or
     * profile not found
     */
    public CompletableFuture<Result> getProfile(Http.Request request, Long userId) {
        ErrorResponse errorResponse = new ErrorResponse();
        Profile profile;
        try {
            profile = profileRepository.findID(userId).get();
        } catch (ExecutionException ex) {
            return CompletableFuture.supplyAsync(() -> {
                errorResponse.map("Database Exception", "other");
                return internalServerError(errorResponse.toJson());

            });
        } catch (InterruptedException ex) {
            return CompletableFuture.supplyAsync(() -> {
                errorResponse.map("Thread exception", "other");
                return internalServerError(errorResponse.toJson());
            });
        }

        if (profile != null) {
            // Converts profile to json and adds nationality, passport and traveller type properties
            JsonNode profileJson = Json.toJson(profile);
            ObjectNode customProfileJson = (ObjectNode) profileJson;

            return CompletableFuture.supplyAsync(() -> ok(customProfileJson));
        } else {
            return CompletableFuture.supplyAsync(() -> {
                errorResponse.map("Could not find profile in database", "other");
                return notFound(errorResponse.toJson());
            });
        }
    }

    /**
     * Updates the profile received in the body of the request as well as the related nationalities,
     * passports and traveller types
     *
     * @param request Contains the HTTP request info
     * @return Ok if updated successfully, badRequest if profile json malformed
     */
    @With({Everyone.class, Authenticator.class})
    public CompletionStage<Result> updateProfile(Http.Request request) {
        //Get user
        User user = request.attrs().get(ActionState.USER);
        // Get json parameters
        JsonNode json = request.body().asJson();
        return updateProfileHelper(json, user.id);
    }

    /**
     * Updates the profile received in the body of the request as well as the related nationalities,
     * passports and traveller types
     *
     * @param request Contains the HTTP request info
     * @return Ok if updated successfully, badRequest if profile json malformed
     */
    // TODO: set auth to admin role @With({Everyone.class, Authenticator.class})
    public CompletionStage<Result> updateProfile(Http.Request request, Long userId) {
        // Get json parameters
        JsonNode json = request.body().asJson();
        return updateProfileHelper(json, userId);
    }

    /**
     * Updates the profile received in the body of the request as well as the related nationalities,
     * passports and traveller types
     *
     * @return Ok if updated successfully, badRequest if profile json malformed
     */
    private CompletionStage<Result> updateProfileHelper(JsonNode json, Long userId) {
        // Run the Validator
        ErrorResponse errorResponse = new UserValidator(json).profile();
        if (errorResponse.error()) {
            return CompletableFuture.supplyAsync(() -> badRequest(errorResponse.toJson()));
        }

        // Convert json to profile
        Profile profile = Json.fromJson(json, Profile.class);

        if (profile != null) {
            // Updates profile in database
            profile.userId = userId;
            profileRepository.updateProfile(profile);

            return CompletableFuture
                .supplyAsync(() -> ok(Json.toJson("Successfully updated profile in database")));
        } else {
            return CompletableFuture.supplyAsync(() -> {
                errorResponse.map("Could not find profile to update", "other");
                return badRequest(errorResponse.toJson());
            });
        }
    }

    //TODO: Authorization for adminonly
    public CompletableFuture<Result> deleteProfile(Long id) {
        return profileRepository.deleteProfile(id).thenApplyAsync(rowsDeleted ->
            (!rowsDeleted) ? notFound(Json.toJson("No such Profile"))
                : ok(Json.toJson("Profile Deleted"))
        );
    }

    // Private Methods

    /**
     * Retrieves IDs from country definition table and creates nationality objects to be stored in
     * nationality table
     *
     * @param nationalityStrings Array of country names from which IDs will be retrieved from
     * database
     * @param userID Id of the profile which the nationality objects created will use
     * @return List of Nationality objects to be inserted into database
     * @throws ExecutionException Thrown when database cannot be accessed
     * @throws InterruptedException Thrown when connection with database interrupted
     */
    private List<Nationality> getValidNationalities(String[] nationalityStrings, long userID)
        throws ExecutionException, InterruptedException {
        ArrayList<Nationality> nationalities = new ArrayList<>();

        // Iterates through nationality array and checks database for matching country,
        // if found checks nationality table for matching entry, if not found attempts to insert new user nationality
        for (String nationalityString : nationalityStrings) {
            CountryDefinition foundCountry;

            try {
                foundCountry = countryDefinitionRepository.findCountryByExactName(nationalityString)
                    .get();
            } catch (ExecutionException | InterruptedException ex) {
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
     * Retrieves IDs from country definition table and creates passport objects to be stored in
     * passport table
     *
     * @param passportStrings Array of country names from which IDs will be retrieved from database
     * @param userID Id of the profile which the passport objects created will use
     * @return List of Passport objects to be inserted into database
     * @throws ExecutionException Thrown when database cannot be accessed
     * @throws InterruptedException Thrown when connection with database interrupted
     */
    private List<Passport> getValidPassports(String[] passportStrings, long userID)
        throws ExecutionException, InterruptedException {
        ArrayList<Passport> passports = new ArrayList<>();

        // Iterates through passportStrings array and checks database for matching country, stores country if found
        for (String passportString : passportStrings) {
            CountryDefinition foundCountry;

            try {
                foundCountry = countryDefinitionRepository.findCountryByExactName(passportString)
                    .get();
            } catch (ExecutionException | InterruptedException ex) {
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
     * Retrieves IDs from traveller type definition table and creates traveller type objects to be
     * stored in traveller type table
     *
     * @param travellerTypeStrings Array of traveller type descriptions from which IDs will be
     * retrieved from database
     * @param userID Id of the profile which the traveller type objects created will use
     * @return List of TravellerType objects to be inserted into database
     * @throws ExecutionException Thrown when database cannot be accessed
     * @throws InterruptedException Thrown when connection with database interrupted
     */
    private List<TravellerTypeDefinition> getValidTravellerTypes(String[] travellerTypeStrings,
        long userID) throws ExecutionException, InterruptedException {
        ArrayList<TravellerTypeDefinition> travellerTypes = new ArrayList<>();

        // Iterates through travellerTypeStrings array and checks database for matching traveller types, stores traveller type if found
        for (String travellerTypeString : travellerTypeStrings) {
            TravellerTypeDefinition foundTravellerType;
            foundTravellerType = travellerTypeDefinitionRepository
                .getTravellerTypeDefinitionByDescription(travellerTypeString).get();
            if (foundTravellerType != null) {
                travellerTypes.add(foundTravellerType);
            }
        }

        return travellerTypes;
    }

    /**
     * Retrieves all profiles, filters them and then returns the filtered list of profiles.
     *
     * @param nationalityId nationality request
     * @param gender gender requested
     * @param minAge minimum age for filter
     * @param maxAge maximum age for filter
     * @param travellerTypeId traveller type requested
     * @return List of profiles within requested parameters
     */
    public CompletableFuture<List<Profile>> searchProfiles(Long nationalityId, String gender,
        int minAge, int maxAge, Long travellerTypeId) {
        return profileRepository.getAllProfiles().thenApplyAsync(profiles -> {

            List<Profile> toReturn = new ArrayList<>(profiles);

            for (Profile profile : profiles) {
                if (gender != null) {
                    if (!profile.gender.equalsIgnoreCase(gender)) {
                        toReturn.remove(profile);
                        continue;
                    }
                }

//                LocalDate birthDate = LocalDate.parse(profile.dateOfBirth, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
//                int age = Period.between(birthDate, LocalDate.now()).getYears();
                int age = profile.calculateAge();

                if (age < minAge || age > maxAge) {
                    toReturn.remove(profile);
                    continue;
                }

                if (nationalityId != 0) {
                    boolean found = false;
                    for (CountryDefinition country : profile.nationalities) {
                        if (country.id.equals(nationalityId)) {
                            found = true;
                        }
                    }
                    if (!found) {
                        toReturn.remove(profile);
                        continue;
                    }
                }

                if (travellerTypeId != 0) {
                    boolean found = false;
                    for (TravellerTypeDefinition travellerTypeDefinition : profile.travellerTypes) {
                        if (travellerTypeDefinition.id.equals(travellerTypeId)) {
                            found = true;
                        }
                    }
                    if (!found) {
                        toReturn.remove(profile);
                    }
                }

            }

            return toReturn;
        });
    }

    /**
     * Retrieves all profiles, filters them and returns the result inside a Result object as JSON
     *
     * @param nationalityId nationality request
     * @param gender gender requested
     * @param minAge minimum age for filter
     * @param maxAge maximum age for filter
     * @param travellerTypeId traveller type requested
     * @return A ok result containing the JSON of the profiles matching search criteria
     */
    public CompletableFuture<Result> searchProfilesJson(Long nationalityId, String gender,
        int minAge, int maxAge, Long travellerTypeId) {
        return searchProfiles(nationalityId, gender, minAge, maxAge, travellerTypeId)
            .thenApplyAsync(profiles ->
                ok(Json.toJson(profiles)));
    }
}


