package rs.nms.newsroom.server.service.helper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.nms.newsroom.server.domain.Story;
import rs.nms.newsroom.server.domain.enums.StoryStatus;
import rs.nms.newsroom.server.dto.StoryDTOs;
import rs.nms.newsroom.server.exception.ResourceNotFoundException;
import rs.nms.newsroom.server.repository.StoryRepository;
import rs.nms.newsroom.server.service.NotificationSender;
import rs.nms.newsroom.server.service.StoryLogService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoryStatusChangerTest {

    @Mock private StoryRepository storyRepository;
    @Mock private StoryLogService storyLogService;
    @Mock private NotificationSender notificationService;
    @Mock private StoryMapper storyMapper;
    @Mock private StoryUpdater storyUpdater;

    @InjectMocks
    private StoryStatusChanger statusChanger;

    private Story story;
    private Story snapshotBefore;
    private StoryDTOs.StoryResponse responseDto;

    @BeforeEach
    void setUp() {
        story = new Story();
        story.setId(10L);
        story.setTitle("Breaking News");
        story.setStatus(StoryStatus.DRAFT);

        snapshotBefore = new Story();
        snapshotBefore.setId(10L);
        snapshotBefore.setStatus(StoryStatus.DRAFT);

        responseDto = new StoryDTOs.StoryResponse();
        responseDto.setId(10L);
        responseDto.setTitle("Breaking News");
        responseDto.setStatus(StoryStatus.PUBLISHED.name());
    }

    @Test
    void testPublish_success() {
        when(storyRepository.findById(10L)).thenReturn(Optional.of(story));
        when(storyUpdater.clone(story)).thenReturn(snapshotBefore);
        when(storyRepository.save(any(Story.class))).thenAnswer(inv -> inv.getArgument(0));
        when(storyMapper.mapToResponse(any(Story.class))).thenReturn(responseDto);

        StoryDTOs.StoryResponse res = statusChanger.publish(10L, "editor");

        assertThat(res).isNotNull();
        verify(storyRepository).findById(10L);
        verify(storyUpdater).clone(story);
        verify(storyRepository).save(story);
        verify(storyLogService).logChange(
                eq(story),
                eq(rs.nms.newsroom.server.domain.StoryLog.OperationType.UPDATE),
                eq(snapshotBefore)
        );
        verify(notificationService).sendGlobalNotification(
                eq(rs.nms.newsroom.server.domain.enums.NotificationType.STORY_STATUS_CHANGED),
                anyString(), // <-- Accept any notification message
                any()
        );
        verify(storyMapper, atLeastOnce()).mapToResponse(any(Story.class));

        assertThat(story.getStatus()).isEqualTo(StoryStatus.PUBLISHED);
        assertThat(story.getApprovedBy()).isEqualTo("editor");
        assertThat(story.getApprovedAt()).isNotNull();
    }

    @Test
    void testReject_success() {
        when(storyRepository.findById(10L)).thenReturn(Optional.of(story));
        when(storyUpdater.clone(story)).thenReturn(snapshotBefore);
        when(storyRepository.save(any(Story.class))).thenAnswer(inv -> inv.getArgument(0));
        when(storyMapper.mapToResponse(any(Story.class))).thenReturn(responseDto);

        StoryDTOs.StoryResponse res = statusChanger.reject(10L, "chief_editor");

        assertThat(story.getStatus()).isEqualTo(StoryStatus.REJECTED);
        assertThat(story.getApprovedBy()).isEqualTo("chief_editor");
        assertThat(story.getApprovedAt()).isNotNull();
        assertThat(res).isNotNull();
    }

    @Test
    void testArchive_success() {
        when(storyRepository.findById(10L)).thenReturn(Optional.of(story));
        when(storyUpdater.clone(story)).thenReturn(snapshotBefore);
        when(storyRepository.save(any(Story.class))).thenAnswer(inv -> inv.getArgument(0));
        when(storyMapper.mapToResponse(any(Story.class))).thenReturn(responseDto);

        StoryDTOs.StoryResponse res = statusChanger.archive(10L, "admin");

        assertThat(story.getStatus()).isEqualTo(StoryStatus.ARCHIVED);
        assertThat(story.getApprovedBy()).isEqualTo("admin");
        assertThat(story.getApprovedAt()).isNotNull();
        assertThat(res).isNotNull();
    }

    @Test
    void testChangeStatus_storyNotFound_throws() {
        when(storyRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> statusChanger.publish(999L, "someone"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Story not found with id: 999");
    }
}