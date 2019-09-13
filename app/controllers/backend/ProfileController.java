package controllers.backend;

import actions.ActionState;
import actions.Authenticator;
import actions.roles.Everyone;
import akka.http.javadsl.model.HttpRequest;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import models.Profile;
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
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> addNewProfile(Http.Request request) {
        User user = request.attrs().get(ActionState.USER);

        // Get profile data from request body and perform validation
        JsonNode data = request.body().asJson();
        ErrorResponse validatorResult = new UserValidator(data).profile();

        if (validatorResult.error()) {
            return CompletableFuture.supplyAsync(() -> badRequest(validatorResult.toJson()));
        }
        else {
            Profile newProfile = Json.fromJson(data, Profile.class);

            if (!user.id.equals(newProfile.userId) && !user.admin) {
                return CompletableFuture.supplyAsync(() -> forbidden("You do not have permission to create a profile for this user"));
            }

            return profileRepository.findID(newProfile.userId).thenApplyAsync(profile -> {
                // If a profile already exists for this user
                if (profile != null) {
                    return badRequest(Json.toJson("A profile for this user already exists"));
                } else {
                    profileRepository.addProfile(newProfile);
                    return created(Json.toJson(newProfile.userId));
                }
            });
        }
    }

    /**
     * Gets a profile based on the userID specified in the request.
     *
     * @param userId The user ID to return data for
     * @return Ok with profile json object if profile found, badRequest if request malformed or
     * profile not found
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> getProfile(Long userId) {
        return profileRepository.findID(userId).thenComposeAsync(profile -> {
            if (profile == null) {
                return CompletableFuture.supplyAsync(() -> notFound(Json.toJson("Profile for that user not found")));
            }

            return profileRepository.getProfileFollowerCounts(profile.userId).thenApplyAsync(profileWithCounts -> {
                profile.followingUsersCount = profileWithCounts.followingUsersCount;
                profile.followerUsersCount = profileWithCounts.followerUsersCount;

                try {
                    return ok(sanitizeJson(Json.toJson(profile)));
                } catch (IOException e) {
                    return internalServerError(Json.toJson(SANITIZATION_ERROR));
                }
            });
        });
    }

    /**
     * Updates the profile received in the body of the request as well as the related nationalities,
     * passports and traveller types.
     *
     * @param request Contains the HTTP request info
     * @param userId ID of profile to update
     * @return Ok if updated successfully, badRequest if profile json malformed
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> updateProfile(Http.Request request, Long userId) {
        User user = request.attrs().get(ActionState.USER);

        // Retrieves the data from the request body and performs validation
        JsonNode data = request.body().asJson();
        ErrorResponse errorResponse = new UserValidator(data).profile();

        if (errorResponse.error()) {
            return CompletableFuture.supplyAsync(() -> badRequest(errorResponse.toJson()));
        } else {
            return profileRepository.findID(userId).thenComposeAsync(profile -> {
                if (profile == null) {
                    return CompletableFuture
                        .supplyAsync(() -> notFound(Json.toJson("This profile does not exist")));
                } else if (!userId.equals(user.id) && !user.admin) {
                    return CompletableFuture.supplyAsync(() -> forbidden(Json.toJson("Forbidden")));
                } else {
                    final Profile oldProfile = profile.copy();
                    Profile updatedProfile = Json.fromJson(data, Profile.class);
                    updatedProfile.userId = userId;

                    return profileRepository.updateProfile(updatedProfile)
                        .thenApplyAsync(updatedUserId -> ok(Json.toJson(oldProfile)));
                }
            });
        }
    }

    /**
     * Retrieves all profiles, filters them and then returns the filtered list of profiles.
     *
     * @param nationalityIds nationality request
     * @param genders gender requested
     * @param minAge minimum age for filter
     * @param maxAge maximum age for filter
     * @param travellerTypeIds traveller type requested
     * @param searchQuery The string to filter names by
     * @param sortBy What column to sort by
     * @param ascending Whether or not to sort ascendingly
     * @param pageNum Number of page we are currently showing
     * @param pageSize Number of results to show per page
     * @param requestOrder The order that this request has, allows frontend to determine what
     * results to take
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

        // Constrain sortBy to a set, default to creation date
        if (sortBy == null ||
            !Arrays.asList("user_id", "first_name", "middle_name", "last_name", "date_of_birth",
                "gender", "creation_date").contains(sortBy)) {
            sortBy = "creation_date";
        }

        return profileRepository.getAllProfiles(user.id, nationalityIds, travellerTypeIds, genders,
            minAge, maxAge, searchQuery, sortBy, ascending, pageNum, pageSize)
            .thenApplyAsync(profiles -> {
                try {
                    return ok(sanitizeJson(Json.toJson(
                        new PagingResponse<>(profiles.getList(), requestOrder,
                            profiles.getTotalPageCount())
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