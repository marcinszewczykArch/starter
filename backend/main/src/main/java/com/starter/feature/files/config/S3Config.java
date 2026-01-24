package com.starter.feature.files.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import jakarta.annotation.PostConstruct;

import java.net.URI;

/**
 * Configuration for AWS S3 client and presigner.
 * Enables Spring Retry for S3 operations.
 * Supports LocalStack for local development (when s3-endpoint is set).
 */
@Slf4j
@Configuration
@EnableRetry
@ConfigurationProperties(prefix = "app.storage")
@Data
public class S3Config {
    private String s3BucketName;
    private String s3Region;

    @Value("${app.storage.s3-endpoint:}")
    private String s3Endpoint;

    @PostConstruct
    public void logConfiguration() {
        log.info(
            "S3Config initialized - s3BucketName: '{}', s3Region: '{}'",
            s3BucketName != null ? s3BucketName : "null",
            s3Region != null ? s3Region : "null"
        );
        if (s3BucketName == null || s3BucketName.isBlank()) {
            log.warn("S3_BUCKET_NAME is not set or empty. File storage feature may not work correctly.");
        } else {
            log.info("S3_BUCKET_NAME is set: {}", s3BucketName);
        }
    }

    @Bean
    public S3Client s3Client() {
        log.info(
            "Creating S3Client bean - bucketName: '{}', region: '{}', endpoint: '{}'",
            s3BucketName, s3Region, s3Endpoint != null && !s3Endpoint.isBlank() ? s3Endpoint : "default AWS"
        );
        if (s3BucketName == null || s3BucketName.isBlank()) {
            log.error("S3_BUCKET_NAME is null or blank!");
            throw new IllegalStateException("S3_BUCKET_NAME must be set in environment variables");
        }

        var builder = S3Client.builder()
            .region(Region.of(s3Region));

        // Use LocalStack if endpoint is configured (local development)
        if (s3Endpoint != null && !s3Endpoint.isBlank()) {
            log.info("Using LocalStack S3 endpoint: {}", s3Endpoint);
            builder.endpointOverride(URI.create(s3Endpoint))
                .credentialsProvider(
                    StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test", "test")
                    )
                );
        } else {
            // Use default AWS credentials (production)
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }

        S3Client client = builder.build();
        log.info("S3Client created successfully for bucket: {}", s3BucketName);
        return client;
    }

    @Bean
    public S3Presigner s3Presigner() {
        var builder = S3Presigner.builder()
            .region(Region.of(s3Region));

        // Use LocalStack if endpoint is configured (local development)
        if (s3Endpoint != null && !s3Endpoint.isBlank()) {
            log.info("Using LocalStack S3 presigner endpoint: {}", s3Endpoint);
            builder.endpointOverride(URI.create(s3Endpoint))
                .credentialsProvider(
                    StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test", "test")
                    )
                );
        }

        return builder.build();
    }
}
