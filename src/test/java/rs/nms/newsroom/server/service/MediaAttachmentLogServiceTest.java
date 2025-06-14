package rs.nms.newsroom.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import rs.nms.newsroom.server.domain.MediaAttachment;
import rs.nms.newsroom.server.domain.MediaAttachmentLog;
import rs.nms.newsroom.server.dto.MediaAttachmentLogDTOs.MediaAttachmentLogResponse;
import rs.nms.newsroom.server.repository.MediaAttachmentLogRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MediaAttachmentLogServiceTest {

    @Mock
    private MediaAttachmentLogRepository logRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private MediaAttachmentLogService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void logOperation_shouldSaveLogWithSnapshot() {
        MediaAttachment attachment = new MediaAttachment();
        attachment.setId(1L);
        attachment.setFileName("file.jpg");
        attachment.setMediaType("IMAGE");
        attachment.setFormat("jpg");
        attachment.setDurationSec(0);
        attachment.setWidth(100);
        attachment.setHeight(200);
        attachment.setUploadedBy("admin");
        attachment.setUploadedAt(LocalDateTime.of(2024, 5, 1, 10, 0));

        service.logOperation(attachment, "UPLOAD", "admin");

        ArgumentCaptor<MediaAttachmentLog> captor = ArgumentCaptor.forClass(MediaAttachmentLog.class);
        verify(logRepository).save(captor.capture());

        MediaAttachmentLog saved = captor.getValue();
        assertThat(saved.getOperation()).isEqualTo("UPLOAD");
        assertThat(saved.getPerformedBy()).isEqualTo("admin");
        assertThat(saved.getMediaAttachmentId()).isEqualTo(1L);
        assertThat(saved.getSnapshot()).contains("file.jpg");
        assertThat(saved.getSnapshot()).contains("IMAGE");
        assertThat(saved.getSnapshot()).doesNotContain("filePath");
    }

    @Test
    void getLogsForAttachment_shouldReturnListOfDTOs() {
        MediaAttachmentLog log1 = new MediaAttachmentLog();
        log1.setId(1L);
        log1.setMediaAttachmentId(10L);
        log1.setOperation("UPLOAD");
        log1.setPerformedBy("user");
        log1.setPerformedAt(LocalDateTime.of(2024, 5, 1, 10, 0));

        when(logRepository.findByMediaAttachmentId(10L)).thenReturn(List.of(log1));

        List<MediaAttachmentLogResponse> result = service.getLogsForAttachment(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getOperation()).isEqualTo("UPLOAD");
    }

    @Test
    void getLogsForAttachment_paged_shouldReturnPageOfDTOs() {
        MediaAttachmentLog log1 = new MediaAttachmentLog();
        log1.setId(1L);
        log1.setMediaAttachmentId(10L);
        log1.setOperation("UPLOAD");
        log1.setPerformedBy("user");
        log1.setPerformedAt(LocalDateTime.now());

        Page<MediaAttachmentLog> logPage = new PageImpl<>(List.of(log1));
        Pageable pageable = PageRequest.of(0, 10);

        when(logRepository.findByMediaAttachmentId(10L, pageable)).thenReturn(logPage);

        Page<MediaAttachmentLogResponse> result = service.getLogsForAttachment(10L, pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getOperation()).isEqualTo("UPLOAD");
    }
}
