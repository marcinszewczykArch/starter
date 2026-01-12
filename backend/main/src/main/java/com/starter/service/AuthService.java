package com.starter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.starter.domain.User;
import com.starter.dto.AuthResponse;
import com.starter.dto.LoginRequest;
import com.starter.dto.RegisterRequest;
import com.starter.exception.InvalidCredentialsException;
import com.starter.exception.InvalidTokenException;
import com.starter.repository.UserRepository;
import com.starter.security.JwtUtil;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

/** Service for authentication operations. */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int TOKEN_LENGTH = 32;
    private static final int PASSWORD_RESET_EXPIRATION_HOURS = 1;

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailVerificationService emailVerificationService;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    /** Register a new user and send verification email. */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        log.info("Registering user with email: {}", normalizedEmail);

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User user = userService.createUser(normalizedEmail, hashedPassword, User.Role.USER);

        // Send verification email
        emailVerificationService.sendVerificationEmail(user);

        String token = jwtUtil.generateToken(user);

        return AuthResponse.builder()
            .token(token)
            .userId(user.getId())
            .email(user.getEmail())
            .role(user.getRole().name())
            .emailVerified(user.isEmailVerified())
            .build();
    }

    /** Login with email and password. */
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        log.info("Login attempt for email: {}", normalizedEmail);

        User user =
            userService
                .findByEmail(normalizedEmail)
                .orElseThrow(
                    () -> {
                        log.warn("Login failed: user not found for email {}", normalizedEmail);
                        return new InvalidCredentialsException();
                    }
                );

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed: invalid password for email {}", normalizedEmail);
            throw new InvalidCredentialsException();
        }

        String token = jwtUtil.generateToken(user);
        log.info("Login successful for email: {}", normalizedEmail);

        return AuthResponse.builder()
            .token(token)
            .userId(user.getId())
            .email(user.getEmail())
            .role(user.getRole().name())
            .emailVerified(user.isEmailVerified())
            .build();
    }

    /**
     * Request password reset - sends email with reset link. Always returns success to prevent email
     * enumeration.
     */
    @Transactional
    public void requestPasswordReset(String email) {
        String normalizedEmail = normalizeEmail(email);
        log.info("Password reset requested for email: {}", normalizedEmail);

        userService
            .findByEmail(normalizedEmail)
            .ifPresentOrElse(
                user -> {
                    String token = generateToken();
                    Instant expiresAt =
                        Instant.now().plus(PASSWORD_RESET_EXPIRATION_HOURS, ChronoUnit.HOURS);
                    userRepository.updatePasswordResetToken(user.getId(), token, expiresAt);
                    emailService.sendPasswordResetEmail(user.getEmail(), token);
                    log.info("Password reset email sent to: {}", normalizedEmail);
                },
                () -> log.warn("Password reset requested for non-existent email: {}", normalizedEmail)
            );
    }

    /** Reset password using token. */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user =
            userRepository
                .findByPasswordResetToken(token)
                .orElseThrow(
                    () -> {
                        log.warn("Invalid password reset token attempted");
                        return new InvalidTokenException("Invalid or expired reset token");
                    }
                );

        if (user.getPasswordResetTokenExpiresAt() == null
            || Instant.now().isAfter(user.getPasswordResetTokenExpiresAt())) {
            log.warn("Expired password reset token for user: {}", user.getEmail());
            throw new InvalidTokenException("Reset token has expired");
        }

        String hashedPassword = passwordEncoder.encode(newPassword);
        userRepository.updatePassword(user.getId(), hashedPassword);
        log.info("Password reset successful for user: {}", user.getEmail());
    }

    /** Normalize email to lowercase for consistent storage and lookup. */
    private String normalizeEmail(String email) {
        return email.toLowerCase(java.util.Locale.ROOT).trim();
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
