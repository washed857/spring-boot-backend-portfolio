package rs.nms.newsroom.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rs.nms.newsroom.server.dto.RoleDTOs;
import rs.nms.newsroom.server.service.RoleService;

import java.util.List;

/**
 * REST controller for user role management.
 * <p>
 * Supports CRUD operations for user roles, intended for administrative use.
 * All endpoints are secured by role-based and permission-based access controls.
 * </p>
 */
@Tag(
    name = "Role Management",
    description = "Administration of user roles, including creation, editing, retrieval, and deletion. Only users with appropriate privileges (typically ADMIN) may perform these actions."
)
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(
    	    summary = "Create a New Role",
    	    description = """
    	        Creates a new user role.  
    	        Accessible to ADMIN users only.
    	    """
    )
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public RoleDTOs.RoleResponse create(@Valid @RequestBody RoleDTOs.RoleCreateRequest request) {
        return roleService.create(request);
    }

    @Operation(
    	    summary = "Get All Roles (ID & Name Only)",
    	    description = """
    	        Returns a list of all user roles, containing only their ID and name.  
    	        Typically used for dropdown selection.  
    	        Requires VIEW_ROLE or CREATE_USER permission.
    	    """
    )
    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_ROLE') or hasAuthority('CREATE_USER')")
    public List<RoleDTOs.RoleSummary> getAll() {
        return roleService.getAllSummaries();
    }

    @Operation(
    	    summary = "Get Role Details by ID",
    	    description = """
    	        Returns detailed information about a specific user role based on its ID.  
    	        Accessible to ADMIN users only.
    	    """
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public RoleDTOs.RoleResponse getById(@PathVariable Long id) {
        return roleService.getById(id);
    }

    @Operation(
    	    summary = "Update an Existing Role",
    	    description = """
    	        Updates the details of an existing user role.  
    	        Accessible to ADMIN users only.
    	    """
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public RoleDTOs.RoleResponse update(
            @PathVariable Long id,
            @Valid @RequestBody RoleDTOs.RoleUpdateRequest request) {
        return roleService.update(id, request);
    }

    @Operation(
    	    summary = "Delete Role",
    	    description = """
    	        Deletes a user role by its ID.  
    	        Accessible to ADMIN users only.
    	    """
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        roleService.delete(id);
    }
}
