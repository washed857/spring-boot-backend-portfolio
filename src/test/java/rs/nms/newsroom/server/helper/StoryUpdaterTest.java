package rs.nms.newsroom.server.service.helper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.nms.newsroom.server.domain.Story;
import rs.nms.newsroom.server.domain.StoryType;
import rs.nms.newsroom.server.domain.enums.StoryStatus;
import rs.nms.newsroom.server.dto.StoryDTOs;
import rs.nms.newsroom.server.exception.ResourceNotFoundException;
import rs.nms.newsroom.server.repository.StoryRepository;
import rs.nms.newsroom.server.repository.StoryTypeRepository;
import rs.nms.newsroom.server.service.NotificationSender;
import rs.nms.newsroom.server.service.StoryLogService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoryUpdaterTest {

    @Mock private StoryRepository storyRepository;
    @Mock private StoryTypeRepository storyTypeRepository;
    @Mock private StoryLogService storyLogService;
    @Mock private NotificationSender notificationSender;
    @Mock private StoryMapper storyMapper;

    @InjectMocks
    private StoryUpdater storyUpdater;

    private Story story;
    private StoryType storyType;
    private StoryDTOs.StoryUpdateRequest updateRequest;
    private StoryDTOs.StoryResponse responseDto;

    @BeforeEach
    void setUp() {
        storyType = new StoryType();
        storyType.setId(101L);

        story = new Story();
        story.setId(42L);
        story.setStatus(StoryStatus.DRAFT);

        updateRequest = new StoryDTOs.StoryUpdateRequest();
        updateRequest.setTitle("New title");
        updateRequest.setStatus("PUBLISHED");
        updateRequest.setApprovedBy("editor");
        updateRequest.setStoryTypeId(101L);

        responseDto = new StoryDTOs.StoryResponse();
        responseDto.setId(42L);
        responseDto.setTitle("New title");
    }

    @Test
    void testUpdate_success_allFields() {
        when(storyRepository.findById(42L)).thenReturn(Optional.of(story));
        when(storyTypeRepository.findById(101L)).thenReturn(Optional.of(storyType));
        when(storyRepository.save(any(Story.class))).thenAnswer(inv -> inv.getArgument(0));
        when(storyMapper.mapToResponse(any(Story.class))).thenReturn(responseDto);

        StoryDTOs.StoryResponse result = storyUpdater.update(42L, updateRequest);

        assertThat(result).isNotNull();
        assertThat(story.getTitle()).isEqualTo("New title");
        assertThat(story.getStatus()).isEqualTo(StoryStatus.PUBLISHED);
        assertThat(story.getApprovedBy()).isEqualTo("editor");
        assertThat(story.getApprovedAt()).isNotNull();
        assertThat(story.getStoryType()).isSameAs(storyType);

        verify(storyRepository).save(story);
        verify(notificationSender).sendGlobalNotification(
                eq(rs.nms.newsroom.server.domain.enums.NotificationType.STORY_STATUS_CHANGED),
                contains("Status"),
                any()
        );
        verify(storyLogService).logChange(eq(story), eq(rs.nms.newsroom.server.domain.StoryLog.OperationType.UPDATE), any());
        verify(storyMapper, atLeastOnce()).mapToResponse(story);
    }

    @Test
    void testUpdate_noStatus_noNotification() {
        updateRequest.setStatus(null);
        when(storyRepository.findById(42L)).thenReturn(Optional.of(story));
        when(storyTypeRepository.findById(101L)).thenReturn(Optional.of(storyType));
        when(storyRepository.save(any(Story.class))).thenAnswer(inv -> inv.getArgument(0));
        when(storyMapper.mapToResponse(any(Story.class))).thenReturn(responseDto);

        storyUpdater.update(42L, updateRequest);

        verify(notificationSender, never()).sendGlobalNotification(any(), any(), any());
    }

    @Test
    void testUpdate_storyNotFound() {
        when(storyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> storyUpdater.update(99L, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Story not found with id: 99");
    }

    @Test
    void testUpdate_invalidStatus_throws() {
        updateRequest.setStatus("INVALID_STATUS");
        when(storyRepository.findById(42L)).thenReturn(Optional.of(story));

        assertThatThrownBy(() -> storyUpdater.update(42L, updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid story status");
    }

    @Test
    void testUpdate_storyTypeNotFound_throws() {
        when(storyRepository.findById(42L)).thenReturn(Optional.of(story));
        when(storyTypeRepository.findById(101L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> storyUpdater.update(42L, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("StoryType not found with id: 101");
    }

    @Test
    void testUpdate_onlyTitle() {
        updateRequest = new StoryDTOs.StoryUpdateRequest();
        updateRequest.setTitle("Title only");
        when(storyRepository.findById(42L)).thenReturn(Optional.of(story));
        when(storyRepository.save(any(Story.class))).thenAnswer(inv -> inv.getArgument(0));
        when(storyMapper.mapToResponse(any(Story.class))).thenReturn(responseDto);

        StoryDTOs.StoryResponse result = storyUpdater.update(42L, updateRequest);

        assertThat(story.getTitle()).isEqualTo("Title only");
        verify(notificationSender, never()).sendGlobalNotification(any(), any(), any());
    }
}
