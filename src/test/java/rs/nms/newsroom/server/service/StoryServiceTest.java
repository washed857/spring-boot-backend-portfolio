package rs.nms.newsroom.server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import rs.nms.newsroom.server.domain.Story;
import rs.nms.newsroom.server.dto.StoryDTOs;
import rs.nms.newsroom.server.repository.StoryRepository;
import rs.nms.newsroom.server.service.helper.*;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class StoryServiceTest {

    @Mock private StoryCreator storyCreator;
    @Mock private StoryUpdater storyUpdater;
    @Mock private StoryStatusChanger storyStatusChanger;
    @Mock private StoryMapper storyMapper;
    @Mock private StoryDeleter storyDeleter;
    @Mock private StoryLocker storyLocker;
    @Mock private StoryVersionService storyVersionService;
    @Mock private StoryRepository storyRepository;

    @InjectMocks
    private StoryService storyService;

    private Story story;
    private StoryDTOs.StoryResponse response;

    @BeforeEach
    void setUp() {
        story = new Story();
        story.setId(1L);

        response = new StoryDTOs.StoryResponse();
        response.setId(1L);
    }

    @Test
    void testCreate_delegatesToCreator() {
        StoryDTOs.StoryCreateRequest req = new StoryDTOs.StoryCreateRequest();
        when(storyCreator.create(req)).thenReturn(response);

        var res = storyService.create(req);

        assertThat(res.getId()).isEqualTo(1L);
        verify(storyCreator).create(req);
    }

    @Test
    void testGetById_delegatesToMapper() {
        when(storyMapper.getById(1L)).thenReturn(response);

        var res = storyService.getById(1L);

        assertThat(res.getId()).isEqualTo(1L);
        verify(storyMapper).getById(1L);
    }

    @Test
    void testGetByRundownId_delegatesToMapper() {
        when(storyMapper.getByRundownId(1L)).thenReturn(java.util.List.of(response));

        var res = storyService.getByRundownId(1L);

        assertThat(res).hasSize(1);
        verify(storyMapper).getByRundownId(1L);
    }

    @Test
    void testGetByAuthorId_delegatesToMapper() {
        when(storyMapper.getByAuthorId(2L)).thenReturn(java.util.List.of(response));

        var res = storyService.getByAuthorId(2L);

        assertThat(res).hasSize(1);
        verify(storyMapper).getByAuthorId(2L);
    }

    @Test
    void testUpdate_callsVersionServiceAndUpdater() {
        StoryDTOs.StoryUpdateRequest req = new StoryDTOs.StoryUpdateRequest();
        story.setAuthor(new rs.nms.newsroom.server.domain.User());
        story.getAuthor().setId(2L);

        when(storyRepository.findById(1L)).thenReturn(Optional.of(story));
        when(storyUpdater.update(1L, req)).thenReturn(response);

        var res = storyService.update(1L, req);

        assertThat(res.getId()).isEqualTo(1L);
        verify(storyVersionService).createVersionSnapshot(1L, 2L);
        verify(storyUpdater).update(1L, req);
    }

    @Test
    void testPublish_delegatesToStatusChanger() {
        when(storyStatusChanger.publish(1L, "admin")).thenReturn(response);

        var res = storyService.publish(1L, "admin");

        assertThat(res.getId()).isEqualTo(1L);
        verify(storyStatusChanger).publish(1L, "admin");
    }

    @Test
    void testReject_delegatesToStatusChanger() {
        when(storyStatusChanger.reject(1L, "editor")).thenReturn(response);

        var res = storyService.reject(1L, "editor");

        assertThat(res.getId()).isEqualTo(1L);
        verify(storyStatusChanger).reject(1L, "editor");
    }

    @Test
    void testArchive_delegatesToStatusChanger() {
        when(storyStatusChanger.archive(1L, "admin")).thenReturn(response);

        var res = storyService.archive(1L, "admin");

        assertThat(res.getId()).isEqualTo(1L);
        verify(storyStatusChanger).archive(1L, "admin");
    }

    @Test
    void testLock_delegatesToLocker() {
        when(storyLocker.lockStory(1L)).thenReturn(response);

        var res = storyService.lock(1L);

        assertThat(res.getId()).isEqualTo(1L);
        verify(storyLocker).lockStory(1L);
    }

    @Test
    void testUnlock_delegatesToLocker() {
        when(storyLocker.unlockStory(1L)).thenReturn(response);

        var res = storyService.unlock(1L);

        assertThat(res.getId()).isEqualTo(1L);
        verify(storyLocker).unlockStory(1L);
    }

    @Test
    void testDelete_callsVersionServiceAndDeleter() {
        story.setAuthor(new rs.nms.newsroom.server.domain.User());
        story.getAuthor().setId(10L);

        when(storyRepository.findById(1L)).thenReturn(Optional.of(story));

        storyService.delete(1L);

        verify(storyVersionService).createVersionSnapshot(1L, 10L);
        verify(storyDeleter).delete(1L);
    }
}