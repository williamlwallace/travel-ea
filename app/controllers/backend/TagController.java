package controllers.backend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import models.Enums.TagType;
import play.api.libs.json.Json;
import play.mvc.Http;
import play.mvc.Result;
import repository.TagRepository;

public class TagController extends TEABackController {

    private final TagRepository tagRepository;

    @Inject
    public TagController(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    /**
     * TEST METHOD ONLY, NEEDS TO BE FIXED OR REMOVED
     * 
     * @param request
     * @param name
     * @return
     */
    public CompletableFuture<Result> getTags(Http.Request request, String name) {
        return tagRepository.searchTags(TagType.PHOTO_TAG, name).thenApplyAsync(tags ->
        {
            try {
                return ok(new ObjectMapper().writeValueAsString(tags));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return badRequest();
            }
        });

    }

}
