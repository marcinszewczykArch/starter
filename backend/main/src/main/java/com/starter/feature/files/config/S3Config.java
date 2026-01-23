package com.starter.feature.files.config;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * Configuration for AWS S3 client and presigner.
 * Enables Spring Retry for S3 operations.
 */
@Configuration
@EnableRetry
@ConfigurationProperties(prefix = "app.storage")
@Data
public class S3Config {
    private String s3BucketName;
    private String s3Region;

    @Bean
    @ConditionalOnExpression("'${app.storage.s3-bucket-name:}' != ''")
    public S3Client s3Client() {
        if (s3BucketName == null || s3BucketName.isBlank()) {
            throw new IllegalStateException("S3_BUCKET_NAME must be set in environment variables");
        }
        return S3Client.builder()
            .region(Region.of(s3Region))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
    }

    @Bean
    @ConditionalOnExpression("'${app.storage.s3-bucket-name:}' != ''")
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
            .region(Region.of(s3Region))
            .build();
    }
}
