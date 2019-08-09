package controllers.backend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import models.Enums.TagType;
import play.mvc.Http;
import play.mvc.Result;
import repository.TagRepository;
import util.objects.PagePair;
import util.objects.Pair;

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
    public CompletableFuture<Result> getTags(Http.Request request, Integer pageNum, Integer pageSize) {
        return tagRepository.searchTags(TagType.DESTINATION_TAG, pageNum, pageSize).thenApplyAsync(tags ->
        {
            try {
                return ok(new ObjectMapper().writeValueAsString(new PagePair<Collection<?>, Integer>(tags.getList(), tags.getTotalPageCount())));
            } catch (JsonProcessingException e) {
                return badRequest();
            }
        });

    }

}
