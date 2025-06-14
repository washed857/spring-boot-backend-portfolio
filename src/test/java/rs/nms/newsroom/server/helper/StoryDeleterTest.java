package rs.nms.newsroom.server.service.helper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.nms.newsroom.server.domain.Story;
import rs.nms.newsroom.server.domain.User;
import rs.nms.newsroom.server.exception.ResourceNotFoundException;
import rs.nms.newsroom.server.repository.StoryRepository;
import rs.nms.newsroom.server.service.StoryLogService;
import rs.nms.newsroom.server.service.StoryVersionService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoryDeleterTest {

    @Mock private StoryRepository storyRepository;
    @Mock private StoryLogService storyLogService;
    @Mock private StoryUpdater storyUpdater;
    @Mock private StoryVersionService storyVersionService;

    @InjectMocks
    private StoryDeleter storyDeleter;

    private Story story;
    private User author;

    @BeforeEach
    void setUp() {
        author = new User();
        author.setId(15L);

        story = new Story();
        story.setId(42L);
        story.setAuthor(author);
    }

    @Test
    void testDelete_success() {
        when(storyRepository.findById(42L)).thenReturn(Optional.of(story));

        Story snapshot = new Story();
        when(storyUpdater.clone(story)).thenReturn(snapshot);

        doNothing().when(storyLogService).logChange(story, rs.nms.newsroom.server.domain.StoryLog.OperationType.DELETE, snapshot);
        doNothing().when(storyRepository).delete(story);

        storyDeleter.delete(42L);

        verify(storyRepository).findById(42L);
        verify(storyVersionService).createVersionSnapshot(42L, 15L);
        verify(storyUpdater).clone(story);
        verify(storyLogService).logChange(story, rs.nms.newsroom.server.domain.StoryLog.OperationType.DELETE, snapshot);
        verify(storyRepository).delete(story);
    }

    @Test
    void testDelete_storyNotFound_throws() {
        when(storyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> storyDeleter.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Story not found with id: 99");

        verify(storyRepository).findById(99L);
        verifyNoMoreInteractions(storyVersionService, storyUpdater, storyLogService, storyRepository);
    }
}
