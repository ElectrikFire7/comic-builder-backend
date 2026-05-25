package com.fullcomicbuilder.backend.controller;

import com.fullcomicbuilder.backend.dto.request.GoogleOAuthRequest;
import com.fullcomicbuilder.backend.dto.response.AuthResponse;
import com.fullcomicbuilder.backend.dto.response.UserResponse;
import com.fullcomicbuilder.backend.model.User;
import com.fullcomicbuilder.backend.security.AuthenticatedUser;
import com.fullcomicbuilder.backend.service.AuthService;
import com.fullcomicbuilder.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication endpoints.
 * Handles Google OAuth login and user profile retrieval.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    /**
     * Handles Google OAuth login.
     * Accepts a Google ID token, verifies it server-side, and returns a JWT token.
     * 
     * @param request GoogleOAuthRequest containing the Google ID token credential
     * @return ResponseEntity with AuthResponse containing JWT and user data
     */
    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(@Valid @RequestBody GoogleOAuthRequest request) {
        return ResponseEntity.ok(authService.googleLogin(request));
    }

    /**
     * Returns the current authenticated user's profile.
     * 
     * @param principal The authenticated user principal from JWT
     * @return ResponseEntity with user profile data
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal AuthenticatedUser principal) {
        User user = userService.findById(principal.getUserId());
        return ResponseEntity.ok(UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .activeApiKeyId(user.getActiveApiKeyId())
                .build());
    }
}
