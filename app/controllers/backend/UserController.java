package controllers.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.*;
import models.TravellerType.TravellerTypeKey;
import play.data.Form;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static java.lang.Math.max;

/**
 * Manage a database of users
 */
public class UserController extends Controller {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final CountryDefinitionRepository countryDefinitionRepository;
    private final NationalityRepository nationalityRepository;
    private final PassportRepository passportRepository;
    private final TravellerTypeRepository travellerTypeRepository;
    private final TravellerTypeDefinitionRepository travellerTypeDefinitionRepository;
    private final FormFactory formFactory;
    private final HttpExecutionContext httpExecutionContext;

    @Inject
    public UserController(FormFactory formFactory,
                          UserRepository userRepository,
                          ProfileRepository profileRepository,
                          CountryDefinitionRepository countryDefinitionRepository,
                          NationalityRepository nationalityRepository,
                          PassportRepository passportRepository,
                          HttpExecutionContext httpExecutionContext,
                          TravellerTypeRepository travellerTypeRepository,
                          TravellerTypeDefinitionRepository travellerTypeDefinitionRepository) {
        this.userRepository = userRepository;
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
     * Display the paginated list of users.
     *
     * @param page   Current page number (starts from 0)
     * @param order  Sort order (either asc or desc)
     * @param filter Filter applied on user names
     * @return Returns a CompletionStage ok type for successful query
     */
    public CompletionStage<Result> pagedUsers(int page, String order, String filter) {
        // Run a db operation in another thread (using DatabaseExecutionContext)
        return userRepository.page(page, 10, order, filter).thenApplyAsync(users ->
                ok(Json.toJson(users.getList())), httpExecutionContext.current());
    }

    /**
     * Delete a user with given uid
     *
     * @param uid ID of user to delete
     * @return Ok if user successfully deleted, badrequest if no such user found
     */
    public CompletionStage<Result> deleteUser(Long uid) {
        return userRepository.deleteUser(uid).thenApplyAsync(rowsDeleted ->
                        (rowsDeleted > 0) ? ok("Successfully deleted user with uid: " + uid) : badRequest("No user with such uid found"),
                httpExecutionContext.current());
    }


    /**
     * Method to handle adding a new user to the database. The username provided must be unique,
     * and the password field must be non-empty
     *
     * @param request The HTTP request sent, the body of this request should be a JSON User object
     * @return OK if user is successfully added to DB, badRequest otherwise
     */
    public CompletableFuture<Result> addNewUser(Http.Request request) {
        //Get the data from the request as a JSON object
        JsonNode data = request.body().asJson();

        //Sends the received data to the validator for checking
        ErrorResponse validatorResult = new UserValidator(data).login();

        //Checks if the validator found any errors in the data
        if (validatorResult.error()) {
            return CompletableFuture.supplyAsync(() -> badRequest(validatorResult.toJson()));
        }

        // Convert body to user object, and set uid to null to force new id to be generated
        User user = new User();
        user.username = data.get("username").asText();
        user.password = data.get("password").asText();
        user.uid = null;

        // Create salt to user, and hash password before storing in database
        user.salt = CryptoManager.generateNewSalt();
        user.password = CryptoManager.hashPassword(user.password, Base64.getDecoder().decode(user.salt));

        User foundUser;

        // Check if username taken
        try {
            foundUser = userRepository.findUserName(user.username).get();
        }
        catch (ExecutionException ex) {
            return CompletableFuture.supplyAsync(() -> {
                validatorResult.map("Database Exception", "other");
                return internalServerError(validatorResult.toJson());
            
            });
        }
        catch (InterruptedException ex) {
            return CompletableFuture.supplyAsync(() -> {
                validatorResult.map("Thread exception", "other");
                return internalServerError(validatorResult.toJson());
            });
        }

        // If username was already in use
        if (foundUser != null) {
            CompletableFuture.supplyAsync(() -> {
                validatorResult.map("Username already in use", "other");
                return badRequest(validatorResult.toJson());
            });
        }
        // Otherwise if username is free, add to database and return ok
        return userRepository.insertUser(user).thenApplyAsync(uid ->
                (uid != null) ? ok(Long.toString(uid)) : internalServerError());
        //else {
            //Else, no errors found, continue with adding to the database
            //Create a new user from the request data, basing off the User class
            //User newUser = Json.fromJson(data, User.class);
            //Generate a new salt for the new user
            //newUser.salt = CryptoManager.generateNewSalt();
            //Generate the salted password
            //newUser.password = CryptoManager.hashPassword(newUser.password, Base64.getDecoder().decode(newUser.salt));

            //This block ensures that the username (email) is not taken already, and returns a CompletableFuture<Result>
            //The chained thenComposes results in the last function's return value being the overall return value
            //return userRepository.findUserName(newUser.username)                //Check whether the username is already in the database
            //        .thenComposeAsync(user -> CompletableFuture.supplyAsync(() -> {  //Pass that result (a User object) into the new function using thenCompose
            //            if (user != null) return null;                          //If a user is found pass null into the next function using thenCompose
            //            else return userRepository.insertUser(newUser);         //If a user is not found pass the result of insertUser (a Long) ito the next function using thenCompose
            //        }))
            //        .thenComposeAsync(uid -> CompletableFuture.supplyAsync(() -> {   //Num should be a uid of a new user or null, the return of this lambda is the overall return of the whole method
            //            if (uid == null) {
            //                //Create the error to be sent to client
            //               validatorResult.map("Email already in use", "other");
            //              return badRequest(validatorResult.toJson());    //If the uid is null, return a badRequest message...
            //            }
            //            else return ok(Json.toJson(uid));                                           //If the uid is not null, return an ok message with the uid contained within
            //          })).thenApplyAsync(result -> result);
        //}
    }

    /**
     * Handles login attempts. A username and password must be provided as a JSON object,
     * by default this JSON object deserializes to a User object which is then compared against
     * the database to check for correct password and username etc.
     *
     * @param request HTTP request with username and password in body
     * @return OK with User JSON data on successful login, otherwise badRequest with specific error message
     */
    public CompletionStage<Result> login(Http.Request request) {
        // Get json parameters
        JsonNode json = request.body().asJson();

        // Run the Validator
        ErrorResponse errorResponse = new UserValidator(json).login();

        if (errorResponse.error()) {
            return CompletableFuture.supplyAsync(() -> badRequest(errorResponse.toJson()));
        }

        // Find user with username on database
        return userRepository.findUserName(json.get("username").asText("")).thenApplyAsync(foundUser -> {
            // If no such user was found with that username, return bad request
           if(foundUser == null) {
               errorResponse.map("Unauthorised", "other");
               return status(401,errorResponse.toJson());
           }
           // Otherwise if a user was found, check if correct password
           else {
               // Check if password given matches hashed and salted password on db
               if(CryptoManager.checkPasswordMatch(json.get("password").asText(""), foundUser.salt, foundUser.password)) {
                   // Redact password specific fields and return json user object
                   foundUser.password = null;
                   foundUser.salt = null;
                   return ok(Json.toJson(foundUser.uid));
               }
               // If password was incorrect, return bad request
               else {
                    errorResponse.map("Unauthorised", "other");
                    return status(401,errorResponse.toJson());
               }
           }
        });
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

        System.out.println(json);
        // Converts json to Profile object, sets uid to link profile and user
        Profile profile = new Profile();
        profile.uid = json.get("uid").asLong();
        profile.firstName = json.get("firstName").asText();
        profile.lastName = json.get("lastName").asText();
        profile.middleName = json.get("middleName").asText();
        profile.gender = json.get("gender").asText();
        profile.birthDate = json.get("birthDate").asText();

        // Converts users nationalities, passports and traveller types to arrays of strings
        String[] nationalityStrings = json.get("nationalities").asText().split(",");
        String[] passportStrings = json.get("passports").asText().split(",");
        String[] travellerTypeStrings = json.get("travellerTypes").asText().split(",");

        // Creates lists of nationality, passport and traveller type objects from CountryDefinition and TravellerTypeDefinition tables in database
        List<Nationality> nationalities;
        List<Passport> passports;
        List<TravellerType> travellerTypes;

        try {
            nationalities = getValidNationalities(nationalityStrings, profile.uid);
            passports = getValidPassports(passportStrings, profile.uid);
            travellerTypes = getValidTravellerTypes(travellerTypeStrings, profile.uid);
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
            foundProfile = profileRepository.findID(profile.uid).get();
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

        // If userId was already in use
        if (foundProfile != null) {
            return CompletableFuture.supplyAsync(() -> {
                errorResponse.map("Profile already created for this user id","other");
                return badRequest(errorResponse.toJson());
            });
        }
        // If userId is free, add to database along with nationalities and passports and return ok
        else {
            // TODO: Do these need to be .get(), also should these be a transaction?
            profileRepository.addProfile(profile);

            // Adds all of the users nationalities in the database
            for (Nationality nationality : nationalities) {
                nationalityRepository.insertNationality(nationality);
            }

            // Stores all the users passports in the database
            for (Passport passport : passports) {
                passportRepository.insertPassport(passport);
            }

            // Stores all the users traveller types in the database
            for (TravellerType travellerType : travellerTypes) {
                travellerTypeRepository.addTravellerTypeToProfile(travellerType);
            }

            return CompletableFuture.supplyAsync(() -> ok("Successfully added new profile to database"));
        }
    }

    /**
     * Gets a profile based on the userID specified in the request
     * @param uid The user ID to return data for
     * @return Ok with profile json object if profile found, badRequest if request malformed or profile not found
     */
    public CompletionStage<Result> getProfile(Long uid) {
        ErrorResponse errorResponse = new ErrorResponse();
        Profile profile;

        try {
            profile = profileRepository.findID(uid).get();
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
            // Creates lists for nationality, passport and traveller type objects to be stored in
            List<Nationality> nationalities;
            List<Passport> passports;
            List<TravellerType> travellerTypes;

            try {
                nationalities = nationalityRepository.getAllNationalitiesOfUser(profile.uid).get();
                passports = passportRepository.getAllPassportsOfUser(profile.uid).get();
                travellerTypes = travellerTypeRepository.getAllTravellerTypesFromProfile(profile.uid).get();
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

            // Creates nationality, passport and traveller type strings to send
            String nationalityString = "";
            String passportString = "";
            String travellerTypeString = "";

            try {
                if (nationalities != null) {
                    for (Nationality nationality : nationalities) {
                        CountryDefinition countryDefinition = countryDefinitionRepository.findCountryByID(nationality.countryId).get();

                        if (countryDefinition != null) {
                            nationalityString += countryDefinition.name + ",";
                        }
                    }

                    nationalityString = nationalityString.substring(0, max(0, nationalityString.length() - 1));
                }

                if (passports != null) {
                    for (Passport passport : passports) {
                        CountryDefinition countryDefinition = countryDefinitionRepository.findCountryByID(passport.countryId).get();

                        if (countryDefinition != null) {
                            passportString += countryDefinition.name + ",";
                        }
                    }

                    passportString = passportString.substring(0, max(0, passportString.length() - 1));
                }

                if (travellerTypes != null) {
                    for (TravellerType travellerType : travellerTypes) {
                        TravellerTypeDefinition travellerTypeDefinition = travellerTypeDefinitionRepository.getTravellerTypeDefinitionById(travellerType.key.travellerTypeId).get();

                        if (travellerTypeDefinition != null) {
                            travellerTypeString += travellerTypeDefinition.description + ",";
                        }
                    }

                    travellerTypeString = travellerTypeString.substring(0, max(0, travellerTypeString.length() - 1));
                }
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

            // Converts profile to json and adds nationality, passport and traveller type properties
            JsonNode profileJson = Json.toJson(profile);
            ObjectNode customProfileJson = (ObjectNode)profileJson;
            customProfileJson.put("nationalities", nationalityString);
            customProfileJson.put("passports", passportString);
            customProfileJson.put("travellerTypes", travellerTypeString);

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
    public CompletionStage<Result> updateProfile(Http.Request request, Long uid) {
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
            profile = profileRepository.findID(uid).get();
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
            profile.birthDate = json.get("birthDate").asText();

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
            List<TravellerType> travellerTypesToDelete;

            try {
                // Gets objects to add
                nationalities = getValidNationalities(nationalityStrings, profile.uid);
                passports = getValidPassports(passportStrings, profile.uid);
                travellerTypes = getValidTravellerTypes(travellerTypeStrings, profile.uid);

                // Gets objects to delete
                nationalitiesToDelete = nationalityRepository.getAllNationalitiesOfUser(profile.uid).get();
                passportsToDelete = passportRepository.getAllPassportsOfUser(profile.uid).get();
                travellerTypesToDelete = travellerTypeRepository.getAllTravellerTypesFromProfile(profile.uid).get();
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

            for (TravellerType travellerType : travellerTypesToDelete) {
                travellerTypeRepository.deleteTravellerTypeFromProfile(travellerType);
            }

            // Adds new nationality, passport and traveller type objects to the database
            for (Nationality nationality : nationalities) {
                nationalityRepository.insertNationality(nationality);
            }

            for (Passport passport : passports) {
                passportRepository.insertPassport(passport);
            }

            for (TravellerType travellerType : travellerTypes) {
                travellerTypeRepository.addTravellerTypeToProfile(travellerType);
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

    /**
     * Gets all profiles
     * @return A CompletableFuture of type result, which contains a json array containing all the profiles
     */
    public CompletableFuture<Result> getAllProfiles() {
        ErrorResponse errorResponse = new ErrorResponse();
        List<Profile> profiles;
        ArrayList<ObjectNode> profilesToSend = new ArrayList<>();

        try {
            profiles = profileRepository.getAllProfiles().get();

            for (Profile profile : profiles) {
                JsonNode profileJson = Json.toJson(profile);
                ObjectNode customProfileJson = (ObjectNode)profileJson;

                List<Nationality> nationalities = nationalityRepository.getAllNationalitiesOfUser(profile.uid).get();
                List<Passport> passports = passportRepository.getAllPassportsOfUser(profile.uid).get();
                List<TravellerType> travellerTypes = travellerTypeRepository.getAllTravellerTypesFromProfile(profile.uid).get();

                // Creates nationality, passport and traveller type strings to send
                String nationalityString = "";
                String passportString = "";
                String travellerTypeString = "";

                if (nationalities != null) {
                    for (Nationality nationality : nationalities) {
                        CountryDefinition countryDefinition = countryDefinitionRepository.findCountryByID(nationality.countryId).get();

                        if (countryDefinition != null) {
                            nationalityString += countryDefinition.name + ",";
                        }
                    }

                    nationalityString = nationalityString.substring(0, max(0, nationalityString.length() - 1));
                }

                if (passports != null) {
                    for (Passport passport : passports) {
                        CountryDefinition countryDefinition = countryDefinitionRepository.findCountryByID(passport.countryId).get();

                        if (countryDefinition != null) {
                            passportString += countryDefinition.name + ",";
                        }
                    }

                    passportString = passportString.substring(0, max(0, passportString.length() - 1));
                }

                if (travellerTypes != null) {
                    for (TravellerType travellerType : travellerTypes) {
                        TravellerTypeDefinition travellerTypeDefinition = travellerTypeDefinitionRepository.getTravellerTypeDefinitionById(travellerType.key.travellerTypeId).get();

                        if (travellerTypeDefinition != null) {
                            travellerTypeString += travellerTypeDefinition.description + ",";
                        }
                    }

                    travellerTypeString = travellerTypeString.substring(0, max(0, travellerTypeString.length() - 1));
                }

                customProfileJson.put("nationalities", nationalityString);
                customProfileJson.put("passports", passportString);
                customProfileJson.put("travellerTypes", travellerTypeString);

                profilesToSend.add(customProfileJson);
            }
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

        ObjectNode node = Json.newObject();
        ArrayNode arrayToSend = new ObjectMapper().valueToTree(profilesToSend);
        node.putArray("allProfiles").addAll(arrayToSend);
        return CompletableFuture.supplyAsync(() -> ok(node.get("allProfiles")));
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
                nationality.uid = userID;
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
            catch (ExecutionException | InterruptedException ex) {;
                throw ex;
            }

            if (foundCountry != null) {
                // Creates found passport object and stores in list
                Passport passport = new Passport();
                passport.uid = userID;
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
    private List<TravellerType> getValidTravellerTypes(String[] travellerTypeStrings, long userID) throws ExecutionException, InterruptedException {
        ArrayList<TravellerType> travellerTypes = new ArrayList<>();

        // Iterates through travellerTypeStrings array and checks database for matching traveller types, stores traveller type if found
        for (String travellerTypeString : travellerTypeStrings) {
            TravellerTypeDefinition foundTravellerType;

            try {
                foundTravellerType = travellerTypeDefinitionRepository.getTravellerTypeDefinitionByDescription(travellerTypeString).get();
            }
            catch (ExecutionException | InterruptedException ex) {
                throw ex;
            }

            if (foundTravellerType != null) {
                // Creates found travellerType object and stores in list
                TravellerType travellerType = new TravellerType();
                travellerType.key = new TravellerTypeKey(userID, foundTravellerType.id);
                travellerTypes.add(travellerType);
            }
        }

        return travellerTypes;
    }
}