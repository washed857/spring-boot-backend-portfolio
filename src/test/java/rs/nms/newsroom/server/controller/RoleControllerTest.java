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
import rs.nms.newsroom.server.dto.RoleDTOs;
import rs.nms.newsroom.server.service.RoleService;
import static org.hamcrest.Matchers.hasItems;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = RoleController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = { JwtAuthenticationFilter.class, JwtTokenUtil.class }
        )
)
@AutoConfigureMockMvc(addFilters = false)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoleService roleService;

    @Autowired
    private ObjectMapper objectMapper;

    // Helper for full response
    private RoleDTOs.RoleResponse buildRoleResponse() {
        RoleDTOs.RoleResponse resp = new RoleDTOs.RoleResponse();
        resp.setId(1L);
        resp.setName("ADMIN");
        resp.setPermissions(Set.of("USER_MANAGE", "PERMISSION_MANAGE"));
        return resp;
    }

    // Helper for summary
    private RoleDTOs.RoleSummary buildRoleSummary() {
        RoleDTOs.RoleSummary summary = new RoleDTOs.RoleSummary();
        summary.setId(1L);
        summary.setName("ADMIN");
        return summary;
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_shouldReturnRoleResponse() throws Exception {
        RoleDTOs.RoleCreateRequest req = new RoleDTOs.RoleCreateRequest();
        req.setName("ADMIN");
        req.setPermissionIds(Set.of(1L, 2L));

        RoleDTOs.RoleResponse resp = buildRoleResponse();

        when(roleService.create(any(RoleDTOs.RoleCreateRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("ADMIN"))
                .andExpect(jsonPath("$.permissions").isArray())
                .andExpect(jsonPath("$.permissions", hasItems("USER_MANAGE", "PERMISSION_MANAGE")));
    }

    @Test
    @WithMockUser(authorities = "VIEW_ROLE")
    void getAll_shouldReturnListOfSummaries() throws Exception {
        when(roleService.getAllSummaries()).thenReturn(List.of(buildRoleSummary()));

        mockMvc.perform(get("/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("ADMIN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getById_shouldReturnRoleDetails() throws Exception {
        when(roleService.getById(1L)).thenReturn(buildRoleResponse());

        mockMvc.perform(get("/roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("ADMIN"))
                .andExpect(jsonPath("$.permissions").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_shouldReturnUpdatedRole() throws Exception {
        RoleDTOs.RoleUpdateRequest req = new RoleDTOs.RoleUpdateRequest();
        req.setName("SUPER_ADMIN");
        req.setPermissionIds(Set.of(3L));

        RoleDTOs.RoleResponse updated = new RoleDTOs.RoleResponse();
        updated.setId(1L);
        updated.setName("SUPER_ADMIN");
        updated.setPermissions(Set.of("ALL_ACCESS"));

        when(roleService.update(eq(1L), any(RoleDTOs.RoleUpdateRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/roles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("SUPER_ADMIN"))
                .andExpect(jsonPath("$.permissions[0]").value("ALL_ACCESS"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_shouldReturnOk() throws Exception {
        doNothing().when(roleService).delete(1L);

        mockMvc.perform(delete("/roles/1"))
                .andExpect(status().isOk());
    }
}