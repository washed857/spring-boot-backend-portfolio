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
import rs.nms.newsroom.server.repository.*;
import rs.nms.newsroom.server.service.StoryLogService;
import rs.nms.newsroom.server.util.ClientContextHelper;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class StoryCreatorTest {

    @Mock private StoryRepository storyRepository;
    @Mock private UserRepository userRepository;
    @Mock private RundownRepository rundownRepository;
    @Mock private StoryTypeRepository storyTypeRepository;
    @Mock private StoryLogService storyLogService;
    @Mock private StoryMapper storyMapper;

    @InjectMocks
    private StoryCreator storyCreator;

    private StoryDTOs.StoryCreateRequest req;
    private User author;
    private Rundown rundown;
    private StoryType storyType;

    @BeforeEach
    void setUp() {
        req = new StoryDTOs.StoryCreateRequest();
        req.setTitle("Title");
        req.setStatus("DRAFT");
        req.setAuthorId(1L);
        req.setRundownId(2L);

        author = new User();
        author.setId(1L);

        rundown = new Rundown();
        rundown.setId(2L);

        storyType = new StoryType();
        storyType.setId(50L);
    }

    @Test
    void testCreate_success_withoutStoryType() {
        try (MockedStatic<ClientContextHelper> mocked = mockStatic(ClientContextHelper.class)) {
            mocked.when(ClientContextHelper::getCurrentClientId).thenReturn(123L);

            when(userRepository.findById(1L)).thenReturn(Optional.of(author));
            when(rundownRepository.findById(2L)).thenReturn(Optional.of(rundown));
            when(storyRepository.save(any(Story.class))).thenAnswer(inv -> {
                Story s = inv.getArgument(0);
                s.setId(123L);
                return s;
            });
            when(storyMapper.mapToResponse(any(Story.class))).thenReturn(new StoryDTOs.StoryResponse());

            StoryDTOs.StoryResponse result = storyCreator.create(req);

            assertThat(result).isNotNull();
            verify(userRepository).findById(1L);
            verify(rundownRepository).findById(2L);
            verify(storyRepository).save(any(Story.class));
            verify(storyLogService).logChange(any(Story.class), eq(rs.nms.newsroom.server.domain.StoryLog.OperationType.CREATE), isNull());
            verify(storyMapper).mapToResponse(any(Story.class));
        }
    }

    @Test
    void testCreate_withInvalidStatus_throws() {
        req.setStatus("NEPOSTOJI");
        try (MockedStatic<ClientContextHelper> mocked = mockStatic(ClientContextHelper.class)) {
            mocked.when(ClientContextHelper::getCurrentClientId).thenReturn(123L);

            when(userRepository.findById(1L)).thenReturn(Optional.of(author));

            assertThatThrownBy(() -> storyCreator.create(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid story status");
        }
    }

    @Test
    void testCreate_withStoryType() {
        req.setStoryTypeId(50L);
        try (MockedStatic<ClientContextHelper> mocked = mockStatic(ClientContextHelper.class)) {
            mocked.when(ClientContextHelper::getCurrentClientId).thenReturn(123L);

            when(userRepository.findById(1L)).thenReturn(Optional.of(author));
            when(rundownRepository.findById(2L)).thenReturn(Optional.of(rundown));
            when(storyTypeRepository.findById(50L)).thenReturn(Optional.of(storyType));
            when(storyRepository.save(any(Story.class))).thenAnswer(inv -> {
                Story s = inv.getArgument(0);
                s.setId(999L);
                return s;
            });
            when(storyMapper.mapToResponse(any(Story.class))).thenReturn(new StoryDTOs.StoryResponse());

            StoryDTOs.StoryResponse result = storyCreator.create(req);

            assertThat(result).isNotNull();
            verify(storyTypeRepository).findById(50L);
        }
    }

    @Test
    void testCreate_authorNotFound_throws() {
        try (MockedStatic<ClientContextHelper> mocked = mockStatic(ClientContextHelper.class)) {
            mocked.when(ClientContextHelper::getCurrentClientId).thenReturn(123L);

            when(userRepository.findById(1L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> storyCreator.create(req))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Test
    void testCreate_rundownNotFound_throws() {
        try (MockedStatic<ClientContextHelper> mocked = mockStatic(ClientContextHelper.class)) {
            mocked.when(ClientContextHelper::getCurrentClientId).thenReturn(123L);

            when(userRepository.findById(1L)).thenReturn(Optional.of(author));
            when(rundownRepository.findById(2L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> storyCreator.create(req))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Rundown not found");
        }
    }
}
