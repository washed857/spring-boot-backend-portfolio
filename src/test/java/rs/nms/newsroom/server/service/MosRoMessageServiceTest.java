package rs.nms.newsroom.server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import rs.nms.newsroom.server.domain.MosRoMessage;
import rs.nms.newsroom.server.dto.MosRoMessageDTOs;
import rs.nms.newsroom.server.dto.mos.RoCreateMessage;
import rs.nms.newsroom.server.dto.mos.RoReplaceMessage;
import rs.nms.newsroom.server.dto.mos.RoUpdateMessage;
import rs.nms.newsroom.server.repository.MosRoMessageRepository;
import rs.nms.newsroom.server.websocket.MosRoWebSocketBroadcaster;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MosRoMessageServiceTest {

    @Mock
    private MosRoMessageRepository mosRoMessageRepository;

    @Mock
    private MosRoWebSocketBroadcaster webSocketBroadcaster;

    @InjectMocks
    private MosRoMessageService mosRoMessageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveFromRoCreate_shouldSaveAndBroadcast() {
        RoCreateMessage dto = new RoCreateMessage();
        dto.setRoID("ro1");
        dto.setSlug("slug-1");
        dto.setMosExternalMeta("meta-1");

        MosRoMessage savedEntity = new MosRoMessage();
        savedEntity.setId(1L);
        savedEntity.setRoId("ro1");
        savedEntity.setSlug("slug-1");
        savedEntity.setMeta("meta-1");
        savedEntity.setReceivedAt(LocalDateTime.now());

        when(mosRoMessageRepository.save(any(MosRoMessage.class))).thenReturn(savedEntity);

        mosRoMessageService.saveFromRoCreate(dto);

        verify(mosRoMessageRepository).save(any(MosRoMessage.class));
        verify(webSocketBroadcaster).broadcastCreate(savedEntity);
    }

    @Test
    void updateFromRoUpdate_whenExists_shouldUpdateAndBroadcast() {
        RoUpdateMessage dto = new RoUpdateMessage();
        dto.setRoID("ro2");
        dto.setNewSlug("new-slug");
        dto.setNewMosExternalMeta("new-meta");

        MosRoMessage entity = new MosRoMessage();
        entity.setId(2L);
        entity.setRoId("ro2");
        entity.setSlug("old-slug");
        entity.setMeta("old-meta");

        when(mosRoMessageRepository.findByRoId("ro2")).thenReturn(Optional.of(entity));
        when(mosRoMessageRepository.save(any())).thenReturn(entity);

        mosRoMessageService.updateFromRoUpdate(dto);

        assertThat(entity.getSlug()).isEqualTo("new-slug");
        assertThat(entity.getMeta()).isEqualTo("new-meta");
        verify(webSocketBroadcaster).broadcastUpdate(entity);
    }

    @Test
    void updateFromRoUpdate_whenNotExists_shouldDoNothing() {
        RoUpdateMessage dto = new RoUpdateMessage();
        dto.setRoID("roX");
        when(mosRoMessageRepository.findByRoId("roX")).thenReturn(Optional.empty());
        mosRoMessageService.updateFromRoUpdate(dto);
        verify(mosRoMessageRepository, never()).save(any());
        verify(webSocketBroadcaster, never()).broadcastUpdate(any());
    }

    @Test
    void replaceFromRoReplace_whenExists_shouldUpdateAndBroadcast() {
        RoReplaceMessage dto = new RoReplaceMessage();
        dto.setRoID("ro3");
        dto.setSlug("repl-slug");
        dto.setMosExternalMeta("repl-meta");

        MosRoMessage entity = new MosRoMessage();
        entity.setId(3L);
        entity.setRoId("ro3");

        when(mosRoMessageRepository.findByRoId("ro3")).thenReturn(Optional.of(entity));
        when(mosRoMessageRepository.save(entity)).thenReturn(entity);

        mosRoMessageService.replaceFromRoReplace(dto);

        assertThat(entity.getSlug()).isEqualTo("repl-slug");
        assertThat(entity.getMeta()).isEqualTo("repl-meta");
        verify(webSocketBroadcaster).broadcastReplace(entity);
    }

    @Test
    void replaceFromRoReplace_whenNotExists_shouldCreateAndBroadcast() {
        RoReplaceMessage dto = new RoReplaceMessage();
        dto.setRoID("ro4");
        dto.setSlug("repl-slug2");
        dto.setMosExternalMeta("repl-meta2");

        when(mosRoMessageRepository.findByRoId("ro4")).thenReturn(Optional.empty());
        when(mosRoMessageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        mosRoMessageService.replaceFromRoReplace(dto);

        verify(mosRoMessageRepository).save(any(MosRoMessage.class));
        verify(webSocketBroadcaster).broadcastReplace(any(MosRoMessage.class));
    }

    @Test
    void deleteByRoId_whenExists_shouldDeleteAndBroadcast() {
        String roId = "ro5";
        MosRoMessage entity = new MosRoMessage();
        entity.setRoId(roId);

        when(mosRoMessageRepository.findByRoId(roId)).thenReturn(Optional.of(entity));

        mosRoMessageService.deleteByRoId(roId);

        verify(mosRoMessageRepository).delete(entity);
        verify(webSocketBroadcaster).broadcastDelete(roId);
    }

    @Test
    void deleteByRoId_whenNotExists_shouldDoNothing() {
        String roId = "ro6";
        when(mosRoMessageRepository.findByRoId(roId)).thenReturn(Optional.empty());
        mosRoMessageService.deleteByRoId(roId);
        verify(mosRoMessageRepository, never()).delete(any());
        verify(webSocketBroadcaster, never()).broadcastDelete(any());
    }

    @Test
    void getAll_shouldReturnMappedDTOs() {
        MosRoMessage entity1 = new MosRoMessage();
        entity1.setId(10L);
        entity1.setRoId("ro10");
        entity1.setSlug("slug10");
        entity1.setMeta("meta10");
        entity1.setReceivedAt(LocalDateTime.now());

        when(mosRoMessageRepository.findAll()).thenReturn(List.of(entity1));

        List<MosRoMessageDTOs.MosRoMessageResponse> result = mosRoMessageService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRoId()).isEqualTo("ro10");
        assertThat(result.get(0).getSlug()).isEqualTo("slug10");
    }
}
