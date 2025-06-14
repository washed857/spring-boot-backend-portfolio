package rs.nms.newsroom.server.dto;

import lombok.Data;

import java.time.LocalDateTime;

public class ScheduleLogDTOs {

    @Data
    public static class ScheduleLogResponse {
        private Long id;
        private Long scheduleId;
        private String operation;
        private String performedBy;
        private LocalDateTime performedAt;
    }
}