package controllers.backend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import models.Enums.TagType;
import play.mvc.Http;
import play.mvc.Result;
import repository.TagRepository;
import util.objects.PagePair;

public class TagController extends TEABackController {

    private final TagRepository tagRepository;

    @Inject
    public TagController(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    /**
     * Controller method to get all tags of a certain type, optional name parameter to search by name.
     * Name search is case insensitive
     * 
     * @param request HTTP request, needs to contain the tag type under tagType in  a JSON
     * @param name Optional sting by which to search for a name
     * @return Ok with a paginated list of tags as well as the total page count
     */
    public CompletableFuture<Result> getTags(Http.Request request, String name, Integer pageNum, Integer pageSize) {

        JsonNode data = request.body().asJson();
        ObjectMapper objectMapper = new ObjectMapper();
        TagType tagType = objectMapper.convertValue(data.get("tagType"), TagType.class);

        if (name != null) {
            return tagRepository.searchTags(tagType, name, pageNum, pageSize).thenApplyAsync(tags ->
            {
                try {
                    return ok(new ObjectMapper().writeValueAsString(new PagePair<Collection<?>, Integer>(tags.getList(), tags.getTotalPageCount())));
                } catch (JsonProcessingException e) {
                    return badRequest();
                }
            });
        } else {
            return tagRepository.searchTags(tagType, pageNum, pageSize)
                .thenApplyAsync(tags ->
                {
                    try {
                        return ok(new ObjectMapper().writeValueAsString(
                            new PagePair<Collection<?>, Integer>(tags.getList(),
                                tags.getTotalPageCount())));
                    } catch (JsonProcessingException e) {
                        return badRequest();
                    }
                });
        }
    }

    /**
     * Controller method to get all tags a user has used. Paginated and sorted by timeUsed
     *
     * @param request Contains the HTTP request info
     * @param userId ID of the user for whom to recieve the tags
     * @param pageNum page being requested
     * @param pageSize number of tags per page
     * @return OK with the data as the tags and totalPageCount
     */
    public CompletableFuture<Result> getUserTags(Http.Request request, Long userId, Integer pageNum, Integer pageSize) {
        return tagRepository.getRecentUserTags(userId, pageNum, pageSize).thenApplyAsync(tags ->
        {
            try {
                return ok(new ObjectMapper().writeValueAsString(new PagePair<Collection<?>, Integer>(tags.getList(), tags.getTotalPageCount())));
            } catch (JsonProcessingException e) {
                return badRequest();
            }
        });

    }

}
