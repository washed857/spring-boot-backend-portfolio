package rs.nms.newsroom.server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import rs.nms.newsroom.server.dto.StoryDTOs;
import rs.nms.newsroom.server.service.StorySearchService;
import rs.nms.newsroom.server.service.StoryService;

import java.util.List;
import java.util.Map;

/**
 * REST controller for comprehensive news story management.
 * <p>
 * Supports CRUD, workflow, locking, advanced search, and editorial operations for newsroom content.
 * </p>
 */
@Tag(
    name = "Stories",
    description = "News story management, including status workflow, locking, versioning, and editorial operations."
)
@RestController
@RequestMapping("/stories")
@RequiredArgsConstructor
public class StoryController {

    private final StoryService storyService;
    private final StorySearchService storySearchService;

    @Operation(
        summary = "Create a New Story",
        description = "Creates a new news story with specified attributes. Accessible to JOURNALIST, EDITOR, and ADMIN roles."
    )
    @PostMapping
    @PreAuthorize("hasRole('JOURNALIST') or hasRole('EDITOR') or hasRole('ADMIN')")
    public ResponseEntity<StoryDTOs.StoryResponse> create(
            @Valid @RequestBody StoryDTOs.StoryCreateRequest createRequest) {
        return ResponseEntity.ok(storyService.create(createRequest));
    }

    @Operation(
        summary = "Get Story by ID",
        description = "Retrieves a news story by its unique identifier."
    )
    @GetMapping("/{id}")
    public ResponseEntity<StoryDTOs.StoryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(storyService.getById(id));
    }

    @Operation(
        summary = "Get All Stories for a Rundown",
        description = "Retrieves all news stories associated with a given rundown."
    )
    @GetMapping("/by-rundown/{rundownId}")
    public ResponseEntity<List<StoryDTOs.StoryResponse>> getByRundownId(
            @PathVariable Long rundownId) {
        return ResponseEntity.ok(storyService.getByRundownId(rundownId));
    }

    @Operation(
        summary = "Get All Stories by Author",
        description = "Retrieves all news stories created by a specific author."
    )
    @GetMapping("/by-author/{authorId}")
    public ResponseEntity<List<StoryDTOs.StoryResponse>> getByAuthorId(
            @PathVariable Long authorId) {
        return ResponseEntity.ok(storyService.getByAuthorId(authorId));
    }

    @Operation(
        summary = "Search Stories with Filters and Pagination",
        description = """
            Advanced search for news stories using filters (query, status, type, author), with pagination support.
            Requires story.read authority.
        """
    )
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('story.read')")
    public ResponseEntity<?> searchStories(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long authorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<StoryDTOs.StoryResponse> result = storySearchService.search(query, status, type, authorId, pageable);
        return ResponseEntity.ok(result);
    }

    @Operation(
        summary = "Update Story (PUT)",
        description = "Updates the details of an existing news story. Accessible to EDITOR and ADMIN roles."
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    public ResponseEntity<StoryDTOs.StoryResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody StoryDTOs.StoryUpdateRequest updateRequest) {
        return ResponseEntity.ok(storyService.update(id, updateRequest));
    }

    @Operation(
        summary = "Delete Story (DELETE)",
        description = "Deletes a news story by its ID. Accessible to EDITOR and ADMIN roles."
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        storyService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Publish Story",
        description = "Publishes a news story. Only accessible to EDITOR and ADMIN roles. Requires approval information."
    )
    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    public ResponseEntity<StoryDTOs.StoryResponse> publish(
            @PathVariable Long id,
            @RequestParam String approvedBy) {
        return ResponseEntity.ok(storyService.publish(id, approvedBy));
    }

    @Operation(
        summary = "Reject Story",
        description = "Rejects a news story. Only accessible to EDITOR and ADMIN roles. Requires information about who rejected."
    )
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    public ResponseEntity<StoryDTOs.StoryResponse> reject(
            @PathVariable Long id,
            @RequestParam String rejectedBy) {
        return ResponseEntity.ok(storyService.reject(id, rejectedBy));
    }

    @Operation(
        summary = "Archive Story",
        description = "Archives a news story. Only accessible to EDITOR and ADMIN roles. Requires information about who archived."
    )
    @PutMapping("/{id}/archive")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    public ResponseEntity<StoryDTOs.StoryResponse> archive(
            @PathVariable Long id,
            @RequestParam String archivedBy) {
        return ResponseEntity.ok(storyService.archive(id, archivedBy));
    }

    @Operation(
        summary = "Lock Story",
        description = "Locks a story to prevent concurrent editing. Requires story.lock authority."
    )
    @PutMapping("/{id}/lock")
    @PreAuthorize("hasAuthority('story.lock')")
    public ResponseEntity<StoryDTOs.StoryResponse> lockStory(@PathVariable Long id) {
        return ResponseEntity.ok(storyService.lock(id));
    }

    @Operation(
        summary = "Unlock Story",
        description = "Unlocks a previously locked story. Requires story.lock authority."
    )
    @PutMapping("/{id}/unlock")
    @PreAuthorize("hasAuthority('story.lock')")
    public ResponseEntity<StoryDTOs.StoryResponse> unlockStory(@PathVariable Long id) {
        return ResponseEntity.ok(storyService.unlock(id));
    }
}