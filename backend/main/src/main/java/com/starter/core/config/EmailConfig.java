package com.starter.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Configuration for email service using Resend. */
@Configuration
@ConfigurationProperties(prefix = "app.email")
@Getter
@Setter
public class EmailConfig {

    /** Resend API key. */
    private String apiKey;

    /** Email address to send from. */
    private String fromAddress = "noreply@example.com";

    /** Whether email sending is enabled. */
    private boolean enabled = true;

    /** Application name for email templates. */
    private String appName = "Starter App";

    /** Base URL for links in emails. */
    private String baseUrl = "http://localhost:5173";
}
