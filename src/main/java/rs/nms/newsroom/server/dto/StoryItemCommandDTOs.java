package rs.nms.newsroom.server.dto;

import lombok.Data;
import java.util.List;

@Data
public class StoryItemCommandDTOs {

    @Data
    public static class StoryItemCommandCreateRequest {
        private Long storyItemId;
        private String commandType;
        private Long commandId;
        private Integer executionOrder = 1;
        private Integer delayMs = 0;
        private String parameters; // JSON string
    }

    @Data
    public static class StoryItemCommandResponse {
        private Long id;
        private Long storyItemId;
        private String commandType;
        private Long commandId;
        private Integer executionOrder;
        private Integer delayMs;
        private String parameters;
    }

    @Data
    public static class StoryItemCommandUpdateRequest {
        private String commandType;
        private Long commandId;
        private Integer executionOrder;
        private Integer delayMs;
        private String parameters;
    }

    @Data
    public static class BulkCommandsRequest {
        private Long storyItemId;
        private List<StoryItemCommandCreateRequest> commands;
    }
}