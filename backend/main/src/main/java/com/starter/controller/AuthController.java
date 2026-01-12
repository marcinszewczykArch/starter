package com.starter.controller;

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

import com.starter.dto.AuthResponse;
import com.starter.dto.ForgotPasswordRequest;
import com.starter.dto.LoginRequest;
import com.starter.dto.MessageResponse;
import com.starter.dto.RegisterRequest;
import com.starter.dto.ResendVerificationRequest;
import com.starter.dto.ResetPasswordRequest;
import com.starter.dto.UserResponse;
import com.starter.dto.VerifyEmailRequest;
import com.starter.security.UserPrincipal;
import com.starter.service.AuthService;
import com.starter.service.EmailVerificationService;

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
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
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
}
