package rs.nms.newsroom.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import rs.nms.newsroom.server.domain.Location;
import rs.nms.newsroom.server.domain.LocationLog;
import rs.nms.newsroom.server.dto.LocationLogDTOs.LocationLogResponse;
import rs.nms.newsroom.server.repository.LocationLogRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class LocationLogServiceTest {

    @Mock
    private LocationLogRepository logRepository;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private LocationLogService locationLogService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void log_shouldSaveLogWithCorrectFields() throws Exception {
        // Arrange
        Location before = new Location();
        before.setId(1L);
        Location after = new Location();
        after.setId(1L);

        String beforeJson = "{\"id\":1}";
        String afterJson = "{\"id\":1}";
        when(objectMapper.writeValueAsString(before)).thenReturn(beforeJson);
        when(objectMapper.writeValueAsString(after)).thenReturn(afterJson);

        // Act
        locationLogService.log("UPDATE", "user@example.com", before, after);

        // Assert
        ArgumentCaptor<LocationLog> logCaptor = ArgumentCaptor.forClass(LocationLog.class);
        verify(logRepository, times(1)).save(logCaptor.capture());
        LocationLog savedLog = logCaptor.getValue();

        assertThat(savedLog.getLocationId()).isEqualTo(1L);
        assertThat(savedLog.getOperation()).isEqualTo("UPDATE");
        assertThat(savedLog.getPerformedBy()).isEqualTo("user@example.com");
        assertThat(savedLog.getSnapshotBefore()).isEqualTo(beforeJson);
        assertThat(savedLog.getSnapshotAfter()).isEqualTo(afterJson);
        assertThat(savedLog.getPerformedAt()).isNotNull();
    }

    @Test
    void log_shouldFallbackToEmptyJsonOnSerializationError() throws Exception {
        Location before = new Location();
        before.setId(2L);
        Location after = new Location();
        after.setId(2L);

        when(objectMapper.writeValueAsString(any(Location.class)))
                .thenThrow(new JsonProcessingException("fail") {});

        locationLogService.log("CREATE", "admin", before, after);

        ArgumentCaptor<LocationLog> logCaptor = ArgumentCaptor.forClass(LocationLog.class);
        verify(logRepository).save(logCaptor.capture());
        LocationLog savedLog = logCaptor.getValue();

        assertThat(savedLog.getSnapshotBefore()).isEqualTo("{}");
        assertThat(savedLog.getSnapshotAfter()).isEqualTo("{}");
    }

    @Test
    void log_shouldHandleNullBeforeAndAfter() {
        // Act
        locationLogService.log("DELETE", "admin", null, null);

        ArgumentCaptor<LocationLog> logCaptor = ArgumentCaptor.forClass(LocationLog.class);
        verify(logRepository).save(logCaptor.capture());
        LocationLog savedLog = logCaptor.getValue();

        assertThat(savedLog.getLocationId()).isNull();
        assertThat(savedLog.getSnapshotBefore()).isNull();
        assertThat(savedLog.getSnapshotAfter()).isNull();
    }

    @Test
    void getLogsForLocation_shouldReturnMappedDTOsWithPagination() {
        // Arrange
        Long locationId = 99L;
        Pageable pageable = PageRequest.of(0, 2, Sort.by("performedAt").descending());

        LocationLog log1 = new LocationLog();
        log1.setId(10L);
        log1.setLocationId(locationId);
        log1.setOperation("UPDATE");
        log1.setPerformedBy("user1");
        log1.setPerformedAt(LocalDateTime.now());
        log1.setSnapshotBefore("before1");
        log1.setSnapshotAfter("after1");

        LocationLog log2 = new LocationLog();
        log2.setId(11L);
        log2.setLocationId(locationId);
        log2.setOperation("CREATE");
        log2.setPerformedBy("user2");
        log2.setPerformedAt(LocalDateTime.now());
        log2.setSnapshotBefore("before2");
        log2.setSnapshotAfter("after2");

        List<LocationLog> logs = List.of(log1, log2);
        Page<LocationLog> pagedLogs = new PageImpl<>(logs, pageable, 2);

        when(logRepository.findByLocationId(locationId, pageable)).thenReturn(pagedLogs);

        // Act
        Page<LocationLogResponse> result = locationLogService.getLogsForLocation(locationId, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);

        LocationLogResponse dto1 = result.getContent().get(0);
        assertThat(dto1.getId()).isEqualTo(10L);
        assertThat(dto1.getLocationId()).isEqualTo(locationId);
        assertThat(dto1.getOperation()).isEqualTo("UPDATE");
        assertThat(dto1.getPerformedBy()).isEqualTo("user1");
        assertThat(dto1.getSnapshotBefore()).isEqualTo("before1");
        assertThat(dto1.getSnapshotAfter()).isEqualTo("after1");

        LocationLogResponse dto2 = result.getContent().get(1);
        assertThat(dto2.getId()).isEqualTo(11L);
        assertThat(dto2.getLocationId()).isEqualTo(locationId);
        assertThat(dto2.getOperation()).isEqualTo("CREATE");
        assertThat(dto2.getPerformedBy()).isEqualTo("user2");
        assertThat(dto2.getSnapshotBefore()).isEqualTo("before2");
        assertThat(dto2.getSnapshotAfter()).isEqualTo("after2");
    }
}
