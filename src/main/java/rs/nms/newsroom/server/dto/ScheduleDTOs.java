package rs.nms.newsroom.server.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import rs.nms.newsroom.server.domain.enums.ScheduleStatus;

import java.time.LocalDateTime;

@Data
public class ScheduleDTOs {

    @Data
    public static class ScheduleCreateRequest {
        @NotNull
        private Long rundownId;

        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startTime;

        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime endTime;

        private String channel;

        private ScheduleStatus status = ScheduleStatus.PLANNED;
    }

    @Data
    public static class ScheduleUpdateRequest {
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startTime;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime endTime;

        private String channel;

        private ScheduleStatus status;
    }

    @Data
    public static class ScheduleResponse {
        private Long id;
        private Long rundownId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String channel;
        private String status;
    }
}