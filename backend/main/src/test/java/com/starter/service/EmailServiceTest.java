package com.starter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.starter.config.EmailConfig;

/** Unit tests for EmailService. */
class EmailServiceTest {

    private EmailConfig emailConfig;
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailConfig = new EmailConfig();
        emailConfig.setApiKey("test-api-key");
        emailConfig.setFromAddress("noreply@test.com");
        emailConfig.setAppName("Test App");
        emailConfig.setBaseUrl("https://test.com");
        emailConfig.setEnabled(false); // Disabled by default for unit tests

        emailService = new EmailService(emailConfig, new ObjectMapper());
    }

    @Test
    void sendVerificationEmail_shouldNotSendWhenDisabled() {
        // when/then - should not throw, just log
        assertDoesNotThrow(() -> emailService.sendVerificationEmail("user@example.com", "abc123"));
    }

    @Test
    void sendPasswordResetEmail_shouldNotSendWhenDisabled() {
        // when/then - should not throw, just log
        assertDoesNotThrow(
            () -> emailService.sendPasswordResetEmail("user@example.com", "reset-token-xyz")
        );
    }

    @Test
    void sendEmail_shouldNotSendWhenDisabled() {
        // when/then - should not throw, just log
        assertDoesNotThrow(() -> emailService.sendEmail("user@example.com", "Subject", "<p>HTML</p>"));
    }

    @Test
    void emailConfig_shouldHaveCorrectDefaults() {
        EmailConfig config = new EmailConfig();

        assertThat(config.isEnabled()).isTrue();
        assertThat(config.getFromAddress()).isEqualTo("noreply@example.com");
        assertThat(config.getAppName()).isEqualTo("Starter App");
        assertThat(config.getBaseUrl()).isEqualTo("http://localhost:5173");
    }
}
