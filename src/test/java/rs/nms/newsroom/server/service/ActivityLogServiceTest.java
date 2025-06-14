package rs.nms.newsroom.server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import rs.nms.newsroom.server.domain.ActivityLog;
import rs.nms.newsroom.server.domain.User;
import rs.nms.newsroom.server.dto.ActivityLogDTOs;
import rs.nms.newsroom.server.exception.ResourceNotFoundException;
import rs.nms.newsroom.server.repository.ActivityLogRepository;
import rs.nms.newsroom.server.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ActivityLogServiceTest {

    @Mock
    private ActivityLogRepository activityLogRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ActivityLogService activityLogService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // --- Test logActivity ---

    @Test
    void logActivity_shouldSaveLog_whenValidInput() {
        Long userId = 1L;
        String actionType = "CREATE";
        String targetTable = "story";
        Long targetId = 5L;
        String detail = "Details";

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        activityLogService.logActivity(userId, actionType, targetTable, targetId, detail);

        ArgumentCaptor<ActivityLog> captor = ArgumentCaptor.forClass(ActivityLog.class);
        verify(activityLogRepository, times(1)).save(captor.capture());
        ActivityLog saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getActionType()).isEqualTo(actionType);
        assertThat(saved.getTargetTable()).isEqualTo(targetTable);
        assertThat(saved.getTargetId()).isEqualTo(targetId);
        assertThat(saved.getDetail()).isEqualTo(detail);
    }

    @Test
    void logActivity_shouldThrowException_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() ->
                activityLogService.logActivity(1L, "UPDATE", "story", 3L, ""))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void logActivity_shouldThrowException_whenActionTypeBlank() {
        assertThatThrownBy(() ->
                activityLogService.logActivity(1L, "  ", "story", 3L, ""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Action type");
    }

    @Test
    void logActivity_shouldThrowException_whenUserIdNull() {
        assertThatThrownBy(() ->
                activityLogService.logActivity(null, "DELETE", "story", 3L, ""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User ID");
    }

    // --- Test getFilteredLogs ---

    @Test
    void getFilteredLogs_shouldReturnPageOfResponses() {
        ActivityLogDTOs.ActivityLogFilterRequest filter = new ActivityLogDTOs.ActivityLogFilterRequest();
        filter.setUserId(1L);
        filter.setActionType("CREATE");
        filter.setTargetTable("story");

        Pageable pageable = PageRequest.of(0, 10);

        User user = new User();
        user.setId(1L);
        user.setFullName("Pera Peric");

        ActivityLog log = new ActivityLog();
        log.setId(100L);
        log.setUser(user);
        log.setActionType("CREATE");
        log.setTargetTable("story");
        log.setTargetId(200L);
        log.setDetail("Details");
        log.setTimestamp(LocalDateTime.now());

        Page<ActivityLog> logs = new PageImpl<>(List.of(log), pageable, 1);

        when(activityLogRepository.findByFilter(
                eq(1L), eq("CREATE"), eq("story"),
                isNull(), isNull(), eq(pageable)
        )).thenReturn(logs);

        Page<ActivityLogDTOs.ActivityLogResponse> result = activityLogService.getFilteredLogs(filter, pageable);
        assertThat(result.getContent()).hasSize(1);
        ActivityLogDTOs.ActivityLogResponse resp = result.getContent().get(0);
        assertThat(resp.getId()).isEqualTo(100L);
        assertThat(resp.getUserFullName()).isEqualTo("Pera Peric");
        assertThat(resp.getActionType()).isEqualTo("CREATE");
        assertThat(resp.getTargetTable()).isEqualTo("story");
        assertThat(resp.getTargetId()).isEqualTo(200L);
    }

    @Test
    void getFilteredLogs_shouldThrowException_whenStartDateAfterEndDate() {
        ActivityLogDTOs.ActivityLogFilterRequest filter = new ActivityLogDTOs.ActivityLogFilterRequest();
        filter.setStartDate(LocalDateTime.of(2024, 1, 2, 10, 0));
        filter.setEndDate(LocalDateTime.of(2024, 1, 1, 10, 0));
        Pageable pageable = Pageable.unpaged();
        assertThatThrownBy(() -> activityLogService.getFilteredLogs(filter, pageable))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Start date");
    }

    @Test
    void getFilteredLogs_shouldThrowException_whenFilterIsNull() {
        Pageable pageable = Pageable.unpaged();
        assertThatThrownBy(() -> activityLogService.getFilteredLogs(null, pageable))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Filter");
    }
}