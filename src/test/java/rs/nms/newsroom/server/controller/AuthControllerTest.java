package rs.nms.newsroom.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import rs.nms.newsroom.server.dto.AuthDTOs;
import rs.nms.newsroom.server.service.AuthService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
	    controllers = AuthController.class,
	    excludeAutoConfiguration = {
	        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class
	    }
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private rs.nms.newsroom.server.config.security.JwtTokenUtil jwtTokenUtil;


    @Test
    @DisplayName("POST /api/auth/login - should return AuthResponse")
    void authenticateUser_success() throws Exception {
        AuthDTOs.LoginRequest loginRequest = new AuthDTOs.LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("pass123");

        AuthDTOs.AuthResponse authResponse = new AuthDTOs.AuthResponse(
                10L,
                "accessTokenSample",
                "refreshTokenSample",
                "testuser",
                "Test User",
                "ADMIN"
        );

        when(authService.authenticateUser(any(AuthDTOs.LoginRequest.class)))
                .thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_id").value(10))
                .andExpect(jsonPath("$.accessToken").value("accessTokenSample"))
                .andExpect(jsonPath("$.refreshToken").value("refreshTokenSample"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.fullName").value("Test User"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("POST /api/auth/refresh-token - should return RefreshTokenResponse")
    void refreshToken_success() throws Exception {
        AuthDTOs.RefreshTokenRequest refreshRequest = new AuthDTOs.RefreshTokenRequest();
        refreshRequest.setRefreshToken("refreshTokenSample");

        AuthDTOs.RefreshTokenResponse refreshResponse = new AuthDTOs.RefreshTokenResponse(
                "newAccessToken", "refreshTokenSample"
        );

        when(authService.refreshToken(any(AuthDTOs.RefreshTokenRequest.class)))
                .thenReturn(refreshResponse);

        mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("newAccessToken"))
                .andExpect(jsonPath("$.refreshToken").value("refreshTokenSample"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("POST /api/auth/logout - should return 200 OK")
    void logout_success() throws Exception {
        AuthDTOs.LogoutRequest logoutRequest = new AuthDTOs.LogoutRequest();
        logoutRequest.setRefreshToken("refreshTokenSample");

        doNothing().when(authService).logout(anyString());

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/auth/login - validation fail should return 400")
    void authenticateUser_invalidRequest() throws Exception {
        AuthDTOs.LoginRequest loginRequest = new AuthDTOs.LoginRequest();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
		        .andExpect(status().isBadRequest())
		        .andExpect(jsonPath("$.fieldErrors").exists())
		        .andExpect(jsonPath("$.fieldErrors.username").value("must not be blank"))
		        .andExpect(jsonPath("$.fieldErrors.password").value("must not be blank"));

    }
}