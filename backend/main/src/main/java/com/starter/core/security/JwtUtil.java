package com.starter.core.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.starter.core.user.User;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;

/** Utility class for JWT token generation and validation. */
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtUtil(
        @Value("${jwt.secret:default-secret-key-for-development-only-change-in-production-32chars}") String secret,
        @Value("${jwt.expiration-ms:86400000}") long expirationMs
    ) {
        // Ensure key is at least 256 bits (32 bytes) for HS256
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 characters");
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    /** Generate JWT token for a user. */
    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant expiration = now.plusMillis(expirationMs);

        return Jwts.builder()
            .subject(user.getId().toString())
            .claim("email", user.getEmail())
            .claim("role", user.getRole().name())
            .claim("emailVerified", user.isEmailVerified())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .signWith(secretKey)
            .compact();
    }

    /** Validate token and return claims. Returns null if invalid. */
    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    /** Extract user ID from token. Returns null if invalid. */
    public Long getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        if (claims == null) {
            return null;
        }
        try {
            return Long.parseLong(claims.getSubject());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Extract email from token. Returns null if invalid. */
    public String getEmailFromToken(String token) {
        Claims claims = validateToken(token);
        if (claims == null) {
            return null;
        }
        return claims.get("email", String.class);
    }

    /** Extract role from token. Returns null if invalid. */
    public User.Role getRoleFromToken(String token) {
        Claims claims = validateToken(token);
        if (claims == null) {
            return null;
        }
        return User.Role.valueOf(claims.get("role", String.class));
    }

    /** Check if token is valid (not expired, correct signature). */
    public boolean isTokenValid(String token) {
        return validateToken(token) != null;
    }
}
