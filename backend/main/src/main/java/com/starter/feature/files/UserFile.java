package com.starter.feature.files;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Entity representing a user's uploaded file.
 * Metadata is stored in PostgreSQL, actual file is stored in S3.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFile {
    private Long id;
    private Long userId;
    private String filename;  // Sanitized filename
    private String s3Key;     // Full S3 key: users/{userId}/files/{uuid}-{sanitizedFilename}
    private Long sizeBytes;
    private String contentType;
    private String thumbnailS3Key;  // Optional: thumbnail for images
    private Instant createdAt;
    private Instant updatedAt;
}
