package com.starter.core.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.starter.core.auth.dto.AuthResponse;
import com.starter.core.auth.dto.LoginRequest;
import com.starter.core.auth.dto.RegisterRequest;
import com.starter.core.email.EmailService;
import com.starter.core.exception.InvalidCredentialsException;
import com.starter.core.security.JwtUtil;
import com.starter.core.user.User;
import com.starter.core.user.UserRepository;
import com.starter.core.user.UserService;

import java.time.Instant;
import java.util.Optional;

/** Unit tests for AuthService. */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private EmailVerificationService emailVerificationService;

    @Mock
    private EmailService emailService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService =
            new AuthService(
                userService,
                userRepository,
                passwordEncoder,
                jwtUtil,
                emailVerificationService,
                emailService
            );
    }

    @Test
    void register_shouldCreateUserAndReturnToken() {
        // given
        RegisterRequest request =
            RegisterRequest.builder().email("new@example.com").password("password123").build();

        Instant now = Instant.now();
        User savedUser =
            User.builder()
                .id(1L)
                .email("new@example.com")
                .password("hashedPassword")
                .role(User.Role.USER)
                .emailVerified(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userService.createUser("new@example.com", "hashedPassword", User.Role.USER))
            .thenReturn(savedUser);
        when(jwtUtil.generateToken(savedUser)).thenReturn("jwt-token");

        // when
        AuthResponse response = authService.register(request);

        // then
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("new@example.com");
        assertThat(response.getRole()).isEqualTo("USER");
        assertThat(response.isEmailVerified()).isFalse();

        verify(passwordEncoder).encode("password123");
        verify(userService).createUser("new@example.com", "hashedPassword", User.Role.USER);
        verify(emailVerificationService).sendVerificationEmail(savedUser);
        verify(jwtUtil).generateToken(savedUser);
    }

    @Test
    void login_shouldReturnTokenForValidCredentials() {
        // given
        LoginRequest request =
            LoginRequest.builder().email("user@example.com").password("password123").build();

        Instant now = Instant.now();
        User user =
            User.builder()
                .id(1L)
                .email("user@example.com")
                .password("hashedPassword")
                .role(User.Role.USER)
                .emailVerified(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(userService.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(user)).thenReturn("jwt-token");

        // when
        AuthResponse response = authService.login(request);

        // then
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("user@example.com");
        assertThat(response.getRole()).isEqualTo("USER");
        assertThat(response.isEmailVerified()).isTrue();
    }

    @Test
    void login_shouldThrowExceptionForNonexistentUser() {
        // given
        LoginRequest request =
            LoginRequest.builder().email("nonexistent@example.com").password("password").build();

        when(userService.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_shouldThrowExceptionForWrongPassword() {
        // given
        LoginRequest request =
            LoginRequest.builder().email("user@example.com").password("wrongPassword").build();

        Instant now = Instant.now();
        User user =
            User.builder()
                .id(1L)
                .email("user@example.com")
                .password("hashedPassword")
                .role(User.Role.USER)
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(userService.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(InvalidCredentialsException.class);
    }
}
