package rs.nms.newsroom.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rs.nms.newsroom.server.dto.MediaAttachmentDTOs;
import rs.nms.newsroom.server.dto.MediaAttachmentSearchCriteria;
import rs.nms.newsroom.server.service.MediaAttachmentService;

import java.io.IOException;
import java.util.List;

/**
 * REST controller for media attachment management.
 * <p>
 * Supports upload, retrieval, search, and deletion of media files,
 * with fine-grained security controls and OpenAPI documentation.
 * </p>
 */
@Tag(
    name = "Media Attachment",
    description = "Management of media files (upload, search, and deletion). Supports filtering, metadata, and secure access for authorized users."
)
@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
public class MediaAttachmentController {

    private final MediaAttachmentService mediaAttachmentService;

    @Operation(
        summary = "Upload New Media File",
        description = """
            Uploads a new media file (video, audio, image, or document) with associated metadata.
            - Only users with MEDIA_UPLOAD authority can perform this action.
            - All uploads are attributed to the authenticated user.
        """
    )
    @PreAuthorize("hasAuthority('MEDIA_UPLOAD')")
    @PostMapping("/upload")
    public ResponseEntity<MediaAttachmentDTOs.MediaAttachmentResponse> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @Valid @ModelAttribute MediaAttachmentDTOs.MediaAttachmentUploadRequest request,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {

        return ResponseEntity.ok(
                mediaAttachmentService.uploadMedia(file, request, userDetails.getUsername())
        );
    }

    @Operation(
        summary = "Get Media File by ID",
        description = """
            Retrieves a single media file and its metadata by unique ID.
            - Requires MEDIA_VIEW authority.
        """
    )
    @PreAuthorize("hasAuthority('MEDIA_VIEW')")
    @GetMapping("/{id}")
    public ResponseEntity<MediaAttachmentDTOs.MediaAttachmentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(mediaAttachmentService.getById(id));
    }

    @Operation(
        summary = "Get All Media Files by Type",
        description = """
            Returns a list of all media files of a given type (VIDEO, AUDIO, IMAGE, DOCUMENT).
            - Requires MEDIA_VIEW authority.
        """
    )
    @PreAuthorize("hasAuthority('MEDIA_VIEW')")
    @GetMapping("/by-type/{mediaType}")
    public ResponseEntity<List<MediaAttachmentDTOs.MediaAttachmentResponse>> getByMediaType(
            @PathVariable String mediaType) {
        return ResponseEntity.ok(mediaAttachmentService.getByMediaType(mediaType));
    }

    @Operation(
        summary = "Search and Filter Media Files with Pagination",
        description = """
            Advanced search for media files using filters (type, date, uploader, etc.), with pagination and sorting.
            - Returns paginated results for efficient media management.
            - Requires MEDIA_VIEW authority.
        """
    )
    @PreAuthorize("hasAuthority('MEDIA_VIEW')")
    @PostMapping("/search")
    public ResponseEntity<Page<MediaAttachmentDTOs.MediaAttachmentResponse>> searchMediaAttachments(
            @RequestBody MediaAttachmentSearchCriteria criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {

        Sort.Order order = new Sort.Order(
                sort.length > 1 && sort[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                sort[0]
        );
        Pageable pageable = PageRequest.of(page, size, Sort.by(order));
        return ResponseEntity.ok(mediaAttachmentService.search(criteria, pageable));
    }

    @Operation(
        summary = "Delete Media File by ID",
        description = """
            Deletes a media file by its unique ID.
            - Only users with MEDIA_DELETE authority are permitted to perform this action.
        """
    )
    @PreAuthorize("hasAuthority('MEDIA_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) throws IOException {
        mediaAttachmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
