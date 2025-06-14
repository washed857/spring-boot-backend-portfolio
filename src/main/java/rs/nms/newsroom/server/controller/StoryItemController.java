package rs.nms.newsroom.server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import rs.nms.newsroom.server.dto.StoryItemDTOs;
import rs.nms.newsroom.server.dto.StoryItemSearchCriteria;
import rs.nms.newsroom.server.service.StoryItemService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;

import java.util.List;

/**
 * REST controller for granular management of story items within a newsroom workflow.
 * <p>
 * Provides endpoints for creation, retrieval, update, advanced search, and deletion
 * of individual rundown/script items. Designed for integration with larger story and rundown APIs.
 * </p>
 */
@Tag(
    name = "Story Item Management",
    description = "API for creating, retrieving, updating, searching, and deleting individual story items within a newsroom workflow. Supports advanced filtering, pagination, and association with main stories."
)
@RestController
@RequestMapping("/story-items")
@RequiredArgsConstructor
public class StoryItemController {

    private final StoryItemService storyItemService;

    @Operation(
        summary = "Create Story Item",
        description = """
            Creates a new story item with the specified properties and associates it with a parent story.
            Fields may include: type, title, text, CG comment, duration, order, media, presenters, and more.
            Typically used for granular management of rundown items in a newsroom workflow.
        """
    )
    @PostMapping
    public StoryItemDTOs.StoryItemResponse create(@RequestBody StoryItemDTOs.StoryItemCreateRequest request) {
        return storyItemService.create(request);
    }

    @Operation(
        summary = "Get Story Item by ID",
        description = """
            Retrieves a single story item by its unique identifier.
        """
    )
    @GetMapping("/{id}")
    public StoryItemDTOs.StoryItemResponse getById(@PathVariable Long id) {
        return storyItemService.getById(id);
    }

    @Operation(
        summary = "Get All Story Items by Story ID",
        description = """
            Retrieves all story items associated with a specified main story.
            Used for displaying a full rundown or script for a specific news story.
        """
    )
    @GetMapping("/by-story/{storyId}")
    public List<StoryItemDTOs.StoryItemResponse> getByStoryId(@PathVariable Long storyId) {
        return storyItemService.getByStoryId(storyId);
    }

    @Operation(
        summary = "Update Story Item",
        description = """
            Updates the details of an existing story item.
            Can be used to modify content, timing, order, CG data, etc.
        """
    )
    @PutMapping("/{id}")
    public StoryItemDTOs.StoryItemResponse update(
            @PathVariable Long id,
            @Valid @RequestBody StoryItemDTOs.StoryItemUpdateRequest request) {
        return storyItemService.update(id, request);
    }

    @Operation(
        summary = "Search and Filter Story Items with Pagination",
        description = """
            Advanced search for story items using filters (by type, story, presenter, date, etc.), with support for pagination and sorting.
            Returns paginated results: content, page, size, totalElements, totalPages.
        """
    )
    @PostMapping("/search")
    public Page<StoryItemDTOs.StoryItemResponse> search(
            @RequestBody StoryItemSearchCriteria criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {

        Sort.Order order = new Sort.Order(
                sort.length > 1 && sort[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                sort[0]
        );

        return storyItemService.search(criteria, PageRequest.of(page, size, Sort.by(order)));
    }

    @Operation(
        summary = "Delete Story Item",
        description = """
            Permanently deletes a story item by its ID.
        """
    )
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        storyItemService.delete(id);
    }
}
