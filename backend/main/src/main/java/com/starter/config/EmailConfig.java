package com.starter.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

/** Configuration for email service using AWS SES. */
@Configuration
@ConfigurationProperties(prefix = "app.email")
@Getter
@Setter
public class EmailConfig {

    /** Email address to send from (must be verified in SES). */
    private String fromAddress = "noreply@example.com";

    /** AWS region for SES. */
    private String region = "eu-central-1";

    /** Whether email sending is enabled. */
    private boolean enabled = true;

    /** Application name for email templates. */
    private String appName = "Starter App";

    /** Base URL for links in emails. */
    private String baseUrl = "http://localhost:5173";

    @Bean
    public SesClient sesClient() {
        return SesClient.builder()
            .region(Region.of(region))
            .build();
    }
}
