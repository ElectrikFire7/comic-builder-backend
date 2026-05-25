package com.fullcomicbuilder.backend.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Lightweight principal stored in the SecurityContext after JWT validation.
 */
@Getter
@AllArgsConstructor
public class AuthenticatedUser {
    private final String userId;
    private final String email;
}
