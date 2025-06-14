package rs.nms.newsroom.server.service.helper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockedStatic;
import rs.nms.newsroom.server.domain.*;
import rs.nms.newsroom.server.dto.StoryDTOs;
import rs.nms.newsroom.server.exception.ResourceNotFoundException;
import rs.nms.newsroom.server.repository.StoryRepository;
import rs.nms.newsroom.server.util.ClientContextHelper;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoryMapperTest {

    @Mock private StoryRepository storyRepository;

    @InjectMocks
    private StoryMapper storyMapper;

    private static final Long CLIENT_ID = 123L;
    private static final Long RUNDOWN_ID = 44L;
    private static final Long AUTHOR_ID = 55L;

    private Story story;
    private User author;
    private Rundown rundown;
    private StoryType storyType;

    @BeforeEach
    void setUp() {
        author = new User();
        author.setId(AUTHOR_ID);
        author.setFullName("Ime Prezime");

        rundown = new Rundown();
        rundown.setId(RUNDOWN_ID);
        rundown.setTitle("Naslov Rundowna");

        storyType = new StoryType();
        storyType.setId(5L);
        storyType.setDisplayName("Breaking News");

        story = new Story();
        story.setId(100L);
        story.setTitle("Naslov vesti");
        story.setStatus(rs.nms.newsroom.server.domain.enums.StoryStatus.DRAFT);
        story.setAuthor(author);
        story.setRundown(rundown);
        story.setApprovedBy("urednik");
        story.setApprovedAt(LocalDateTime.now());
        story.setCreatedAt(LocalDateTime.now());
        story.setStoryType(storyType);

        StoryItem item1 = new StoryItem();
        item1.setId(201L);
        StoryItem item2 = new StoryItem();
        item2.setId(202L);
        story.setStoryItems(Set.of(item1, item2));
    }

    @Test
    void testGetById_success() {
        when(storyRepository.findById(100L)).thenReturn(Optional.of(story));
        StoryDTOs.StoryResponse res = storyMapper.getById(100L);
        assertThat(res.getId()).isEqualTo(100L);
        assertThat(res.getTitle()).isEqualTo("Naslov vesti");
        assertThat(res.getStoryItemIds()).containsExactlyInAnyOrder(201L, 202L);
        assertThat(res.getStoryTypeName()).isEqualTo("Breaking News");
    }

    @Test
    void testGetById_notFound() {
        when(storyRepository.findById(1000L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> storyMapper.getById(1000L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Story not found with id: 1000");
    }

    @Test
    void testGetByRundownId() {
        try (MockedStatic<ClientContextHelper> mocked = mockStatic(ClientContextHelper.class)) {
            mocked.when(ClientContextHelper::getCurrentClientId).thenReturn(CLIENT_ID);
            when(storyRepository.findByClientIdAndRundownId(CLIENT_ID, RUNDOWN_ID)).thenReturn(List.of(story));

            List<StoryDTOs.StoryResponse> result = storyMapper.getByRundownId(RUNDOWN_ID);
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getRundownId()).isEqualTo(RUNDOWN_ID);
        }
    }

    @Test
    void testGetByAuthorId() {
        try (MockedStatic<ClientContextHelper> mocked = mockStatic(ClientContextHelper.class)) {
            mocked.when(ClientContextHelper::getCurrentClientId).thenReturn(CLIENT_ID);
            when(storyRepository.findByClientIdAndAuthorId(CLIENT_ID, AUTHOR_ID)).thenReturn(List.of(story));

            List<StoryDTOs.StoryResponse> result = storyMapper.getByAuthorId(AUTHOR_ID);
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getAuthorId()).isEqualTo(AUTHOR_ID);
        }
    }

    @Test
    void testMapToResponse_full() {
        StoryDTOs.StoryResponse response = storyMapper.mapToResponse(story);
        assertThat(response.getId()).isEqualTo(story.getId());
        assertThat(response.getTitle()).isEqualTo(story.getTitle());
        assertThat(response.getStatus()).isEqualTo(story.getStatus().name());
        assertThat(response.getRundownTitle()).isEqualTo("Naslov Rundowna");
        assertThat(response.getStoryItemIds()).containsExactlyInAnyOrder(201L, 202L);
        assertThat(response.getStoryTypeId()).isEqualTo(storyType.getId());
        assertThat(response.getStoryTypeName()).isEqualTo(storyType.getDisplayName());
    }

    @Test
    void testMapToResponse_withoutStoryType() {
        story.setStoryType(null);
        StoryDTOs.StoryResponse response = storyMapper.mapToResponse(story);
        assertThat(response.getStoryTypeId()).isNull();
        assertThat(response.getStoryTypeName()).isNull();
    }
}
