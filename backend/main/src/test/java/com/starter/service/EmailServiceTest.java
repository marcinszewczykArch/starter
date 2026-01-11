package com.starter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SesException;

import com.starter.config.EmailConfig;

/** Unit tests for EmailService. */
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private SesClient sesClient;

    private EmailConfig emailConfig;
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailConfig = new EmailConfig();
        emailConfig.setFromAddress("noreply@test.com");
        emailConfig.setAppName("Test App");
        emailConfig.setBaseUrl("https://test.com");
        emailConfig.setEnabled(true);

        emailService = new EmailService(sesClient, emailConfig);
    }

    @Test
    void sendVerificationEmail_shouldSendWithCorrectContent() {
        // when
        emailService.sendVerificationEmail("user@example.com", "abc123");

        // then
        ArgumentCaptor<SendEmailRequest> captor = ArgumentCaptor.forClass(SendEmailRequest.class);
        verify(sesClient).sendEmail(captor.capture());

        SendEmailRequest request = captor.getValue();
        assertThat(request.source()).isEqualTo("noreply@test.com");
        assertThat(request.destination().toAddresses()).containsExactly("user@example.com");
        assertThat(request.message().subject().data()).contains("Verify your email");
        assertThat(request.message().body().html().data())
            .contains("https://test.com/verify-email?token=abc123");
    }

    @Test
    void sendPasswordResetEmail_shouldSendWithCorrectContent() {
        // when
        emailService.sendPasswordResetEmail("user@example.com", "reset-token-xyz");

        // then
        ArgumentCaptor<SendEmailRequest> captor = ArgumentCaptor.forClass(SendEmailRequest.class);
        verify(sesClient).sendEmail(captor.capture());

        SendEmailRequest request = captor.getValue();
        assertThat(request.source()).isEqualTo("noreply@test.com");
        assertThat(request.destination().toAddresses()).containsExactly("user@example.com");
        assertThat(request.message().subject().data()).contains("Reset your password");
        assertThat(request.message().body().html().data())
            .contains("https://test.com/reset-password?token=reset-token-xyz");
    }

    @Test
    void sendEmail_shouldNotSendWhenDisabled() {
        // given
        emailConfig.setEnabled(false);

        // when
        emailService.sendEmail("user@example.com", "Subject", "<p>HTML</p>", "Text");

        // then
        verify(sesClient, never()).sendEmail(any(SendEmailRequest.class));
    }

    @Test
    void sendEmail_shouldThrowEmailSendException_whenSesFails() {
        // given
        SesException sesException = (SesException) SesException.builder()
            .message("SES Error")
            .awsErrorDetails(
                AwsErrorDetails.builder()
                    .errorMessage("Invalid email address")
                    .build()
            )
            .build();

        doThrow(sesException).when(sesClient).sendEmail(any(SendEmailRequest.class));

        // when/then
        assertThrows(
            EmailService.EmailSendException.class,
            () -> emailService.sendEmail("invalid", "Subject", "<p>HTML</p>", "Text")
        );
    }

    @Test
    void sendEmail_shouldSucceed_whenSesReturnsOk() {
        // when/then
        assertDoesNotThrow(
            () -> emailService.sendEmail("user@example.com", "Subject", "<p>HTML</p>", "Text")
        );

        verify(sesClient).sendEmail(any(SendEmailRequest.class));
    }
}
