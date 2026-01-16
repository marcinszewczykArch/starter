package com.starter.core.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.starter.core.auth.dto.AuthResponse;
import com.starter.core.auth.dto.ChangePasswordRequest;
import com.starter.core.auth.dto.ForgotPasswordRequest;
import com.starter.core.auth.dto.LoginRequest;
import com.starter.core.auth.dto.RegisterRequest;
import com.starter.core.auth.dto.ResendVerificationRequest;
import com.starter.core.auth.dto.ResetPasswordRequest;
import com.starter.core.auth.dto.VerifyEmailRequest;
import com.starter.core.common.dto.MessageResponse;
import com.starter.core.security.UserPrincipal;
import com.starter.core.user.dto.UserResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/** REST controller for authentication endpoints. */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration, login, and email verification")
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user and send verification email")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public AuthResponse login(
        @Valid @RequestBody LoginRequest request,
        HttpServletRequest httpRequest
    ) {
        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        return authService.login(request, ipAddress, userAgent);
    }

    /** Extract client IP address, handling proxies (X-Forwarded-For, X-Real-IP). */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, first one is the client
            int commaIndex = xForwardedFor.indexOf(',');
            String firstIp = commaIndex > 0 ? xForwardedFor.substring(0, commaIndex) : xForwardedFor;
            return firstIp.trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user info", security = @SecurityRequirement(name = "bearerAuth"))
    public UserResponse getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        return UserResponse.fromPrincipal(principal);
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email address with token")
    public MessageResponse verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        emailVerificationService.verifyEmail(request.getToken());
        return MessageResponse.of("Email verified successfully");
    }

    @GetMapping("/verify-email")
    @Operation(summary = "Verify email address with token (GET for email links)")
    public MessageResponse verifyEmailGet(@RequestParam String token) {
        emailVerificationService.verifyEmail(token);
        return MessageResponse.of("Email verified successfully");
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend verification email")
    public MessageResponse resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        emailVerificationService.resendVerificationEmail(request.getEmail());
        return MessageResponse.of("Verification email sent");
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset email")
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.requestPasswordReset(request.getEmail());
        return MessageResponse.of("If the email exists, a reset link has been sent");
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using token")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getPassword());
        return MessageResponse.of("Password reset successfully");
    }

    @PostMapping("/change-password")
    @Operation(
        summary = "Change password for authenticated user",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public MessageResponse changePassword(
        @Valid @RequestBody ChangePasswordRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        authService.changePassword(
            principal.getId(),
            request.getCurrentPassword(),
            request.getNewPassword()
        );
        return MessageResponse.of("Password changed successfully");
    }

    @PostMapping("/confirm-email-change")
    @Operation(summary = "Confirm email change with token")
    public MessageResponse confirmEmailChange(@RequestParam String token) {
        authService.confirmEmailChange(token);
        return MessageResponse.of("Email changed successfully");
    }

    @GetMapping("/confirm-email-change")
    @Operation(summary = "Confirm email change with token (GET for email links)")
    public MessageResponse confirmEmailChangeGet(@RequestParam String token) {
        authService.confirmEmailChange(token);
        return MessageResponse.of("Email changed successfully");
    }
}
