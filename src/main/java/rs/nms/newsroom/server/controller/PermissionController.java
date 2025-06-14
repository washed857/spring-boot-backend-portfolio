package rs.nms.newsroom.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rs.nms.newsroom.server.dto.PermissionDTOs;
import rs.nms.newsroom.server.service.PermissionService;

import java.util.List;

/**
 * REST controller for managing permissions.
 * <p>
 * Supports full CRUD for permission entities, strictly restricted to ADMIN users.
 * </p>
 */
@Tag(
    name = "Permission Management",
    description = "Administration of permissions, including creation, retrieval, update, and deletion. All operations are strictly limited to ADMIN users."
)
@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @Operation(
    	    summary = "Create a New Permission",
    	    description = """
    	        Creates a new permission in the system.
    	        - Only users with the ADMIN role are authorized to perform this operation.
    	    """
    )
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public PermissionDTOs.PermissionResponse create(
            @Valid @RequestBody PermissionDTOs.PermissionCreateRequest request) {
        return permissionService.create(request);
    }

    @Operation(
    	    summary = "Get All Permissions",
    	    description = """
    	        Returns a list of all permissions in the system.
    	        - Accessible to ADMIN users only.
    	    """
    )
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<PermissionDTOs.PermissionResponse> getAll() {
        return permissionService.getAll();
    }

    @Operation(
    	    summary = "Get Permission by ID",
    	    description = """
    	        Returns the details of a specific permission by its ID.
    	        - Accessible to ADMIN users only.
    	    """
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public PermissionDTOs.PermissionResponse getById(@PathVariable Long id) {
        return permissionService.getById(id);
    }

    @Operation(
    	    summary = "Update Existing Permission",
    	    description = """
    	        Updates an existing permission by its ID.
    	        - Accessible to ADMIN users only.
    	    """
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public PermissionDTOs.PermissionResponse update(
            @PathVariable Long id,
            @Valid @RequestBody PermissionDTOs.PermissionUpdateRequest request) {
        return permissionService.update(id, request);
    }

    @Operation(
    	    summary = "Delete Permission",
    	    description = """
    	        Deletes a permission by its ID.
    	        - Accessible to ADMIN users only.
    	    """
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        permissionService.delete(id);
    }
}
