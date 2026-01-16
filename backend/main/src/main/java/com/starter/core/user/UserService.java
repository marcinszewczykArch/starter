package com.starter.core.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.starter.core.email.EmailService;
import com.starter.core.exception.EmailAlreadyExistsException;
import com.starter.core.exception.InvalidCredentialsException;
import com.starter.shared.util.TokenGenerator;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/** Service layer for User operations. */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenGenerator tokenGenerator;
    private final EmailService emailService;

    /** Find user by email. */
    public Optional<User> findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    /** Find user by ID. */
    public Optional<User> findById(Long id) {
        log.debug("Finding user by id: {}", id);
        return userRepository.findById(id);
    }

    /** Check if email is already taken. */
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Create a new user. Password should already be hashed.
     *
     * @throws EmailAlreadyExistsException if email is already taken
     */
    @Transactional
    public User createUser(String email, String hashedPassword, User.Role role) {
        log.info("Creating user with email: {}", email);

        // Early check for better error message (avoids DB exception in most cases)
        if (userRepository.existsByEmail(email)) {
            log.warn("Attempted to create user with existing email: {}", email);
            throw new EmailAlreadyExistsException(email);
        }

        User user = User.builder().email(email).password(hashedPassword).role(role).build();

        try {
            return userRepository.save(user);
        } catch (DuplicateKeyException e) {
            // Race condition: another request inserted same email between check and save
            log.warn("Race condition: email {} was inserted by another request", email);
            throw new EmailAlreadyExistsException(email);
        }
    }

    /**
     * Request email change - sends verification email to new address.
     *
     * @param userId   User ID
     * @param newEmail New email address
     * @param password Current password for verification
     * @throws InvalidCredentialsException if password is incorrect
     * @throws EmailAlreadyExistsException if new email is already taken
     */
    @Transactional
    public void requestEmailChange(Long userId, String newEmail, String password) {
        log.info("Email change requested for user ID: {}", userId);

        User user = userRepository
            .findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Email change failed: incorrect password for user ID {}", userId);
            throw new InvalidCredentialsException("Password is incorrect");
        }

        // Normalize email
        String normalizedNewEmail = newEmail.toLowerCase(java.util.Locale.ROOT).trim();

        // Check if new email is the same as current email
        if (normalizedNewEmail.equals(user.getEmail().toLowerCase(java.util.Locale.ROOT))) {
            log.warn("Email change failed: new email is the same as current email for user ID {}", userId);
            throw new IllegalArgumentException("New email must be different from current email");
        }

        // Check if new email is already taken (including archived)
        if (userRepository.existsByEmailIncludingArchived(normalizedNewEmail)) {
            log.warn("Email change failed: new email already exists: {}", normalizedNewEmail);
            throw new EmailAlreadyExistsException("Email is already taken");
        }

        // Generate token
        String token = tokenGenerator.generate();
        Instant expiresAt = Instant.now().plus(1, ChronoUnit.HOURS); // 1 hour expiry

        // Save pending email and token
        userRepository.setEmailChangeToken(userId, normalizedNewEmail, token, expiresAt);

        // Send verification email to NEW address
        emailService.sendEmailChangeVerificationEmail(normalizedNewEmail, token);

        log.info("Email change verification sent to: {}", normalizedNewEmail);
    }

    /**
     * Delete (archive) user account.
     *
     * @param userId   User ID
     * @param password User password for verification
     * @throws InvalidCredentialsException if password is incorrect
     */
    @Transactional
    public void deleteAccount(Long userId, String password) {
        log.info("Account deletion requested for user ID: {}", userId);

        User user = userRepository
            .findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Account deletion failed: incorrect password for user ID {}", userId);
            throw new InvalidCredentialsException("Password is incorrect");
        }

        // Soft delete (archive)
        userRepository.archiveUser(userId);

        log.info("Account archived for user ID: {}", userId);
    }
}
