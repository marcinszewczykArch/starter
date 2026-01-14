package com.starter.core.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.starter.core.admin.LoginHistory;
import com.starter.core.admin.LoginHistoryService;
import com.starter.core.auth.dto.AuthResponse;
import com.starter.core.auth.dto.LoginRequest;
import com.starter.core.auth.dto.RegisterRequest;
import com.starter.core.config.SecurityTokenConfig;
import com.starter.core.email.EmailService;
import com.starter.core.exception.InvalidCredentialsException;
import com.starter.core.exception.InvalidTokenException;
import com.starter.core.security.JwtUtil;
import com.starter.core.user.User;
import com.starter.core.user.UserRepository;
import com.starter.core.user.UserService;
import com.starter.shared.util.TokenGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/** Service for authentication operations. */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailVerificationService emailVerificationService;
    private final EmailService emailService;
    private final TokenGenerator tokenGenerator;
    private final SecurityTokenConfig securityTokenConfig;
    private final LoginHistoryService loginHistoryService;

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

    /**
     * Login with email and password.
     *
     * @param request   Login credentials and optional GPS location
     * @param ipAddress IP address of the request
     * @param userAgent User-Agent header
     * @return AuthResponse with JWT token
     */
    public AuthResponse login(LoginRequest request, String ipAddress, String userAgent) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        log.info("Login attempt for email: {}", normalizedEmail);

        // Extract GPS coordinates if provided
        BigDecimal gpsLat = null;
        BigDecimal gpsLng = null;
        if (request.getLocation() != null) {
            gpsLat = request.getLocation().getLatitude();
            gpsLng = request.getLocation().getLongitude();
        }

        User user = userService.findByEmail(normalizedEmail).orElse(null);

        // User not found
        if (user == null) {
            log.warn("Login failed: user not found for email {}", normalizedEmail);
            loginHistoryService.recordFailedLogin(
                normalizedEmail, null, ipAddress, userAgent, LoginHistory.FailureReason.USER_NOT_FOUND
            );
            throw new InvalidCredentialsException();
        }

        // Invalid password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed: invalid password for email {}", normalizedEmail);
            loginHistoryService.recordFailedLogin(
                normalizedEmail, user.getId(), ipAddress, userAgent, LoginHistory.FailureReason.INVALID_PASSWORD
            );
            throw new InvalidCredentialsException();
        }

        // Success - record login history
        loginHistoryService.recordSuccessfulLogin(user.getId(), ipAddress, userAgent, gpsLat, gpsLng);

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
                    String token = tokenGenerator.generate();
                    Instant expiresAt = Instant.now()
                        .plus(securityTokenConfig.getPasswordResetExpirationHours(), ChronoUnit.HOURS);
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

    /**
     * Change password for authenticated user.
     *
     * @param userId          ID of the user changing password
     * @param currentPassword current password for verification
     * @param newPassword     new password to set
     * @throws InvalidCredentialsException if current password is incorrect
     * @throws IllegalArgumentException    if new password same as current
     */
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        log.info("Password change requested for user ID: {}", userId);

        User user =
            userRepository
                .findById(userId)
                .orElseThrow(
                    () -> {
                        log.warn("Password change failed: user not found for ID {}", userId);
                        return new InvalidCredentialsException();
                    }
                );

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            log.warn("Password change failed: incorrect current password for user {}", user.getEmail());
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            log.warn("Password change failed: new password same as current for user {}", user.getEmail());
            throw new IllegalArgumentException("New password must be different from current password");
        }

        String hashedPassword = passwordEncoder.encode(newPassword);
        userRepository.updatePassword(userId, hashedPassword);
        log.info("Password changed successfully for user: {}", user.getEmail());
    }

    /** Normalize email to lowercase for consistent storage and lookup. */
    private String normalizeEmail(String email) {
        return email.toLowerCase(java.util.Locale.ROOT).trim();
    }
}
