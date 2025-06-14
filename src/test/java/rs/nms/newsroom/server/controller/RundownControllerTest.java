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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import rs.nms.newsroom.server.dto.RundownDTOs;
import rs.nms.newsroom.server.service.RundownService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
	    controllers = RundownController.class,
	    excludeFilters = {
	        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = rs.nms.newsroom.server.config.security.SecurityConfig.class),
	        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = rs.nms.newsroom.server.config.security.JwtAuthenticationFilter.class)
	}
)
@AutoConfigureMockMvc(addFilters = false)
class RundownControllerTest { 

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RundownService rundownService;

    @Autowired
    private ObjectMapper objectMapper;

    private RundownDTOs.RundownResponse sampleResponse() {
        RundownDTOs.ShowDTO show = new RundownDTOs.ShowDTO();
        show.setId(42L);
        show.setName("Daily Show");
        show.setDescription("Description");
        show.setActive(true);

        RundownDTOs.RundownResponse response = new RundownDTOs.RundownResponse();
        response.setId(1L);
        response.setTitle("Morning Rundown");
        response.setShow(show);
        response.setBroadcastDate(LocalDate.of(2025, 6, 15));
        response.setAuthor("John Smith");
        response.setCreatedAt(LocalDateTime.of(2025, 6, 12, 8, 30));
        response.setStoryIds(Set.of(11L, 12L));
        return response;
    }

    @Nested
    @WithMockUser(roles = {"EDITOR"})
    class CreateRundown {
        @Test
        void createRundown_returnsCreatedRundown() throws Exception {
            RundownDTOs.RundownCreateRequest request = new RundownDTOs.RundownCreateRequest();
            request.setTitle("Morning Rundown");
            request.setShowId(42L);
            request.setBroadcastDate(LocalDate.of(2025, 6, 15));
            request.setAuthor("John Smith");
            request.setExternalId(null);

            RundownDTOs.RundownResponse response = sampleResponse();

            Mockito.when(rundownService.create(any())).thenReturn(response);

            mockMvc.perform(post("/rundowns")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(response.getId()))
                    .andExpect(jsonPath("$.title").value(response.getTitle()))
                    .andExpect(jsonPath("$.show.id").value(response.getShow().getId()))
                    .andExpect(jsonPath("$.broadcastDate").value("2025-06-15"))
                    .andExpect(jsonPath("$.author").value(response.getAuthor()));
        }
    }

    @Nested
    @WithMockUser
    class GetRundown {
        @Test
        void getRundownById_returnsRundown() throws Exception {
            RundownDTOs.RundownResponse response = sampleResponse();
            Mockito.when(rundownService.getById(1L)).thenReturn(response);

            mockMvc.perform(get("/rundowns/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.title").value("Morning Rundown"));
        }

        @Test
        void getAllRundowns_returnsList() throws Exception {
            RundownDTOs.RundownResponse response = sampleResponse();
            Mockito.when(rundownService.getAll()).thenReturn(List.of(response));

            mockMvc.perform(get("/rundowns"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1L));
        }

        @Test
        void getRundownsByDate_returnsFiltered() throws Exception {
            RundownDTOs.RundownResponse response = sampleResponse();
            Mockito.when(rundownService.getByDate(LocalDate.of(2025, 6, 15))).thenReturn(List.of(response));

            mockMvc.perform(get("/rundowns/by-date")
                            .param("date", "2025-06-15"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].broadcastDate").value("2025-06-15"));
        }
    }

    @Nested
    @WithMockUser(roles = {"EDITOR"})
    class FilterRundown {
        @Test
        void filterRundowns_returnsMatching() throws Exception {
            RundownDTOs.RundownFilterRequest filter = new RundownDTOs.RundownFilterRequest();
            filter.setTitle("Morning");
            filter.setShowId(42L);
            filter.setShowName("Daily Show");
            filter.setStartDate(LocalDate.of(2025, 6, 1));
            filter.setEndDate(LocalDate.of(2025, 6, 30));

            RundownDTOs.RundownResponse response = sampleResponse();
            Mockito.when(rundownService.filter(any())).thenReturn(List.of(response));

            mockMvc.perform(post("/rundowns/filter")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(filter)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].show.name").value("Daily Show"));
        }
    }

    @Nested
    @WithMockUser(roles = {"EDITOR"})
    class UpdateRundown {
        @Test
        void updateRundown_returnsUpdated() throws Exception {
            RundownDTOs.RundownUpdateRequest update = new RundownDTOs.RundownUpdateRequest();
            update.setTitle("Evening Rundown");
            update.setShowId(42L);
            update.setBroadcastDate(LocalDate.of(2025, 6, 16));
            update.setAuthor("Jane Doe");

            RundownDTOs.RundownResponse response = sampleResponse();
            response.setTitle("Evening Rundown");
            response.setBroadcastDate(LocalDate.of(2025, 6, 16));
            response.setAuthor("Jane Doe");

            Mockito.when(rundownService.update(eq(1L), any())).thenReturn(response);

            mockMvc.perform(put("/rundowns/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(update)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Evening Rundown"))
                    .andExpect(jsonPath("$.broadcastDate").value("2025-06-16"))
                    .andExpect(jsonPath("$.author").value("Jane Doe"));
        }
    }

    @Nested
    @WithMockUser(roles = {"ADMIN"})
    class DeleteRundown {
        @Test
        void deleteRundown_returnsNoContent() throws Exception {
            mockMvc.perform(delete("/rundowns/1"))
                    .andExpect(status().isNoContent());
            Mockito.verify(rundownService).delete(1L);
        }
    }

    @Nested
    @WithMockUser(roles = {"EDITOR"})
    class LockRundown {
        @Test
        void setLockStatus_returnsNoContent() throws Exception {
            RundownDTOs.LockRequest lockRequest = new RundownDTOs.LockRequest();
            lockRequest.setLocked(true);

            mockMvc.perform(patch("/rundowns/1/lock")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(lockRequest)))
                    .andExpect(status().isNoContent());
            Mockito.verify(rundownService).setLockStatus(1L, true);
        }
    }
}
