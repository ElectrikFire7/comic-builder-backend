package com.fullcomicbuilder.backend.service;

import com.fullcomicbuilder.backend.dto.request.GoogleOAuthRequest;
import com.fullcomicbuilder.backend.dto.response.AuthResponse;
import com.fullcomicbuilder.backend.dto.response.UserResponse;
import com.fullcomicbuilder.backend.model.User;
import com.fullcomicbuilder.backend.security.JwtUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Service for handling OAuth authentication and JWT token generation.
 * Manages Google OAuth token verification and user creation/retrieval.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Value("${app.google.client-id}")
    private String googleClientId;

    /**
     * Handles Google OAuth login flow.
     * Verifies the Google ID token, creates/retrieves the user, and generates a JWT token.
     * 
     * @param request GoogleOAuthRequest containing the Google ID token credential
     * @return AuthResponse with JWT token and user data
     */
    public AuthResponse googleLogin(GoogleOAuthRequest request) {
        // Verify Google token
        GoogleIdToken.Payload payload = verifyGoogleToken(request.getCredential());

        String email = payload.getEmail();
        String name = (String) payload.get("name");

        // Get or create user in database
        User user = userService.getOrCreateUser(email, name);

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getUsername());

        // Build and return response
        return AuthResponse.builder()
                .token(token)
                .user(UserResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .activeApiKeyId(user.getActiveApiKeyId())
                        .build())
                .build();
    }

    /**
     * Verifies a Google ID token using Google's API.
     * Ensures the token is valid and was signed by Google.
     * 
     * @param credential The Google ID token credential from the client
     * @return GoogleIdToken.Payload containing verified claims
     * @throws RuntimeException if token verification fails
     */
    private GoogleIdToken.Payload verifyGoogleToken(String credential) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(credential);
            if (idToken == null) {
                throw new RuntimeException("Invalid Google token");
            }
            return idToken.getPayload();
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify Google token: " + e.getMessage());
        }
    }
}
