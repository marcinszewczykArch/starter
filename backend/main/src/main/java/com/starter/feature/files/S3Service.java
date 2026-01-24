package com.starter.feature.files;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import jakarta.annotation.PostConstruct;

import java.time.Duration;
import java.util.List;

/**
 * Service for S3 operations with retry logic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${app.storage.s3-bucket-name}")
    private String bucketName;

    @PostConstruct
    private void validateBucketName() {
        log.info("S3Service initialization - bucketName: '{}'", bucketName);
        if (bucketName == null || bucketName.isBlank()) {
            log.error("S3 bucket name is null or blank! S3_BUCKET_NAME environment variable is not set.");
            throw new IllegalStateException(
                "S3 bucket name is not configured. Set S3_BUCKET_NAME environment variable."
            );
        }
        log.info("S3Service initialized successfully with bucket: {}", bucketName);
    }

    /**
     * Upload file to S3 with retry logic.
     */
    @Retryable(
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2),
        retryFor = {S3Exception.class}
    )
    public void uploadFile(String s3Key, byte[] content, String contentType) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType(contentType)
                .contentLength((long) content.length)
                .build();

            s3Client.putObject(request, RequestBody.fromBytes(content));
            log.debug("Uploaded file to S3: {} ({} bytes)", s3Key, content.length);
        } catch (S3Exception e) {
            log.error("Failed to upload file to S3: {} - {}", s3Key, e.getMessage());
            throw e;
        }
    }

    /**
     * Delete file from S3 with retry logic.
     */
    @Retryable(
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2),
        retryFor = {S3Exception.class}
    )
    public void deleteFile(String s3Key) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

            s3Client.deleteObject(request);
            log.debug("Deleted file from S3: {}", s3Key);
        } catch (S3Exception e) {
            log.error("Failed to delete file from S3: {} - {}", s3Key, e.getMessage());
            throw e;
        }
    }

    /**
     * Generate presigned URL for downloading file.
     */
    public String generatePresignedDownloadUrl(String s3Key, Duration expiration) {
        GetObjectRequest request = GetObjectRequest.builder()
            .bucket(bucketName)
            .key(s3Key)
            .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(
            presignerBuilder -> presignerBuilder
                .getObjectRequest(request)
                .signatureDuration(expiration)
        );

        return presignedRequest.url().toString();
    }

    /**
     * List all objects in S3 bucket (for cleanup - if needed in future).
     */
    public List<String> listAllObjectKeys(String prefix) {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
            .bucket(bucketName)
            .prefix(prefix)
            .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(request);
        return response.contents().stream()
            .map(S3Object::key)
            .toList();
    }

    /**
     * Check if object exists in S3.
     */
    public boolean objectExists(String s3Key) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();
            s3Client.headObject(request);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            log.error("Error checking object existence: {}", s3Key, e);
            return false;
        }
    }
}
