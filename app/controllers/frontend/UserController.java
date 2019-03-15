package controllers.frontend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.ConfigException;
import controllers.routes;
import models.Profile;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSBodyReadables;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.people;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static play.libs.Scala.asScala;

@Singleton
public class UserController extends Controller {
    private WSClient ws;
    private HttpExecutionContext httpExecutionContext;

    @Inject
    public void UController(WSClient ws, HttpExecutionContext httpExecutionContext) {
        this.ws = ws;
        this.httpExecutionContext = httpExecutionContext;
    }

    /**
     * Displays the people page. Called with the /people URL and uses a GET request.
     * Checks that a user is logged in. Takes them to the people page if they are,
     * otherwise they are taken to the start page.
     *
     * @return displays the people or start page.
     */
    public CompletableFuture<Result> index(Http.Request request) {
        return request.session().getOptional("connected").map(user -> {
            return this.getProfiles().thenApplyAsync(
                    profileList -> {
                        return (profileList.size() >= 0) ? ok(people.render(asScala(profileList), user)) : internalServerError();    // TODO: Change if statement for error handling
                    },
                httpExecutionContext.current());
        }).orElseGet(() -> CompletableFuture.supplyAsync(() -> redirect(controllers.routes.StartController.index())));
    }

    private CompletableFuture<List<Profile>> getProfiles() {
        CompletableFuture<WSResponse> res = ws.url("http://localhost:9000/api/profile").get().toCompletableFuture();
        return res.thenApply(r -> {
            JsonNode json = r.getBody(WSBodyReadables.instance.json());
            try {
                return new ObjectMapper().readValue(new ObjectMapper().treeAsTokens(json),
                        new TypeReference<List<Profile>>() {
                        });
            } catch (Exception ex) {
                return new ArrayList<>();
            }
        });
    }
}
