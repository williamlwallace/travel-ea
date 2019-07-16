package controllers.backend;

import controllers.backend.routes.javascript;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.routing.JavaScriptReverseRouter;
import repository.CountryDefinitionRepository;

public class CountryController extends TEABackController{

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

    public Result getCountryById(Long id) {
        return ok();
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
                controllers.backend.routes.javascript.CountryController.getCountryById()
            )
        ).as(Http.MimeTypes.JAVASCRIPT);
    }
}
