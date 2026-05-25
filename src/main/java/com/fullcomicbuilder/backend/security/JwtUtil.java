package com.fullcomicbuilder.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Utility component for JWT token generation and validation.
 * Handles encoding, decoding, and verification of JWT tokens.
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expiration;

    public JwtUtil(@Value("${app.jwt.secret}") String secret,
                   @Value("${app.jwt.expiration-ms}") long expiration) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expiration = expiration;
    }

    /**
     * Generates a JWT token for the given user ID.
     * 
     * @param userId    The user's ID (subject)
     * @param email     The user's email
     * @param username  The user's username
     * @return JWT token string
     */
    public String generateToken(String userId, String email, String username) {
        return Jwts.builder()
                .subject(userId)
                .claim("email", email)
                .claim("username", username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    /**
     * Extracts the user ID (subject) from the token.
     * 
     * @param token JWT token
     * @return User ID
     */
    public String extractUserId(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Extracts the email claim from the token.
     * 
     * @param token JWT token
     * @return Email address
     */
    public String extractEmail(String token) {
        return extractClaims(token).get("email", String.class);
    }

    /**
     * Validates if the token is valid and not expired.
     * 
     * @param token JWT token
     * @return true if token is valid, false otherwise
     */
    public boolean isTokenValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Extracts all claims from the token.
     * 
     * @param token JWT token
     * @return Claims object
     * @throws JwtException if token is invalid or expired
     */
    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
