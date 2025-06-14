package rs.nms.newsroom.server.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

public class RoleDTOs {

    @Data
    public static class RoleCreateRequest {
        @NotBlank
        private String name;
        private Set<Long> permissionIds;
    }

    @Data
    public static class RoleUpdateRequest {
        private String name;
        private Set<Long> permissionIds;
    }

    @Data
    public static class RoleResponse {
        private Long id;
        private String name;
        private Set<String> permissions;
    }

    @Data
    public static class RoleSummary {
        private Long id;
        private String name;
    }

    @Data
    public static class RoleLogEntry {
        private LocalDateTime timestamp;
        private String operation;
        private String performedBy;
        private String before;
        private String after;
    }
}