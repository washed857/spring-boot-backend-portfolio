package rs.nms.newsroom.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import rs.nms.newsroom.server.domain.StoryItem;
import rs.nms.newsroom.server.dto.StoryItemDTOs;
import rs.nms.newsroom.server.dto.StoryItemSearchCriteria;
import rs.nms.newsroom.server.exception.ResourceNotFoundException;
import rs.nms.newsroom.server.repository.StoryItemRepository;
import rs.nms.newsroom.server.service.helper.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoryItemServiceTest {

    @Mock
    private StoryItemRepository storyItemRepository;
    @Mock
    private StoryItemFactory storyItemFactory;
    @Mock
    private StoryItemAuditService auditService;
    @Mock
    private StoryItemMetricsService metricsService;
    @Mock
    private StoryItemMapper mapper;
    @Mock
    private StoryItemSearcher storyItemSearcher;

    @InjectMocks
    private StoryItemService storyItemService;

    private StoryItem testItem;
    private StoryItemDTOs.StoryItemCreateRequest createReq;
    private StoryItemDTOs.StoryItemUpdateRequest updateReq;
    private StoryItemDTOs.StoryItemResponse responseDto;

    @BeforeEach
    void setup() {
        testItem = new StoryItem();
        testItem.setId(1L);

        createReq = new StoryItemDTOs.StoryItemCreateRequest();
        createReq.setType("PKG");
        createReq.setDisplayOrder(1);
        createReq.setStoryId(10L);

        updateReq = new StoryItemDTOs.StoryItemUpdateRequest();
        updateReq.setType("LIVE");

        responseDto = new StoryItemDTOs.StoryItemResponse();
        responseDto.setId(1L);
        responseDto.setTypeCode("PKG");
    }

    @Test
    void testCreate_success() {
        when(storyItemFactory.createFrom(createReq)).thenReturn(testItem);
        when(storyItemRepository.save(any(StoryItem.class))).thenReturn(testItem);
        when(mapper.toResponse(testItem)).thenReturn(responseDto);

        StoryItemDTOs.StoryItemResponse result = storyItemService.create(createReq);

        assertThat(result.getId()).isEqualTo(1L);
        verify(metricsService).incrementCreated();
        verify(auditService).log(eq("CREATE"), isNull(), eq(testItem));
    }

    @Test
    void testGetById_success() {
        when(storyItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(mapper.toResponse(testItem)).thenReturn(responseDto);

        StoryItemDTOs.StoryItemResponse result = storyItemService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void testGetById_notFound() {
        when(storyItemRepository.findById(5L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> storyItemService.getById(5L));
    }

    @Test
    void testGetByStoryId_success() {
        when(storyItemRepository.findByStoryIdOrdered(10L)).thenReturn(List.of(testItem));
        when(mapper.toResponse(testItem)).thenReturn(responseDto);

        List<StoryItemDTOs.StoryItemResponse> result = storyItemService.getByStoryId(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void testUpdate_success() {
        when(storyItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        doNothing().when(storyItemFactory).updateFrom(eq(testItem), eq(updateReq));
        when(storyItemRepository.save(testItem)).thenReturn(testItem);
        when(mapper.toResponse(testItem)).thenReturn(responseDto);

        StoryItemDTOs.StoryItemResponse result = storyItemService.update(1L, updateReq);

        assertThat(result.getId()).isEqualTo(1L);
        verify(auditService).log(eq("UPDATE"), any(StoryItem.class), eq(testItem));
    }

    @Test
    void testUpdate_notFound() {
        when(storyItemRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> storyItemService.update(99L, updateReq));
    }

    @Test
    void testDelete_success() {
        when(storyItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        doNothing().when(storyItemRepository).delete(testItem);

        storyItemService.delete(1L);

        verify(auditService).log(eq("DELETE"), any(StoryItem.class), isNull());
        verify(storyItemRepository).delete(testItem);
    }

    @Test
    void testDelete_notFound() {
        when(storyItemRepository.findById(100L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> storyItemService.delete(100L));
    }

    @Test
    void testExistsById_true() {
        when(storyItemRepository.existsById(1L)).thenReturn(true);
        assertTrue(storyItemService.existsById(1L));
    }

    @Test
    void testExistsById_false() {
        when(storyItemRepository.existsById(2L)).thenReturn(false);
        assertFalse(storyItemService.existsById(2L));
    }

    @Test
    void testSearch() {
        StoryItemSearchCriteria criteria = new StoryItemSearchCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        Page<StoryItemDTOs.StoryItemResponse> page = new PageImpl<>(List.of(responseDto));

        when(storyItemSearcher.search(criteria, pageable)).thenReturn(page);

        Page<StoryItemDTOs.StoryItemResponse> result = storyItemService.search(criteria, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
    }
}