package rs.nms.newsroom.server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import rs.nms.newsroom.server.dto.RundownDTOs;
import rs.nms.newsroom.server.service.RundownService;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for daily rundown (production playlist) management.
 * <p>
 * Provides endpoints for creation, retrieval, filtering, editing, locking,
 * and deletion of rundowns. Designed for newsroom and broadcast workflows.
 * </p>
 */
@Tag(
    name = "Rundown Management",
    description = "Management of daily rundowns (production playlists). Supports creation, filtering, editing, locking, and date-based search. Designed for newsroom and broadcast workflows."
)
@RestController
@RequestMapping("/rundowns")
@RequiredArgsConstructor
public class RundownController {

    private final RundownService rundownService;

    @PostMapping
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Create a new rundown",
        description = "Creates a new daily rundown (playlist). Only editors or administrators are allowed."
    )
    public ResponseEntity<RundownDTOs.RundownResponse> create(
            @Valid @RequestBody RundownDTOs.RundownCreateRequest createRequest) {
        return ResponseEntity.ok(rundownService.create(createRequest));
    }

    @GetMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get rundown by ID",
        description = "Fetches a rundown by its unique identifier."
    )
    public ResponseEntity<RundownDTOs.RundownResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(rundownService.getById(id));
    }

    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get all rundowns",
        description = "Returns all rundowns for the organization."
    )
    public ResponseEntity<List<RundownDTOs.RundownResponse>> getAll() {
        return ResponseEntity.ok(rundownService.getAll());
    }

    @GetMapping("/by-date")
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get rundowns by date",
        description = "Returns a list of rundowns for a specific date (ISO format: yyyy-MM-dd)."
    )
    public ResponseEntity<List<RundownDTOs.RundownResponse>> getByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(rundownService.getByDate(date));
    }
    
    @PostMapping("/filter")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Filter rundowns",
        description = "Advanced filtering for rundowns based on custom criteria. Only editors or administrators are allowed."
    )
    public ResponseEntity<List<RundownDTOs.RundownResponse>> filter(@RequestBody RundownDTOs.RundownFilterRequest filter) {
        return ResponseEntity.ok(rundownService.filter(filter));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Update rundown",
        description = "Updates an existing rundown. Only editors or administrators are allowed."
    )
    public ResponseEntity<RundownDTOs.RundownResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody RundownDTOs.RundownUpdateRequest updateRequest) {
        return ResponseEntity.ok(rundownService.update(id, updateRequest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Delete rundown",
        description = "Deletes a rundown by its ID. Only administrators are allowed."
    )
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        rundownService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{id}/lock")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Lock or unlock rundown",
        description = "Sets the lock status of a rundown (prevents or allows editing). Only editors or administrators are allowed."
    )
    public ResponseEntity<Void> setLockStatus(
            @PathVariable Long id,
            @Valid @RequestBody RundownDTOs.LockRequest request) {
        rundownService.setLockStatus(id, request.getLocked());
        return ResponseEntity.noContent().build();
    }
}