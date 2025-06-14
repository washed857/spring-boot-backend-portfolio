package rs.nms.newsroom.server.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class StoryDTOs {

    @Data
    public static class StoryCreateRequest {
        @NotBlank(message = "Title is required")
        private String title;

        private String status;

        @NotNull(message = "Author ID is required")
        private Long authorId;

        private Long rundownId;

        private String externalId;

        private Long storyTypeId;
    }

    @Data
    public static class StoryResponse {
        private Long id;
        private String title;
        private String status;
        private Long authorId;
        private String authorName;
        private Long rundownId;
        private String rundownTitle;
        private String approvedBy;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime approvedAt;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;

        private Set<Long> storyItemIds;

        private Long storyTypeId;
        private String storyTypeName;
    }

    @Data
    public static class StoryUpdateRequest {
        private String title;
        private String status;
        private String approvedBy;
        private Long rundownId;
        private Long storyTypeId;
    }

    @Data
    public static class StoryLogEntry {
        private Long id;
        private Long storyId;
        private Long userId;
        private String operation;
        private LocalDateTime timestamp;
        private String snapshotBefore;
        private String snapshotAfter;
    }
}
