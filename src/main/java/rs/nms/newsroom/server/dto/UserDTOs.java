package rs.nms.newsroom.server.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

public class UserDTOs {

	@Data
	public static class UserCreateRequest {
	    @NotBlank
	    @Size(max = 64)
	    private String username;

	    @NotBlank
	    @Size(max = 128)
	    private String fullName;

	    @Email
	    @Size(max = 128)
	    private String email;

	    @NotBlank
	    @Size(min = 8, max = 64)
	    private String password;

	    private Long roleId;

	    private Long clientId;
	}


    @Data
    public static class UserResponse {
        private Long id;
        private String username;
        private String fullName;
        private String email;
        private String role;
        private Set<String> rolePermissions;
        private String status;
        private int activeChatCount;
        private boolean hasPassword;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String profileImagePath;
        private LocalDateTime lastLoginAt;
        private Long clientId;
        private String clientName;
    }

    @Data
    public static class UserUpdateRequest {
        private String fullName;

        @Email
        private String email;

        private String password;

        private Long roleId;
        
        private Long clientId;
    }

    @Data
    public static class ChangePasswordRequest {
        @NotBlank
        private String newPassword;
    }

    @Data
    public static class UserStatistics {
        private long total;
        private long active;
        private long inactive;
        private long admins;
        private LocalDateTime lastRegistered;
    }

    @Data
    public static class UserSearchCriteria {
        private String query;
        private String status;
        private String role;
    }

    @Data
    public static class UserLogEntry {
        private LocalDateTime timestamp;
        private String operation;
        private String performedBy;
    }
}
