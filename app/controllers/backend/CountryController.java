package controllers.backend;

import actions.Authenticator;
import actions.roles.Everyone;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import models.CountryDefinition;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import play.routing.JavaScriptReverseRouter;
import repository.CountryDefinitionRepository;
import util.validation.CountryValidator;
import util.validation.ErrorResponse;

public class CountryController extends TEABackController {

    private final CountryDefinitionRepository countryDefinitionRepository;

    @Inject
    public CountryController(CountryDefinitionRepository countryDefinitionRepository) {
        this.countryDefinitionRepository = countryDefinitionRepository;
    }

    /**
     * Gets all countries. Returns a json list of all countries.
     *
     * @return OK with list of countries
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> getAllCountries() {
        return countryDefinitionRepository.getAllCountries()
            .thenApplyAsync(allCountries -> {
                try {
                    return ok(sanitizeJson(Json.toJson(allCountries)));
                } catch (IOException e) {
                    return internalServerError(Json.toJson(SANITIZATION_ERROR));
                }
            });
    }

    /**
     * Gets a country by id
     *
     * @param id the id of the country to retrieve
     * @return The country if found, or an error if not
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> getCountryById(Long id) {
        return countryDefinitionRepository.findCountryByID(id).thenApplyAsync(country -> {
            if (country == null) {
                return notFound(Json.toJson(id));
            } else {
                try {
                    return ok(sanitizeJson(Json.toJson(country)));
                } catch (IOException e) {
                    return internalServerError(Json.toJson(SANITIZATION_ERROR));
                }
            }
        });
    }

    /**
     * Adds a country to the database
     *
     * @param request The request containing the country id and name
     * @return Ok with the country id if successful or an error if not
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> addCountry(Http.Request request) {
        JsonNode data = request.body().asJson();

        ErrorResponse validatorResult = new CountryValidator(data).validateCountry();
        if (validatorResult.error()) {
            return CompletableFuture.supplyAsync(() -> badRequest(validatorResult.toJson()));
        }
        CountryDefinition newCountry = Json.fromJson(data, CountryDefinition.class);

        return countryDefinitionRepository.findCountryByID(newCountry.id)
            .thenComposeAsync(country -> {
                if (country != null) {
                    return CompletableFuture
                        .supplyAsync(() -> badRequest(Json.toJson("Country already exists!")));
                } else {
                    return countryDefinitionRepository.insertCountryDefinition(newCountry)
                        .thenApplyAsync(id -> {
                            try {
                                return created(sanitizeJson(Json.toJson(id)));
                            } catch (IOException e) {
                                return internalServerError(Json.toJson(SANITIZATION_ERROR));
                            }
                        });
                }
            });
    }


    /**
     * Lists routes to put in JS router for use from frontend.
     *
     * @return JSRouter Play result
     */
    public Result countryRoutes(Http.Request request) {
        return ok(
            JavaScriptReverseRouter.create("countryRouter", "jQuery.ajax", request.host(),
                controllers.backend.routes.javascript.CountryController.getAllCountries(),
                controllers.backend.routes.javascript.CountryController.getCountryById(),
                controllers.backend.routes.javascript.CountryController.addCountry()
            )
        ).as(Http.MimeTypes.JAVASCRIPT);
    }
}
