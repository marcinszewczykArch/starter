package com.starter.core.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.starter.core.config.SecurityTokenConfig;
import com.starter.core.email.EmailService;
import com.starter.core.exception.InvalidTokenException;
import com.starter.core.user.User;
import com.starter.core.user.UserRepository;
import com.starter.shared.util.TokenGenerator;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/** Service for email verification operations. */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final TokenGenerator tokenGenerator;
    private final SecurityTokenConfig securityTokenConfig;

    /**
     * Generate a verification token and send verification email.
     *
     * @param user the user to send verification email to
     * @return the generated token
     */
    @Transactional
    public String sendVerificationEmail(User user) {
        String token = tokenGenerator.generate();
        Instant expiresAt = Instant.now()
            .plus(securityTokenConfig.getEmailVerificationExpirationHours(), ChronoUnit.HOURS);

        userRepository.updateVerificationToken(user.getId(), token, expiresAt);
        emailService.sendVerificationEmail(user.getEmail(), token);

        log.info("Verification email sent to: {}", user.getEmail());
        return token;
    }

    /**
     * Verify email using token.
     *
     * @param token the verification token
     * @throws InvalidTokenException if token is invalid or expired
     */
    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
            .orElseThrow(() -> {
                log.warn("Invalid verification token attempted");
                return new InvalidTokenException("Invalid or expired verification token");
            });

        if (user.getVerificationTokenExpiresAt() == null
            || Instant.now().isAfter(user.getVerificationTokenExpiresAt())) {
            log.warn("Expired verification token for user: {}", user.getEmail());
            throw new InvalidTokenException("Verification token has expired");
        }

        if (user.isEmailVerified()) {
            log.info("Email already verified for user: {}", user.getEmail());
            return;
        }

        userRepository.markEmailVerified(user.getId());
        log.info("Email verified successfully for user: {}", user.getEmail());
    }

    /**
     * Resend verification email if not already verified.
     * Has a cooldown period to prevent abuse.
     *
     * @param email the user's email address
     * @throws InvalidTokenException if user not found, already verified, or cooldown not elapsed
     */
    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email).orElse(null);

        // Don't reveal whether email exists - always return success message
        if (user == null) {
            log.warn("Resend verification attempted for non-existent email: {}", email);
            return;
        }

        if (user.isEmailVerified()) {
            log.info("Resend verification attempted for already verified email: {}", email);
            return; // Don't reveal that email is already verified
        }

        // Check cooldown - prevent spam
        if (user.getVerificationTokenExpiresAt() != null) {
            int expirationHours = securityTokenConfig.getEmailVerificationExpirationHours();
            int cooldownMinutes = securityTokenConfig.getResendVerificationCooldownMinutes();

            Instant cooldownEnd = user.getVerificationTokenExpiresAt()
                .minus(expirationHours, ChronoUnit.HOURS)
                .plus(cooldownMinutes, ChronoUnit.MINUTES);

            if (Instant.now().isBefore(cooldownEnd)) {
                log.warn("Resend verification rate limited for email: {}", email);
                throw new InvalidTokenException(
                    "Please wait " + cooldownMinutes + " minutes before requesting another email"
                );
            }
        }

        sendVerificationEmail(user);
    }
}
