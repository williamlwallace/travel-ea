package controllers.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.*;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import repository.*;

import util.validation.UserValidator;
import util.validation.ErrorResponse;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static java.lang.Math.max;

/**
 * Manage a database of users
 */
public class ProfileController extends Controller {

    private final ProfileRepository profileRepository;

    @Inject
    public ProfileController(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    /**
     * Adds a new profile received as body of a post request to database
     *
     * @param request Contains the HTTP request info
     * @return        Returns CompletionStage type: ok if profile created and added succesfully, badRequest if profile
     *                already exists
     */
    public CompletableFuture<Result> addNewProfile(Http.Request request) {
        // Get json parameters
        JsonNode data = request.body().asJson();

        // Run the Validator
        ErrorResponse validatorResult = new UserValidator(data).profile();

        if (validatorResult.error()) {
            return CompletableFuture.supplyAsync(() -> badRequest(validatorResult.toJson()));
        } else {
            // Converts json to Profile object, sets uid to link profile and user
//        Profile profile = new Profile();
//        profile.userId = json.get("userId").asLong();
//        profile.firstName = json.get("firstName").asText();
//        profile.lastName = json.get("lastName").asText();
//        profile.middleName = json.get("middleName").asText();
//        profile.gender = json.get("gender").asText();
//        profile.dateOfBirth = json.get("dateOfBirth").asText();

            //{profile:

            // "travellerTypes": [{id: 1, string: "hello"}]

            //We are assuming that the json is sent perfectly, object may have to be hand-created later
            Profile newProfile = Json.fromJson(data, Profile.class);

            return profileRepository.findID(newProfile.userId)
                    .thenComposeAsync(userId -> {
                        if (userId != null) {   //If the profile for the user exists (is not null)
                            return null;
                        } else {                //Else insert the profile
                            return profileRepository.addProfile(newProfile);
                        }
                    })
                    .thenApplyAsync(insertedId -> {
                        if (insertedId == null) {
                            validatorResult.map("Profile already created for this user", "other");
                            return badRequest(validatorResult.toJson());
                        } else {
                            return ok(Json.toJson(insertedId));
                        }
                    });
        }
    }

    /**
     * Gets a profile based on the userID specified in the request
     * @param userId The user ID to return data for
     * @return Ok with profile json object if profile found, badRequest if request malformed or profile not found
     */
    public CompletableFuture<Result> getProfile(Long userId) {
        return profileRepository.findID(userId)
                .thenApplyAsync(profile -> {
                    if (profile == null) {
                        ErrorResponse errorResponse = new ErrorResponse();
                        errorResponse.map("Profile for that user not found", "other");
                        return badRequest(errorResponse.toJson());
                    } else {
//                        List<Long> travellerTypes = new ArrayList<>();
//                        List<Long> nationalities = new ArrayList<>();
//                        List<Long> passports = new ArrayList<>();
                        String travellerTypes = "";
                        String nationalities = "";
                        String passports = "";


                        for (TravellerTypeDefinition travellerType : profile.travellerTypes) {
//                            travellerTypes.add(travellerType.id);
                            travellerTypes += travellerType.id + ",";
                        }
                        travellerTypes = travellerTypes.substring(0, max(0, travellerTypes.length() - 1));

                        for (CountryDefinition country : profile.nationalities) {
//                            nationalities.add(country.id);
                            nationalities += country.id + ",";
                        }
                        nationalities = nationalities.substring(0, max(0, nationalities.length() - 1));

                        for (CountryDefinition country : profile.passports) {
//                            passports.add(country.id);
                            passports += country.id + ",";
                        }
                        passports = passports.substring(0, max(0, passports.length() - 1));

                        JsonNode profileJson = Json.toJson(profile);
                        ObjectNode customProfileJson = (ObjectNode)profileJson;

                        customProfileJson.put("travellerTypes", travellerTypes);
                        customProfileJson.put("nationalities", nationalities);
                        customProfileJson.put("passports", passports);

                        return ok(customProfileJson);
                    }
                });

    }

    /**
     * Updates the profile received in the body of the request as well as the related nationalities, passports
     * and traveller types
     * @param request Contains the HTTP request info
     * @return Ok if updated successfully, badRequest if profile json malformed
     */
    public CompletableFuture<Result> updateProfile(Http.Request request, Long userId) {
        // Get json parameters
        JsonNode data = request.body().asJson();

        // Run the Validator
        ErrorResponse errorResponse = new UserValidator(data).profile();

        if (errorResponse.error()) {
            return CompletableFuture.supplyAsync(() -> badRequest(errorResponse.toJson()));
        } else {
            return profileRepository.findID(userId)
                    .thenComposeAsync(profile -> {
                        if (profile == null) {
                            return null;
                        } else {
                            Profile updatedProfile = Json.fromJson(data, Profile.class);
                            updatedProfile.userId = userId;

                            return profileRepository.updateProfile(updatedProfile);
                        }
                    }).thenApplyAsync(updatedUserId -> {
                        if (updatedUserId == null) {
                            errorResponse.map("Profile for that user not found", "other");
                            return badRequest(errorResponse.toJson());
                        } else {
                            return ok(Json.toJson(updatedUserId));
                        }
                    });
        }
    }

    public CompletableFuture<Result> deleteProfile(Long id) {
        return profileRepository.deleteProfile(id).thenApplyAsync(rowsDeleted -> {
            if (rowsDeleted < 1) {
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.map("Profile not found for that user", "other");
                return badRequest(errorResponse.toJson());
            } else {
                return ok(Json.toJson(rowsDeleted));
            }
        });
    }
}