package com.starter.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for security token expiration and cooldown settings.
 * Values are configured in application.yml under app.security.*
 */
@Configuration
@ConfigurationProperties(prefix = "app.security")
@Getter
@Setter
public class SecurityTokenConfig {

    /** Password reset token expiration in hours. Default: 1 hour. */
    private int passwordResetExpirationHours = 1;

    /** Email verification token expiration in hours. Default: 24 hours. */
    private int emailVerificationExpirationHours = 24;

    /** Cooldown before resending verification email in minutes. Default: 5 minutes. */
    private int resendVerificationCooldownMinutes = 5;
}
