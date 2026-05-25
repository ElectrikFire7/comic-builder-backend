package com.fullcomicbuilder.backend.controller;

import com.fullcomicbuilder.backend.security.AuthenticatedUser;
import com.fullcomicbuilder.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Deletes the authenticated user and cascades to:
     * - all API keys in DB
     * - all projects in DB
     * - entire GCS user folder
     */
    @DeleteMapping("/me")
    public ResponseEntity<Map<String, String>> deleteMe(
            @AuthenticationPrincipal AuthenticatedUser principal) {
        userService.deleteUser(principal.getUserId());
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }
}
