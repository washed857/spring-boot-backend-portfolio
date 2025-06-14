package rs.nms.newsroom.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import rs.nms.newsroom.server.config.security.JwtAuthenticationFilter;
import rs.nms.newsroom.server.config.security.JwtTokenUtil;
import rs.nms.newsroom.server.dto.PermissionDTOs;
import rs.nms.newsroom.server.service.PermissionService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = PermissionController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = { JwtAuthenticationFilter.class, JwtTokenUtil.class }
        )
)
@AutoConfigureMockMvc(addFilters = false)
class PermissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PermissionService permissionService;

    @Autowired
    private ObjectMapper objectMapper;

    private PermissionDTOs.PermissionResponse sampleResponse() {
        PermissionDTOs.PermissionResponse dto = new PermissionDTOs.PermissionResponse();
        dto.setId(1L);
        dto.setName("MEDIA_UPLOAD");
        dto.setDescription("Can upload media files");
        return dto;
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_shouldReturnCreatedPermission() throws Exception {
        PermissionDTOs.PermissionCreateRequest req = new PermissionDTOs.PermissionCreateRequest();
        req.setName("MEDIA_UPLOAD");
        req.setDescription("Can upload media files");
        PermissionDTOs.PermissionResponse resp = sampleResponse();

        when(permissionService.create(any(PermissionDTOs.PermissionCreateRequest.class)))
                .thenReturn(resp);

        mockMvc.perform(post("/api/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(resp.getId()))
                .andExpect(jsonPath("$.name").value("MEDIA_UPLOAD"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAll_shouldReturnList() throws Exception {
        when(permissionService.getAll()).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("MEDIA_UPLOAD"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getById_shouldReturnOne() throws Exception {
        when(permissionService.getById(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/permissions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("MEDIA_UPLOAD"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_shouldReturnUpdated() throws Exception {
        PermissionDTOs.PermissionUpdateRequest req = new PermissionDTOs.PermissionUpdateRequest();
        req.setName("MEDIA_UPDATE");
        req.setDescription("Update permission");

        PermissionDTOs.PermissionResponse updated = new PermissionDTOs.PermissionResponse();
        updated.setId(1L);
        updated.setName("MEDIA_UPDATE");
        updated.setDescription("Update permission");

        when(permissionService.update(any(Long.class), any(PermissionDTOs.PermissionUpdateRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/permissions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("MEDIA_UPDATE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_shouldReturnNoContent() throws Exception {
        doNothing().when(permissionService).delete(1L);

        mockMvc.perform(delete("/api/permissions/1"))
                .andExpect(status().isOk());
    }
}