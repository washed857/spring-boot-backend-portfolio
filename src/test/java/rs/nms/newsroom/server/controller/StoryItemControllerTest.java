package rs.nms.newsroom.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import rs.nms.newsroom.server.dto.StoryItemDTOs;
import rs.nms.newsroom.server.dto.StoryItemSearchCriteria;
import rs.nms.newsroom.server.service.StoryItemService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = StoryItemController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = rs.nms.newsroom.server.config.security.SecurityConfig.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = rs.nms.newsroom.server.config.security.JwtAuthenticationFilter.class)
        }
)
@AutoConfigureMockMvc(addFilters = false)
class StoryItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StoryItemService storyItemService;

    @Autowired
    private ObjectMapper objectMapper;

    private StoryItemDTOs.StoryItemResponse sampleResponse() {
        StoryItemDTOs.StoryItemResponse response = new StoryItemDTOs.StoryItemResponse();
        response.setId(111L);
        response.setTypeCode("PKG");
        response.setTypeDisplayName("Package");
        response.setStoryName("Story Title");
        response.setVideoName("video.mp4");
        response.setTextDescription("Sample Description");
        response.setCgMainTitle("CG Title");
        response.setCgSubtitle("CG Subtitle");
        response.setCgSpeakerName("Speaker");
        response.setDisplayOrder(2);
        response.setContent("Some script content");
        response.setSourcePath("/videos/video.mp4");
        response.setDurationSeconds(80);
        response.setTextDurationSeconds(75);
        response.setStoryDurationSeconds(95);
        response.setTimeSeconds(400);
        response.setOnAirTimeSeconds(420);
        response.setColor("blue");
        response.setStoryId(12L);
        response.setPresenterIds(List.of(5L, 6L));
        response.setReporterIds(List.of(7L));
        response.setLocationIds(List.of(3L, 4L));
        response.setCreatedAt(LocalDateTime.of(2025, 6, 20, 11, 15, 0));
        response.setUpdatedAt(LocalDateTime.of(2025, 6, 21, 14, 0, 0));
        return response;
    }

    @Nested
    class Create {
        @Test
        void createStoryItem_returnsCreatedItem() throws Exception {
            StoryItemDTOs.StoryItemCreateRequest request = new StoryItemDTOs.StoryItemCreateRequest();
            request.setStoryId(12L);
            request.setType("PKG");

            StoryItemDTOs.StoryItemResponse response = sampleResponse();
            Mockito.when(storyItemService.create(any())).thenReturn(response);

            mockMvc.perform(post("/api/story-items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(111L))
                    .andExpect(jsonPath("$.typeCode").value("PKG"));
        }
    }

    @Nested
    class Read {
        @Test
        void getById_returnsStoryItem() throws Exception {
            Mockito.when(storyItemService.getById(111L)).thenReturn(sampleResponse());

            mockMvc.perform(get("/api/story-items/111"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(111L));
        }

        @Test
        void getByStoryId_returnsList() throws Exception {
            Mockito.when(storyItemService.getByStoryId(12L)).thenReturn(List.of(sampleResponse()));

            mockMvc.perform(get("/api/story-items/by-story/12"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].storyId").value(12L));
        }
    }

    @Nested
    class Update {
        @Test
        void updateStoryItem_returnsUpdatedItem() throws Exception {
            StoryItemDTOs.StoryItemUpdateRequest updateRequest = new StoryItemDTOs.StoryItemUpdateRequest();
            updateRequest.setType("VO");
            updateRequest.setStoryName("Updated Name");

            StoryItemDTOs.StoryItemResponse updated = sampleResponse();
            updated.setTypeCode("VO");
            updated.setStoryName("Updated Name");

            Mockito.when(storyItemService.update(eq(111L), any())).thenReturn(updated);

            mockMvc.perform(put("/api/story-items/111")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.typeCode").value("VO"))
                    .andExpect(jsonPath("$.storyName").value("Updated Name"));
        }
    }

    @Nested
    class Search {
        @Test
        void search_returnsPagedResults() throws Exception {
            StoryItemSearchCriteria criteria = new StoryItemSearchCriteria();
            Page<StoryItemDTOs.StoryItemResponse> page = new PageImpl<>(
                    List.of(sampleResponse()),
                    PageRequest.of(0, 10),
                    1
            );
            Mockito.when(storyItemService.search(any(), any())).thenReturn(page);

            mockMvc.perform(post("/api/story-items/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(criteria))
                            .param("page", "0")
                            .param("size", "10")
                            .param("sort", "id,asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(111L));
        }
    }

    @Nested
    class Delete {
        @Test
        void deleteStoryItem_returnsOk() throws Exception {
            mockMvc.perform(delete("/api/story-items/111"))
                    .andExpect(status().isOk());
            Mockito.verify(storyItemService).delete(111L);
        }
    }
}