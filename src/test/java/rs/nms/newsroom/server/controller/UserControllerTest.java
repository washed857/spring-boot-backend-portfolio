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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import rs.nms.newsroom.server.config.security.CustomUserDetails;
import rs.nms.newsroom.server.dto.UserDTOs;
import rs.nms.newsroom.server.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;


@WebMvcTest(
    controllers = UserController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "rs\\.nms\\.newsroom\\.server\\.config\\.security\\..*"
    )
)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDTOs.UserResponse sampleResponse() {
        UserDTOs.UserResponse response = new UserDTOs.UserResponse();
        response.setId(1L);
        response.setUsername("uros");
        response.setFullName("Uros Markovic");
        response.setEmail("uros@example.com");
        response.setRole("ADMIN");
        response.setRolePermissions(Set.of("CREATE_USER", "VIEW_USER"));
        response.setStatus("ACTIVE");
        response.setActiveChatCount(2);
        response.setHasPassword(true);
        response.setCreatedAt(LocalDateTime.now().minusDays(5));
        response.setUpdatedAt(LocalDateTime.now());
        response.setProfileImagePath("/img/profile1.png");
        response.setLastLoginAt(LocalDateTime.now().minusHours(1));
        response.setClientId(12L);
        response.setClientName("Test Client");
        return response;
    }

    @Nested
    class Create {

        @Test
        void createUser_returnsUser() throws Exception {
            UserDTOs.UserCreateRequest req = new UserDTOs.UserCreateRequest();
            req.setUsername("uros");
            req.setFullName("Uros Ilic");
            req.setEmail("uros@example.com");
            req.setPassword("testpass123");
            req.setRoleId(2L);
            req.setClientId(12L);

            Mockito.when(userService.create(any())).thenReturn(sampleResponse());

            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("uros"))
                    .andExpect(jsonPath("$.role").value("ADMIN"));
        }
    }

    @Nested
    class Read {

        @Test
        void getById_returnsUser() throws Exception {
            Mockito.when(userService.getById(1L)).thenReturn(sampleResponse());
            mockMvc.perform(get("/users/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.username").value("uros"));
        }

        @Test
        void getAll_returnsList() throws Exception {
            Mockito.when(userService.getAll()).thenReturn(List.of(sampleResponse()));
            mockMvc.perform(get("/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].username").value("uros"));
        }
    }

    @Nested
    class Search {

        @Test
        void searchUsers_returnsPagedResponse() throws Exception {
            UserDTOs.UserSearchCriteria criteria = new UserDTOs.UserSearchCriteria();
            criteria.setQuery("uro");
            criteria.setStatus("ACTIVE");
            criteria.setRole("ADMIN");
            Page<UserDTOs.UserResponse> page = new PageImpl<>(List.of(sampleResponse()), PageRequest.of(0, 10), 1);

            Mockito.when(userService.searchUsers(any(), any())).thenReturn(page);

            mockMvc.perform(post("/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(criteria))
                            .param("page", "0")
                            .param("size", "10")
                            .param("sort", "id,asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].username").value("uros"));
        }
    }

    @Nested
    class Update {

        @Test
        void updateUser_returnsUser() throws Exception {
            UserDTOs.UserUpdateRequest req = new UserDTOs.UserUpdateRequest();
            req.setFullName("Uros Updated");
            req.setEmail("new@example.com");
            req.setPassword("strongpass");

            UserDTOs.UserResponse updated = sampleResponse();
            updated.setFullName("Uros Updated");
            updated.setEmail("new@example.com");

            Mockito.when(userService.update(eq(1L), any())).thenReturn(updated);

            mockMvc.perform(put("/users/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fullName").value("Uros Updated"))
                    .andExpect(jsonPath("$.email").value("new@example.com"));
        }
    }

    @Nested
    class Delete {

        @Test
        void deleteUser_returnsNoContent() throws Exception {
            mockMvc.perform(delete("/users/1"))
                    .andExpect(status().isNoContent());
            Mockito.verify(userService).delete(1L);
        }
    }

    @Nested
    class StatusAndPassword {

        @Test
        void changeStatus_ok() throws Exception {
            mockMvc.perform(patch("/users/1/status")
                            .param("status", "INACTIVE"))
                    .andExpect(status().isOk());
            Mockito.verify(userService).changeStatus(1L, "INACTIVE");
        }

        @Test
        void changePassword_ok() throws Exception {
            UserDTOs.ChangePasswordRequest req = new UserDTOs.ChangePasswordRequest();
            req.setNewPassword("newStrongPass2024");

            mockMvc.perform(patch("/users/1/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk());
            Mockito.verify(userService).changePassword(eq(1L), any());
        }
    }

    @Nested
    class StatsAndLogs {

        @Test
        void getUserStats_ok() throws Exception {
            UserDTOs.UserStatistics stats = new UserDTOs.UserStatistics();
            stats.setTotal(10);
            stats.setActive(8);
            stats.setInactive(2);
            stats.setAdmins(2);
            stats.setLastRegistered(LocalDateTime.now());

            Mockito.when(userService.getUserStatistics()).thenReturn(stats);

            mockMvc.perform(get("/users/stats"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total").value(10));
        }

        @Test
        void getUserLogs_ok() throws Exception {
            UserDTOs.UserLogEntry entry = new UserDTOs.UserLogEntry();
            entry.setTimestamp(LocalDateTime.now().minusDays(1));
            entry.setOperation("CREATE");
            entry.setPerformedBy("User ID: 10");

            Mockito.when(userService.getUserLog(1L)).thenReturn(List.of(entry));

            mockMvc.perform(get("/users/1/logs"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].operation").value("CREATE"));
        }
    }

    @Nested
    class ProfileImage {

        @Test
        void uploadProfileImage_ok() throws Exception {
            MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "image-content".getBytes());
            UserDTOs.UserResponse response = sampleResponse();
            response.setProfileImagePath("/img/avatar.png");

            Mockito.when(userService.updateProfileImage(eq(1L), any())).thenReturn(response);

            mockMvc.perform(multipart("/users/1/profile-image")
                            .file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.profileImagePath").value("/img/avatar.png"));
        }
    }
}