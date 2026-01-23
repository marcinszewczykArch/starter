package com.starter.feature.files;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;

import com.starter.core.exception.ResourceNotFoundException;
import com.starter.feature.files.dto.*;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Service for file operations.
 *
 * Atomicity strategy:
 * - Upload: DB first, then S3 (if S3 fails → rollback DB)
 * - Delete: DB first, then S3 (if S3 fails → orphaned file, but user sees success)
 *
 * Only created if S3Client bean exists (i.e., S3_BUCKET_NAME is set).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(S3Client.class)
public class FileService {
    private final FileRepository fileRepository;
    private final S3Service s3Service;
    private final StorageQuotaService quotaService;
    private final ContentTypeValidator contentTypeValidator;

    @Value("${app.storage.max-file-size}")
    private long maxFileSizeBytes;

    @Value("${app.storage.presigned-url-expiration-minutes:60}")
    private int presignedUrlExpirationMinutes;

    /**
     * Upload file with full validation and atomicity.
     * Strategy: DB first, then S3 (if S3 fails, DB rollback).
     */
    @Transactional
    @CacheEvict(value = "fileStats", key = "#userId")
    public FileDto uploadFile(Long userId, MultipartFile file) throws IOException {
        // 1. Validate file size BEFORE any processing
        if (file.getSize() > maxFileSizeBytes) {
            throw new IllegalArgumentException(
                String.format("File exceeds maximum size of %d bytes", maxFileSizeBytes)
            );
        }

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // 2. Validate content type
        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";  // Default
        }
        contentTypeValidator.validate(contentType);

        // 3. Sanitize filename
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "file_" + System.currentTimeMillis();
        }
        String sanitizedFilename = FilenameSanitizer.sanitize(originalFilename);

        // 4. Check if filename already exists
        if (fileRepository.existsByUserIdAndFilename(userId, sanitizedFilename)) {
            throw new IllegalArgumentException(
                String.format("File with name '%s' already exists", sanitizedFilename)
            );
        }

        // 5. Check quota WITH LOCK (prevents race conditions)
        quotaService.checkQuotaWithLock(userId, file.getSize());

        // 6. Generate S3 key
        String fileId = UUID.randomUUID().toString();
        String s3Key = String.format("users/%d/files/%s-%s", userId, fileId, sanitizedFilename);

        // 7. Read file content
        byte[] content = file.getBytes();

        // 8. Save to DB FIRST (in transaction)
        UserFile userFile = UserFile.builder()
            .userId(userId)
            .filename(sanitizedFilename)
            .s3Key(s3Key)
            .sizeBytes(file.getSize())
            .contentType(contentType)
            .build();

        UserFile saved = fileRepository.save(userFile);

        try {
            // 9. Upload to S3 AFTER DB save
            s3Service.uploadFile(s3Key, content, contentType);
        } catch (Exception e) {
            // 10. If S3 upload fails, transaction will rollback DB insert
            log.error("S3 upload failed for file {}, rolling back DB record", saved.getId(), e);
            throw new RuntimeException("Failed to upload file to storage", e);
        }

        log.info(
            "File uploaded successfully: {} for user {} ({} bytes)",
            sanitizedFilename, userId, file.getSize()
        );
        return toDto(saved);
    }

    /**
     * Get user files with pagination.
     */
    public Page<FileDto> getUserFiles(Long userId, Pageable pageable) {
        Page<UserFile> files = fileRepository.findByUserIdPaginated(
            userId,
            pageable.getPageNumber(),
            pageable.getPageSize()
        );
        return files.map(this::toDto);
    }

    /**
     * Get user files filtered by content type.
     */
    public Page<FileDto> getUserFilesByContentType(Long userId, String contentType, Pageable pageable) {
        Page<UserFile> files = fileRepository.findByUserIdAndContentType(
            userId,
            contentType,
            pageable.getPageNumber(),
            pageable.getPageSize()
        );
        return files.map(this::toDto);
    }

    /**
     * Search files by filename.
     */
    public Page<FileDto> searchFiles(Long userId, String query, Pageable pageable) {
        Page<UserFile> files = fileRepository.findByUserIdAndFilenameContaining(
            userId,
            query,
            pageable.getPageNumber(),
            pageable.getPageSize()
        );
        return files.map(this::toDto);
    }

    /**
     * Get file statistics (cached for 30 seconds).
     */
    @Cacheable(value = "fileStats", key = "#userId", unless = "#result.fileCount == 0")
    public FileStatsDto getFileStats(Long userId) {
        long totalSize = fileRepository.getTotalSizeByUserId(userId);
        long fileCount = fileRepository.countByUserId(userId);

        return FileStatsDto.builder()
            .fileCount((int) fileCount)
            .totalSizeBytes(totalSize)
            .build();
    }

    /**
     * Get storage usage info.
     */
    public StorageQuotaService.StorageUsageInfo getStorageUsage(Long userId) {
        return quotaService.getUsageInfo(userId);
    }

    /**
     * Get presigned download URL for file.
     */
    public String getDownloadUrl(Long userId, Long fileId) {
        UserFile file = fileRepository.findByIdAndUserId(fileId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("File", fileId));

        Duration expiration = Duration.ofMinutes(presignedUrlExpirationMinutes);
        return s3Service.generatePresignedDownloadUrl(file.getS3Key(), expiration);
    }

    /**
     * Delete file (atomic: DB first, then S3).
     * Strategy: DB first → user sees immediate success.
     * If S3 delete fails → orphaned file (acceptable, invisible to user).
     */
    @Transactional
    @CacheEvict(value = "fileStats", key = "#userId")
    public void deleteFile(Long userId, Long fileId) {
        UserFile file = fileRepository.findByIdAndUserId(fileId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("File", fileId));

        // 1. Delete from DB FIRST (user sees immediate success)
        fileRepository.delete(fileId);

        // 2. Delete from S3 SECOND (best effort, don't block)
        try {
            s3Service.deleteFile(file.getS3Key());
            if (file.getThumbnailS3Key() != null) {
                s3Service.deleteFile(file.getThumbnailS3Key());
            }
            log.debug("File deleted from S3: {}", file.getS3Key());
        } catch (Exception e) {
            // Log but don't fail - file is already removed from DB
            log.warn(
                "Failed to delete file from S3 after DB deletion: {} (file will remain in S3 as orphaned)",
                file.getS3Key(), e
            );
            // File remains in S3 but is invisible to user (removed from DB)
            // This is acceptable - orphaned files don't affect UX
        }

        log.info("File deleted: {} for user {}", file.getFilename(), userId);
    }

    /**
     * Delete all user files (used when account is deleted).
     * Strategy: DB first, then S3 (best effort).
     */
    @Transactional
    @CacheEvict(value = "fileStats", key = "#userId")
    public void deleteAllUserFiles(Long userId) {
        log.info("Deleting all files for user ID: {}", userId);

        List<UserFile> files = fileRepository.findByUserId(userId);
        int deletedFromDb = 0;
        int deletedFromS3 = 0;
        int s3Failures = 0;

        // 1. Delete all from DB FIRST
        for (UserFile file : files) {
            fileRepository.delete(file.getId());
            deletedFromDb++;
        }

        // 2. Delete all from S3 SECOND (best effort)
        for (UserFile file : files) {
            try {
                s3Service.deleteFile(file.getS3Key());
                if (file.getThumbnailS3Key() != null) {
                    s3Service.deleteFile(file.getThumbnailS3Key());
                }
                deletedFromS3++;
            } catch (Exception e) {
                log.warn("Failed to delete file {} from S3: {}", file.getS3Key(), e.getMessage());
                s3Failures++;
            }
        }

        log.info(
            "Deleted {} files from DB, {} from S3 ({} S3 failures) for user {}",
            deletedFromDb, deletedFromS3, s3Failures, userId
        );
    }

    private FileDto toDto(UserFile file) {
        return FileDto.builder()
            .id(file.getId())
            .filename(file.getFilename())
            .sizeBytes(file.getSizeBytes())
            .contentType(file.getContentType())
            .createdAt(file.getCreatedAt())
            .build();
    }
}
