package rs.nms.newsroom.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import rs.nms.newsroom.server.domain.Story;
import rs.nms.newsroom.server.domain.StoryVersion;
import rs.nms.newsroom.server.dto.ArticleVersionDTOs;
import rs.nms.newsroom.server.exception.ResourceNotFoundException;
import rs.nms.newsroom.server.repository.StoryRepository;
import rs.nms.newsroom.server.repository.StoryVersionRepository;
import rs.nms.newsroom.server.service.helper.StoryMapper;
import rs.nms.newsroom.server.service.helper.StoryUpdater;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ArticleVersionServiceTest {

    @Mock private StoryRepository storyRepository;
    @Mock private StoryVersionRepository versionRepository;
    @Mock private StoryUpdater storyUpdater;
    @Mock private StoryMapper storyMapper;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private ArticleVersionService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createVersion_shouldReturnResponse_whenValidRequest() throws Exception {
        // Arrange
        Long storyId = 1L;
        Long userId = 5L;
        int versionNumber = 1;
        String jsonSnapshot = "{\"id\":1,\"title\":\"story\"}";

        ArticleVersionDTOs.ArticleVersionCreateRequest req = new ArticleVersionDTOs.ArticleVersionCreateRequest();
        req.setStoryId(storyId);
        req.setContentJson("{\"id\":1,\"title\":\"story\"}");

        Story story = new Story();
        story.setId(storyId);

        Story snapshot = new Story();
        snapshot.setId(storyId);

        StoryVersion version = new StoryVersion();
        version.setId(10L);
        version.setStory(story);
        version.setVersionNumber(versionNumber);
        version.setSnapshot(jsonSnapshot);
        version.setCreatedBy(userId);

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));
        when(storyUpdater.clone(any(Story.class))).thenReturn(snapshot);
        when(objectMapper.writeValueAsString(any(Story.class))).thenReturn(jsonSnapshot);
        when(versionRepository.countByStory(story)).thenReturn(0); // first version
        when(versionRepository.save(any(StoryVersion.class))).thenReturn(version);

        // Act
        ArticleVersionDTOs.ArticleVersionResponse response = service.createVersion(req, userId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getStoryId()).isEqualTo(storyId);
        assertThat(response.getVersionNumber()).isEqualTo(1);
        assertThat(response.getContentJson()).isEqualTo(jsonSnapshot);
        assertThat(response.getCreatedById()).isEqualTo(userId);

        // Check that collaborators were called
        verify(storyRepository).findById(storyId);
        verify(storyUpdater).clone(story);
        verify(objectMapper).writeValueAsString(snapshot);
        verify(versionRepository).save(any(StoryVersion.class));
    }

    @Test
    void createVersion_shouldThrow_ifStoryNotFound() {
        // Arrange
        ArticleVersionDTOs.ArticleVersionCreateRequest req = new ArticleVersionDTOs.ArticleVersionCreateRequest();
        req.setStoryId(1L);
        req.setContentJson("{...}");

        when(storyRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Assert
        assertThatThrownBy(() -> service.createVersion(req, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Story not found");
    }

    @Test
    void createVersion_shouldThrow_ifObjectMapperFails() throws Exception {
        // Arrange
        Long storyId = 1L;
        ArticleVersionDTOs.ArticleVersionCreateRequest req = new ArticleVersionDTOs.ArticleVersionCreateRequest();
        req.setStoryId(storyId);
        req.setContentJson("{...}");

        Story story = new Story();
        story.setId(storyId);

        Story snapshot = new Story();
        snapshot.setId(storyId);

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));
        when(storyUpdater.clone(any(Story.class))).thenReturn(snapshot);
        when(objectMapper.writeValueAsString(any(Story.class))).thenThrow(new JsonProcessingException("fail"){});

        // Assert
        assertThatThrownBy(() -> service.createVersion(req, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to serialize");
    }
}
