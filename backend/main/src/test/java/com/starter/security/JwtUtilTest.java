package com.starter.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.starter.domain.User;

import java.time.Instant;

/** Unit tests for JwtUtil. */
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 32+ character secret for HS256
        String secret = "test-secret-key-for-unit-tests-min-32-characters";
        long expirationMs = 3600000; // 1 hour
        jwtUtil = new JwtUtil(secret, expirationMs);

        testUser =
            User.builder()
                .id(1L)
                .email("test@example.com")
                .password("hashedPassword")
                .role(User.Role.USER)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void generateToken_shouldCreateValidToken() {
        // when
        String token = jwtUtil.generateToken(testUser);

        // then
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }

    @Test
    void validateToken_shouldReturnClaimsForValidToken() {
        // given
        String token = jwtUtil.generateToken(testUser);

        // when
        var claims = jwtUtil.validateToken(token);

        // then
        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.get("email", String.class)).isEqualTo("test@example.com");
        assertThat(claims.get("role", String.class)).isEqualTo("USER");
    }

    @Test
    void validateToken_shouldReturnNullForInvalidToken() {
        // when
        var claims = jwtUtil.validateToken("invalid.token.here");

        // then
        assertThat(claims).isNull();
    }

    @Test
    void validateToken_shouldReturnNullForTamperedToken() {
        // given
        String token = jwtUtil.generateToken(testUser);
        String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";

        // when
        var claims = jwtUtil.validateToken(tamperedToken);

        // then
        assertThat(claims).isNull();
    }

    @Test
    void getUserIdFromToken_shouldReturnUserId() {
        // given
        String token = jwtUtil.generateToken(testUser);

        // when
        Long userId = jwtUtil.getUserIdFromToken(token);

        // then
        assertThat(userId).isEqualTo(1L);
    }

    @Test
    void getEmailFromToken_shouldReturnEmail() {
        // given
        String token = jwtUtil.generateToken(testUser);

        // when
        String email = jwtUtil.getEmailFromToken(token);

        // then
        assertThat(email).isEqualTo("test@example.com");
    }

    @Test
    void getRoleFromToken_shouldReturnRole() {
        // given
        String token = jwtUtil.generateToken(testUser);

        // when
        User.Role role = jwtUtil.getRoleFromToken(token);

        // then
        assertThat(role).isEqualTo(User.Role.USER);
    }

    @Test
    void isTokenValid_shouldReturnTrueForValidToken() {
        // given
        String token = jwtUtil.generateToken(testUser);

        // when & then
        assertThat(jwtUtil.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_shouldReturnFalseForInvalidToken() {
        // when & then
        assertThat(jwtUtil.isTokenValid("invalid.token")).isFalse();
    }

    @Test
    void constructor_shouldThrowExceptionForShortSecret() {
        // when & then
        assertThatThrownBy(() -> new JwtUtil("short", 3600000))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("32 characters");
    }

    @Test
    void generateToken_shouldWorkForAdminRole() {
        // given
        User adminUser =
            User.builder()
                .id(2L)
                .email("admin@example.com")
                .password("hashedPassword")
                .role(User.Role.ADMIN)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        // when
        String token = jwtUtil.generateToken(adminUser);
        User.Role role = jwtUtil.getRoleFromToken(token);

        // then
        assertThat(role).isEqualTo(User.Role.ADMIN);
    }
}
