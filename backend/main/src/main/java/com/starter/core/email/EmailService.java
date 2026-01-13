package com.starter.core.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.starter.core.config.EmailConfig;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/** Service for sending emails via Resend API. */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final String RESEND_API_URL = "https://api.resend.com/emails";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    private final EmailConfig emailConfig;
    private final ObjectMapper objectMapper;

    /**
     * Send a verification email with a token link.
     *
     * @param toEmail recipient email address
     * @param token   verification token
     */
    public void sendVerificationEmail(String toEmail, String token) {
        String subject = "Verify your email - " + emailConfig.getAppName();
        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
        String verifyUrl = emailConfig.getBaseUrl() + "/verify-email?token=" + encodedToken;

        String htmlBody =
            """
                <!DOCTYPE html>
                <html>
                <body style="margin: 0; padding: 20px; font-family: Arial, sans-serif; background-color: #f5f5f5;">
                    <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 30px; border-radius: 8px;">
                        <h2 style="color: #333; margin-top: 0;">Welcome to %s!</h2>
                        <p style="color: #555; font-size: 16px;">Please verify your email address by clicking the button below:</p>
                        <p style="text-align: center; margin: 30px 0;">
                            <a href="%s" style="background-color: #4F46E5; color: #ffffff; padding: 14px 28px; text-decoration: none; border-radius: 6px; display: inline-block; font-weight: bold; font-size: 16px;">Verify Email</a>
                        </p>
                        <p style="color: #555; font-size: 14px;">Or copy this link:<br><a href="%s" style="color: #4F46E5;">%s</a></p>
                        <p style="color: #555; font-size: 14px;">This link will expire in 24 hours.</p>
                        <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                        <p style="color: #999; font-size: 12px;">If you didn't create an account, you can ignore this email.</p>
                    </div>
                </body>
                </html>
                """
                .formatted(emailConfig.getAppName(), verifyUrl, verifyUrl, verifyUrl);

        sendEmail(toEmail, subject, htmlBody);
    }

    /**
     * Send a password reset email with a token link.
     *
     * @param toEmail recipient email address
     * @param token   password reset token
     */
    public void sendPasswordResetEmail(String toEmail, String token) {
        String subject = "Reset your password - " + emailConfig.getAppName();
        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
        String resetUrl = emailConfig.getBaseUrl() + "/reset-password?token=" + encodedToken;

        String htmlBody =
            """
                <!DOCTYPE html>
                <html>
                <body style="margin: 0; padding: 20px; font-family: Arial, sans-serif; background-color: #f5f5f5;">
                    <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 30px; border-radius: 8px;">
                        <h2 style="color: #333; margin-top: 0;">Password Reset Request</h2>
                        <p style="color: #555; font-size: 16px;">You requested to reset your password. Click the button below:</p>
                        <p style="text-align: center; margin: 30px 0;">
                            <a href="%s" style="background-color: #4F46E5; color: #ffffff; padding: 14px 28px; text-decoration: none; border-radius: 6px; display: inline-block; font-weight: bold; font-size: 16px;">Reset Password</a>
                        </p>
                        <p style="color: #555; font-size: 14px;">Or copy this link:<br><a href="%s" style="color: #4F46E5;">%s</a></p>
                        <p style="color: #555; font-size: 14px;">This link will expire in 1 hour.</p>
                        <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                        <p style="color: #999; font-size: 12px;">If you didn't request this, you can ignore this email. Your password won't change.</p>
                    </div>
                </body>
                </html>
                """
                .formatted(resetUrl, resetUrl, resetUrl);

        sendEmail(toEmail, subject, htmlBody);
    }

    /**
     * Send an email via Resend API.
     *
     * @param toEmail recipient email address
     * @param subject email subject
     * @param html    HTML content
     */
    public void sendEmail(String toEmail, String subject, String html) {
        if (!emailConfig.isEnabled()) {
            log.info("Email sending disabled. Would send to: {} subject: {}", toEmail, subject);
            return;
        }

        try {
            Map<String, Object> payload = Map.of(
                "from", emailConfig.getFromAddress(),
                "to", List.of(toEmail),
                "subject", subject,
                "html", html
            );

            String jsonBody = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(RESEND_API_URL))
                .header("Authorization", "Bearer " + emailConfig.getApiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(30))
                .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("Email sent successfully to: {}", toEmail);
            } else {
                log.error("Failed to send email to {}: {} - {}", toEmail, response.statusCode(), response.body());
                throw new EmailSendException("Failed to send email: " + response.body(), null);
            }

        } catch (IOException | InterruptedException e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new EmailSendException("Failed to send email to " + toEmail, e);
        }
    }

    /** Exception thrown when email sending fails. */
    public static class EmailSendException extends RuntimeException {
        public EmailSendException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
