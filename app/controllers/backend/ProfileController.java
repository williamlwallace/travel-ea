package controllers.backend;

import actions.ActionState;
import actions.Authenticator;
import actions.roles.Everyone;
import com.fasterxml.jackson.databind.JsonNode;
import io.ebean.PagedList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import models.CountryDefinition;
import models.Profile;
import models.TravellerTypeDefinition;
import models.User;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import play.routing.JavaScriptReverseRouter;
import repository.ProfileRepository;
import repository.TravellerTypeDefinitionRepository;
import util.objects.PagingResponse;
import util.validation.ErrorResponse;
import util.validation.UserValidator;

/**
 * Manage a database of users.
 */
public class ProfileController extends TEABackController {

    private static final String ERR_OTHER = "other";
    private final ProfileRepository profileRepository;
    private final TravellerTypeDefinitionRepository travellerTypeDefinitionRepository;

    @Inject
    public ProfileController(ProfileRepository profileRepository,
        TravellerTypeDefinitionRepository travellerTypeDefinitionRepository) {
        this.profileRepository = profileRepository;
        this.travellerTypeDefinitionRepository = travellerTypeDefinitionRepository;
    }

    /**
     * Gets all possible traveller types currently stored in db.
     *
     * @return JSON list of traveller types
     */
    public CompletableFuture<Result> getAllTravellerTypes() {
        return travellerTypeDefinitionRepository.getAllTravellerTypeDefinitions()
            .thenApplyAsync(allTravellerTypes -> {
                try {
                    return ok(sanitizeJson(Json.toJson(allTravellerTypes)));
                } catch (IOException e) {
                    return internalServerError(Json.toJson(SANITIZATION_ERROR));
                }
            });
    }

    /**
     * Adds a new profile received as body of a post request to database.
     *
     * @param request Contains the HTTP request info
     * @return Returns CompletableFuture type: ok if profile created and added successfully,
     * badRequest if profile already exists
     */
    public CompletableFuture<Result> addNewProfile(Http.Request request) {
        // Get json parameters
        JsonNode data = request.body().asJson();

        // Run the Validator
        ErrorResponse validatorResult = new UserValidator(data).profile();

        if (validatorResult.error()) {
            return CompletableFuture.supplyAsync(() -> badRequest(validatorResult.toJson()));
        } else {
            //We are assuming that the json is sent perfectly,
            // object may have to be hand-created later
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
                        validatorResult.map("Profile already created for this user", ERR_OTHER);
                        return badRequest(validatorResult.toJson());
                    } else {
                        return created(Json.toJson(insertedId));
                    }
                });
        }
    }


    /**
     * Gets a profile based on the userID specified in auth.
     *
     * @return Ok with profile json object if profile found, badRequest if request malformed or
     * profile not found
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> getMyProfile(Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        return getProfile(user.id);
    }


    /**
     * Gets a profile based on the userID specified in the request.
     *
     * @param userId The user ID to return data for
     * @return Ok with profile json object if profile found, badRequest if request malformed or
     * profile not found
     */
    public CompletableFuture<Result> getProfile(Long userId) {
        return profileRepository.findID(userId)
            .thenApplyAsync(profile -> {
                if (profile == null) {
                    ErrorResponse errorResponse = new ErrorResponse();
                    errorResponse.map("Profile for that user not found", ERR_OTHER);
                    return notFound(errorResponse.toJson());
                } else {
                    try {
                        return ok(sanitizeJson(Json.toJson(profile)));
                    } catch (IOException e) {
                        return internalServerError(Json.toJson(SANITIZATION_ERROR));
                    }
                }
            });
    }

    /**
     * Updates the profile received in the body of the request as well as the related nationalities,
     * passports and traveller types.
     *
     * @param request Contains the HTTP request info
     * @return Ok if updated successfully, badRequest if profile json malformed
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> updateProfile(Http.Request request, Long userId) {
        User user = request.attrs().get(ActionState.USER);
        if (userId.equals(user.id) || user.admin) {
            // Get json parameters
            JsonNode data = request.body().asJson();
            return updateProfileHelper(data, userId);
        } else {
            return CompletableFuture.supplyAsync(() -> forbidden(Json.toJson("Forbidden")));
        }
    }

    /**
     * Updates the profile received in the body of the request as well as the related nationalities,
     * passports and traveller types.
     *
     * @return Ok if updated successfully, badRequest if profile json malformed
     */
    private CompletableFuture<Result> updateProfileHelper(JsonNode data, Long userId) {
        // Run the Validator
        ErrorResponse errorResponse = new UserValidator(data).profile();

        if (errorResponse.error()) {
            return CompletableFuture.supplyAsync(() -> badRequest(errorResponse.toJson()));
        } else {
            return profileRepository.findID(userId)
                .thenComposeAsync(profile -> {
                    final Profile oldProfile = profile.copy();
                    if (profile == null) {
                        errorResponse.map("Profile for that user not found", ERR_OTHER);
                        return CompletableFuture.supplyAsync(() -> badRequest(errorResponse.toJson()));
                    } else {
                        Profile updatedProfile = Json.fromJson(data, Profile.class);
                        updatedProfile.userId = userId;

                        return profileRepository.updateProfile(updatedProfile)
                            .thenApplyAsync(updatedUserId -> {
                                return ok(Json.toJson(oldProfile));
                            });
                    }
                });     
        }
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
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> searchProfilesJson(Http.Request request,
        List<Long> nationalityIds,
        List<Long> travellerTypeIds,
        List<String> genders,
        Integer minAge,
        Integer maxAge,
        String searchQuery,
        String sortBy,
        Boolean ascending,
        Integer pageNum,
        Integer pageSize,
        Integer requestOrder) {

        User user = request.attrs().get(ActionState.USER);

        return profileRepository.getAllProfiles(user.id, nationalityIds, travellerTypeIds, genders,
            minAge, maxAge, searchQuery, sortBy, ascending, pageNum, pageSize)
            .thenApplyAsync(profiles -> {
                try {
                    return ok(sanitizeJson(Json.toJson(
                        new PagingResponse<>(profiles.getList(), requestOrder, profiles.getTotalPageCount())
                    )));
                } catch (IOException e) {
                    return internalServerError(Json.toJson("Failed to serialize response"));
                }
            });
    }

    /**
     * Lists routes to put in JS router for use from frontend.
     *
     * @return JSRouter Play result
     */
    public Result profileRoutes(Http.Request request) {
        return ok(
            JavaScriptReverseRouter.create("profileRouter", "jQuery.ajax", request.host(),
                controllers.backend.routes.javascript.ProfileController.getAllTravellerTypes(),
                controllers.backend.routes.javascript.ProfileController.searchProfilesJson(),
                controllers.backend.routes.javascript.ProfileController.getProfile(),
                controllers.frontend.routes.javascript.ProfileController.index()
            )
        ).as(Http.MimeTypes.JAVASCRIPT);
    }
}