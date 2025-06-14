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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import rs.nms.newsroom.server.dto.StoryDTOs;
import rs.nms.newsroom.server.service.StorySearchService;
import rs.nms.newsroom.server.service.StoryService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = StoryController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = rs.nms.newsroom.server.config.security.SecurityConfig.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = rs.nms.newsroom.server.config.security.JwtAuthenticationFilter.class)
        }
)
@AutoConfigureMockMvc(addFilters = false)
class StoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StoryService storyService;

    @MockBean
    private StorySearchService storySearchService;

    @Autowired
    private ObjectMapper objectMapper;

    private StoryDTOs.StoryResponse sampleResponse() {
        StoryDTOs.StoryResponse response = new StoryDTOs.StoryResponse();
        response.setId(10L);
        response.setTitle("Sample story");
        response.setStatus("DRAFT");
        response.setAuthorId(1L);
        response.setAuthorName("John Doe");
        response.setRundownId(77L);
        response.setRundownTitle("Morning Rundown");
        response.setStoryItemIds(Set.of(100L, 101L));
        response.setApprovedBy(null);
        response.setCreatedAt(LocalDateTime.of(2025, 6, 15, 10, 0, 0));
        response.setStoryTypeId(2L);
        response.setStoryTypeName("Live");
        return response;
    }

    @Nested
    @WithMockUser(roles = {"JOURNALIST"})
    class Create {
        @Test
        void createStory_returnsCreatedStory() throws Exception {
            StoryDTOs.StoryCreateRequest request = new StoryDTOs.StoryCreateRequest();
            request.setTitle("Sample story");
            request.setAuthorId(1L);

            StoryDTOs.StoryResponse response = sampleResponse();
            Mockito.when(storyService.create(any())).thenReturn(response);

            mockMvc.perform(post("/stories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10L))
                    .andExpect(jsonPath("$.title").value("Sample story"));
        }
    }

    @Nested
    @WithMockUser
    class Read {
        @Test
        void getById_returnsStory() throws Exception {
            Mockito.when(storyService.getById(10L)).thenReturn(sampleResponse());

            mockMvc.perform(get("/stories/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10L));
        }

        @Test
        void getByRundownId_returnsList() throws Exception {
            Mockito.when(storyService.getByRundownId(77L)).thenReturn(List.of(sampleResponse()));

            mockMvc.perform(get("/stories/by-rundown/77"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].rundownId").value(77L));
        }

        @Test
        void getByAuthorId_returnsList() throws Exception {
            Mockito.when(storyService.getByAuthorId(1L)).thenReturn(List.of(sampleResponse()));

            mockMvc.perform(get("/stories/by-author/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].authorId").value(1L));
        }
    }

    @Nested
    @WithMockUser(authorities = {"story.read"})
    class Search {
        @Test
        void searchStories_returnsPagedResult() throws Exception {
            Mockito.when(storySearchService.search(any(), any(), any(), any(), any()))
                    .thenReturn(new PageImpl<>(List.of(sampleResponse()), PageRequest.of(0, 10), 1));

            mockMvc.perform(get("/stories/search")
                            .param("query", "Sample")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(10L));
        }
    }

    @Nested
    @WithMockUser(roles = {"EDITOR"})
    class UpdateAndDelete {
        @Test
        void updateStory_returnsUpdatedStory() throws Exception {
            StoryDTOs.StoryUpdateRequest updateRequest = new StoryDTOs.StoryUpdateRequest();
            updateRequest.setTitle("Updated story");

            StoryDTOs.StoryResponse updated = sampleResponse();
            updated.setTitle("Updated story");

            Mockito.when(storyService.update(eq(10L), any())).thenReturn(updated);

            mockMvc.perform(put("/stories/10")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Updated story"));
        }

        @Test
        void deleteStory_returnsNoContent() throws Exception {
            mockMvc.perform(delete("/stories/10"))
                    .andExpect(status().isNoContent());
            Mockito.verify(storyService).delete(10L);
        }
    }

    @Nested
    @WithMockUser(roles = {"EDITOR"})
    class Workflow {
        @Test
        void publish_returnsPublishedStory() throws Exception {
            StoryDTOs.StoryResponse published = sampleResponse();
            published.setStatus("PUBLISHED");

            Mockito.when(storyService.publish(10L, "editorUser")).thenReturn(published);

            mockMvc.perform(put("/stories/10/publish")
                            .param("approvedBy", "editorUser"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("PUBLISHED"));
        }

        @Test
        void reject_returnsRejectedStory() throws Exception {
            StoryDTOs.StoryResponse rejected = sampleResponse();
            rejected.setStatus("REJECTED");

            Mockito.when(storyService.reject(10L, "editorUser")).thenReturn(rejected);

            mockMvc.perform(put("/stories/10/reject")
                            .param("rejectedBy", "editorUser"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("REJECTED"));
        }

        @Test
        void archive_returnsArchivedStory() throws Exception {
            StoryDTOs.StoryResponse archived = sampleResponse();
            archived.setStatus("ARCHIVED");

            Mockito.when(storyService.archive(10L, "editorUser")).thenReturn(archived);

            mockMvc.perform(put("/stories/10/archive")
                            .param("archivedBy", "editorUser"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ARCHIVED"));
        }
    }

    @Nested
    @WithMockUser(authorities = {"story.lock"})
    class Locking {
        @Test
        void lockStory_returnsLocked() throws Exception {
            StoryDTOs.StoryResponse locked = sampleResponse();

            Mockito.when(storyService.lock(10L)).thenReturn(locked);

            mockMvc.perform(put("/stories/10/lock"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10L));
        }

        @Test
        void unlockStory_returnsUnlocked() throws Exception {
            StoryDTOs.StoryResponse unlocked = sampleResponse();

            Mockito.when(storyService.unlock(10L)).thenReturn(unlocked);

            mockMvc.perform(put("/stories/10/unlock"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10L));
        }
    }
}