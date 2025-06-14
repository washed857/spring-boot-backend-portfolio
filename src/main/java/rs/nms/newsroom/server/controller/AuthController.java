package rs.nms.newsroom.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.nms.newsroom.server.dto.AuthDTOs;
import rs.nms.newsroom.server.service.AuthService;

/**
 * REST controller for authentication and token management.
 * Handles login, refresh, and logout operations for API clients.
 */
@Tag(name = "Auth Controller", description = "Authentication and token management endpoints.")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(
        summary = "User login",
        description = "Authenticates a user and returns JWT access and refresh tokens."
    )
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody AuthDTOs.LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.authenticateUser(loginRequest));
    }

    @PostMapping("/refresh-token")
    @Operation(
        summary = "Refresh JWT token",
        description = "Refreshes the access token using a valid refresh token."
    )
    public ResponseEntity<?> refreshToken(@Valid @RequestBody AuthDTOs.RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/logout")
    @Operation(
        summary = "Logout user",
        description = "Logs out a user by invalidating the provided refresh token."
    )
    public ResponseEntity<?> logout(@Valid @RequestBody AuthDTOs.LogoutRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok().build();
    }
}