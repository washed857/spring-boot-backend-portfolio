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
import rs.nms.newsroom.server.dto.ScheduleDTOs;
import rs.nms.newsroom.server.dto.ScheduleSearchCriteria;
import rs.nms.newsroom.server.dto.common.PagedResponse;
import rs.nms.newsroom.server.service.ScheduleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = ScheduleController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = rs.nms.newsroom.server.config.security.SecurityConfig.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = rs.nms.newsroom.server.config.security.JwtAuthenticationFilter.class)
        }
)
@AutoConfigureMockMvc(addFilters = false)
class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScheduleService scheduleService;

    @Autowired
    private ObjectMapper objectMapper;

    private ScheduleDTOs.ScheduleResponse sampleResponse() {
        ScheduleDTOs.ScheduleResponse response = new ScheduleDTOs.ScheduleResponse();
        response.setId(1L);
        response.setRundownId(99L);
        response.setStartTime(LocalDateTime.of(2025, 6, 20, 10, 0, 0));
        response.setEndTime(LocalDateTime.of(2025, 6, 20, 12, 0, 0));
        response.setChannel("Studio1");
        response.setStatus("PLANNED");
        return response;
    }

    @Nested
    @WithMockUser
    class CreateSchedule {
        @Test
        void createSchedule_returnsCreatedSchedule() throws Exception {
            ScheduleDTOs.ScheduleCreateRequest request = new ScheduleDTOs.ScheduleCreateRequest();
            request.setRundownId(99L);
            request.setStartTime(LocalDateTime.of(2025, 6, 20, 10, 0, 0));
            request.setEndTime(LocalDateTime.of(2025, 6, 20, 12, 0, 0));
            request.setChannel("Studio1");

            ScheduleDTOs.ScheduleResponse response = sampleResponse();
            Mockito.when(scheduleService.create(any())).thenReturn(response);

            mockMvc.perform(post("/api/schedules")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.rundownId").value(99L))
                    .andExpect(jsonPath("$.channel").value("Studio1"))
                    .andExpect(jsonPath("$.status").value("PLANNED"));
        }
    }

    @Nested
    @WithMockUser
    class GetSchedule {
        @Test
        void getById_returnsSchedule() throws Exception {
            ScheduleDTOs.ScheduleResponse response = sampleResponse();
            Mockito.when(scheduleService.getById(1L)).thenReturn(response);

            mockMvc.perform(get("/api/schedules/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L));
        }

        @Test
        void getAll_returnsList() throws Exception {
            Mockito.when(scheduleService.getAll()).thenReturn(List.of(sampleResponse()));

            mockMvc.perform(get("/api/schedules"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1L));
        }

        @Test
        void getByRundownId_returnsList() throws Exception {
            Mockito.when(scheduleService.getByRundownId(99L)).thenReturn(List.of(sampleResponse()));

            mockMvc.perform(get("/api/schedules/rundown/99"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].rundownId").value(99L));
        }

        @Test
        void getByChannel_returnsList() throws Exception {
            Mockito.when(scheduleService.getByChannel("Studio1")).thenReturn(List.of(sampleResponse()));

            mockMvc.perform(get("/api/schedules/channel/Studio1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].channel").value("Studio1"));
        }

        @Test
        void getByTimeRange_returnsList() throws Exception {
            Mockito.when(scheduleService.getByTimeRange(
                    any(LocalDateTime.class),
                    any(LocalDateTime.class)
            )).thenReturn(List.of(sampleResponse()));

            mockMvc.perform(get("/api/schedules/time-range")
                            .param("start", "2025-06-20 10:00:00")
                            .param("end", "2025-06-20 12:00:00"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1L));
        }

        @Test
        void getCurrentActive_returnsList() throws Exception {
            Mockito.when(scheduleService.getCurrentActive()).thenReturn(List.of(sampleResponse()));

            mockMvc.perform(get("/api/schedules/active"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1L));
        }

        @Test
        void getUpcoming_returnsList() throws Exception {
            Mockito.when(scheduleService.getUpcoming()).thenReturn(List.of(sampleResponse()));

            mockMvc.perform(get("/api/schedules/upcoming"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1L));
        }
    }

    @Nested
    @WithMockUser
    class SearchSchedule {
        @Test
        void searchSchedules_returnsPaged() throws Exception {
            ScheduleSearchCriteria criteria = new ScheduleSearchCriteria();
            Page<ScheduleDTOs.ScheduleResponse> page = new PageImpl<>(List.of(sampleResponse()), PageRequest.of(0, 10), 1);

            Mockito.when(scheduleService.searchSchedules(any(), any())).thenReturn(page);

            mockMvc.perform(post("/api/schedules/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(criteria)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(1L))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }
    }

    @Nested
    @WithMockUser
    class UpdateSchedule {
        @Test
        void updateSchedule_returnsUpdated() throws Exception {
            ScheduleDTOs.ScheduleUpdateRequest updateRequest = new ScheduleDTOs.ScheduleUpdateRequest();
            updateRequest.setChannel("Studio2");

            ScheduleDTOs.ScheduleResponse response = sampleResponse();
            response.setChannel("Studio2");

            Mockito.when(scheduleService.update(eq(1L), any())).thenReturn(response);

            mockMvc.perform(put("/api/schedules/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.channel").value("Studio2"));
        }
    }

    @Nested
    @WithMockUser
    class DeleteSchedule {
        @Test
        void deleteSchedule_returnsNoContent() throws Exception {
            mockMvc.perform(delete("/api/schedules/1"))
                    .andExpect(status().isNoContent());
            Mockito.verify(scheduleService).delete(1L);
        }
    }
}