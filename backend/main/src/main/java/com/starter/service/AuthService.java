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
import com.starter.security.JwtUtil;

/** Service for authentication operations. */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /** Register a new user. */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        log.info("Registering user with email: {}", normalizedEmail);

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User user = userService.createUser(normalizedEmail, hashedPassword, User.Role.USER);

        String token = jwtUtil.generateToken(user);

        return AuthResponse.builder()
            .token(token)
            .userId(user.getId())
            .email(user.getEmail())
            .role(user.getRole().name())
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
            .build();
    }

    /** Normalize email to lowercase for consistent storage and lookup. */
    private String normalizeEmail(String email) {
        return email.toLowerCase(java.util.Locale.ROOT).trim();
    }
}
