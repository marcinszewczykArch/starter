package com.starter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SesException;

import com.starter.config.EmailConfig;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** Service for sending emails via AWS SES. */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final SesClient sesClient;
    private final EmailConfig emailConfig;

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
                <head>
                    <style>
                        .container { font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; }
                        .button { background-color: #4F46E5; color: white; padding: 12px 24px;
                                  text-decoration: none; border-radius: 6px; display: inline-block; }
                        .footer { color: #666; font-size: 12px; margin-top: 20px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h2>Welcome to %s!</h2>
                        <p>Please verify your email address by clicking the button below:</p>
                        <p><a href="%s" class="button">Verify Email</a></p>
                        <p>Or copy this link: <br><a href="%s">%s</a></p>
                        <p>This link will expire in 24 hours.</p>
                        <div class="footer">
                            <p>If you didn't create an account, you can ignore this email.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(emailConfig.getAppName(), verifyUrl, verifyUrl, verifyUrl);

        String textBody =
            """
                Welcome to %s!

                Please verify your email address by visiting this link:
                %s

                This link will expire in 24 hours.

                If you didn't create an account, you can ignore this email.
                """.formatted(emailConfig.getAppName(), verifyUrl);

        sendEmail(toEmail, subject, htmlBody, textBody);
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
                <head>
                    <style>
                        .container { font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; }
                        .button { background-color: #4F46E5; color: white; padding: 12px 24px;
                                  text-decoration: none; border-radius: 6px; display: inline-block; }
                        .footer { color: #666; font-size: 12px; margin-top: 20px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h2>Password Reset Request</h2>
                        <p>You requested to reset your password. Click the button below:</p>
                        <p><a href="%s" class="button">Reset Password</a></p>
                        <p>Or copy this link: <br><a href="%s">%s</a></p>
                        <p>This link will expire in 1 hour.</p>
                        <div class="footer">
                            <p>If you didn't request this, you can ignore this email. Your password won't change.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(resetUrl, resetUrl, resetUrl);

        String textBody =
            """
                Password Reset Request

                You requested to reset your password. Visit this link:
                %s

                This link will expire in 1 hour.

                If you didn't request this, you can ignore this email. Your password won't change.
                """.formatted(resetUrl);

        sendEmail(toEmail, subject, htmlBody, textBody);
    }

    /**
     * Send a generic email.
     *
     * @param toEmail  recipient email address
     * @param subject  email subject
     * @param htmlBody HTML content
     * @param textBody plain text content (fallback)
     */
    public void sendEmail(String toEmail, String subject, String htmlBody, String textBody) {
        if (!emailConfig.isEnabled()) {
            log.info("Email sending disabled. Would send to: {} subject: {}", toEmail, subject);
            return;
        }

        try {
            SendEmailRequest request = SendEmailRequest.builder()
                .source(emailConfig.getFromAddress())
                .destination(Destination.builder().toAddresses(toEmail).build())
                .message(
                    Message.builder()
                        .subject(Content.builder().data(subject).charset("UTF-8").build())
                        .body(
                            Body.builder()
                                .html(Content.builder().data(htmlBody).charset("UTF-8").build())
                                .text(Content.builder().data(textBody).charset("UTF-8").build())
                                .build()
                        )
                        .build()
                )
                .build();

            sesClient.sendEmail(request);
            log.info("Email sent successfully to: {}", toEmail);

        } catch (SesException e) {
            String errorMsg = e.awsErrorDetails() != null
                ? e.awsErrorDetails().errorMessage()
                : e.getMessage();
            log.error("Failed to send email to {}: {}", toEmail, errorMsg);
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
