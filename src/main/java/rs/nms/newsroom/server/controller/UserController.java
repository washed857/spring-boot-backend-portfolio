package rs.nms.newsroom.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import rs.nms.newsroom.server.config.security.CustomUserDetails;
import rs.nms.newsroom.server.dto.UserDTOs;
import rs.nms.newsroom.server.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;

import rs.nms.newsroom.server.dto.common.PagedResponse;

import java.io.IOException;
import java.util.List;

@Tag(
	 name = "User Management", 
	 description = "User and account administration, including roles, status, password, and profile image management. All changes are audit-logged."
)
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
    	    summary = "Create a New User",
    	    description = """
    	        Creates a new user with the provided data and assigns a role.
    	        - Username and email must be unique.
    	        - All user creation events are recorded in the audit log.
    	        - Only users with the CREATE_USER permission can use this endpoint.
    	        - Returns a 400 error in case of duplicate username or email.
    	    """
    )
    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_USER')")
    public UserDTOs.UserResponse create(@Valid @RequestBody UserDTOs.UserCreateRequest createRequest) {
        return userService.create(createRequest);
    }

    @Operation(
    	    summary = "Get User by ID",
    	    description = """
    	        Retrieves user details by the specified ID.
    	        - Requires VIEW_USER permission or the user requesting their own data (#id == principal.id).
    	        - Returns 404 if the user does not exist.
    	    """
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_USER') or #id == principal.id")
    public UserDTOs.UserResponse getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @Operation(
    	    summary = "Get All Users",
    	    description = """
    	        Returns a list of all users for the current client (tenant).
    	        - Requires VIEW_USER permission.
    	        - Pagination is recommended in production environments.
    	    """
    )
    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_USER')")
    public List<UserDTOs.UserResponse> getAll() {
        return userService.getAll();
    }

    @Operation(
    	    summary = "Search Users with Filters and Pagination",
    	    description = """
    	        Searches for users based on filters: status, role, general text query, with support for pagination and sorting.
    	        - Requires SEARCH_USER permission.
    	        - Query parameters: page, size, sort (e.g., sort=username,desc).
    	        - Returns total count, total pages, and current page.
    	        - All queries are limited to users within the same client (multitenancy).
    	    """
    )
    @PostMapping("/search")
    @PreAuthorize("hasAuthority('SEARCH_USER')")
    public PagedResponse<UserDTOs.UserResponse> searchUsers(
            @RequestBody UserDTOs.UserSearchCriteria criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort
    ) {
        Sort.Order order = new Sort.Order(
                sort.length > 1 && sort[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                sort[0]
        );
        Pageable pageable = PageRequest.of(page, size, Sort.by(order));
        Page<UserDTOs.UserResponse> resultPage = userService.searchUsers(criteria, pageable);

        PagedResponse<UserDTOs.UserResponse> response = new PagedResponse<>();
        response.setContent(resultPage.getContent());
        response.setPage(resultPage.getNumber());
        response.setSize(resultPage.getSize());
        response.setTotalElements(resultPage.getTotalElements());
        response.setTotalPages(resultPage.getTotalPages());
        response.setLast(resultPage.isLast());
        return response;
    }

    @Operation(
    	    summary = "Update User",
    	    description = """
    	        Updates user details (name, email, password, role).
    	        - Requires UPDATE_USER permission or the user may update their own data.
    	        - All updates are recorded in the audit log (UserLog).
    	        - Email must be unique; password changes are specifically logged.
    	        - Username cannot be changed.
    	    """
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_USER') or #id == principal.id")
    public UserDTOs.UserResponse updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDTOs.UserUpdateRequest request) {
        return userService.update(id, request);
    }

    @Operation(
    	    summary = "Delete User (Soft Delete)",
    	    description = """
    	        Marks the user as deleted (soft delete).
    	        - Requires DELETE_USER permission.
    	        - Deletion is allowed only for users within the current client (tenant).
    	        - All deletions are recorded in the audit log.
    	        - Returns 404 if the user does not exist or is already deleted.
    	        - A user cannot delete themselves.
    	    """
    	)
    	@DeleteMapping("/{id}")
    	@PreAuthorize("hasAuthority('DELETE_USER')")
    	public ResponseEntity<Void> delete(
    	        @PathVariable Long id,
    	        @AuthenticationPrincipal CustomUserDetails currentUser
    	) {
    	    if (currentUser != null && currentUser.getId().equals(id)) {
    	        // SECURITY: Self-deletion is disabled to prevent accidental data loss.  
		// Only admin-level deletion is permitted (see AdminController).  
    	        return ResponseEntity.status(403).build();
    	    }
    	    userService.delete(id);
    	    return ResponseEntity.noContent().build();
    	}


    @Operation(
    	    summary = "Change User Status",
    	    description = """
    	        Changes the status of a user (e.g., ACTIVE, INACTIVE, SUSPENDED).
    	        - Requires UPDATE_USER_STATUS permission.
    	        - All status changes are recorded in the audit log (UserLog).
    	        - Status can be any string (standard values should be agreed upon).
    	    """
    )
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('UPDATE_USER_STATUS')")
    public void changeStatus(@PathVariable Long id, @RequestParam String status) {
        userService.changeStatus(id, status);
    }

    @Operation(
    	    summary = "Get Current Authenticated User",
    	    description = """
    	        Returns details of the currently authenticated user based on the JWT token.
    	        - Requires a valid JWT token.
    	        - The user must be authenticated.
    	    """
    )
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserDTOs.UserResponse getCurrentUser(@AuthenticationPrincipal CustomUserDetails currentUser) {
        return userService.getById(currentUser.getId());
    }

    @Operation(
    	    summary = "Get User Statistics",
    	    description = """
    	        Returns basic user statistics:
    	        - total number of users,
    	        - number of active and inactive users,
    	        - number of administrators,
    	        - date of last registration.
    	        - Requires VIEW_USER_STATS permission.
    	    """
    )
    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('VIEW_USER_STATS')")
    public UserDTOs.UserStatistics getUserStats() {
        return userService.getUserStatistics();
    }

    @Operation(
    	    summary = "Change User Password",
    	    description = """
    	        Changes the password of a user.
    	        - Requires CHANGE_USER_PASSWORD permission or the user may change their own password.
    	        - All password changes are recorded in the UserLog.
    	        - Password must meet minimum strength requirements (validated in the DTO).
    	    """
    )
    @PatchMapping("/{id}/password")
    @PreAuthorize("hasAuthority('CHANGE_USER_PASSWORD') or #id == principal.id")
    public void changePassword(@PathVariable Long id, @Valid @RequestBody UserDTOs.ChangePasswordRequest request) {
        userService.changePassword(id, request);
    }

    @Operation(
    	    summary = "Get User Activity Logs",
    	    description = """
    	        Returns a list of activity logs for the specified user, sorted by date (descending).
    	        - Requires VIEW_USER_LOGS permission.
    	        - All changes, deletions, password and status updates, etc., are recorded in the log.
    	    """
    )
    @GetMapping("/{id}/logs")
    @PreAuthorize("hasAuthority('VIEW_USER_LOGS')")
    public List<UserDTOs.UserLogEntry> getUserLogs(@PathVariable Long id) {
        return userService.getUserLog(id);
    }
    
    @Operation(
    	    summary = "Upload User Profile Image",
    	    description = """
    	        Allows the user to upload or change their profile image.
    	        - The image is stored on the server; only the image path is saved in the database.
    	        - Requires a valid JWT token.
    	    """
    	)
    	@PostMapping("/{id}/profile-image")
    	@PreAuthorize("#id == principal.id or hasAuthority('UPDATE_USER')")
    	public ResponseEntity<UserDTOs.UserResponse> uploadProfileImage(
    	        @PathVariable Long id,
    	        @RequestParam("file") MultipartFile file
    	) throws IOException {
    	    UserDTOs.UserResponse updatedUser = userService.updateProfileImage(id, file);
    	    return ResponseEntity.ok(updatedUser);
    	}
}