package com.starter.feature.files;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.starter.feature.files.exception.StorageQuotaExceededException;

/**
 * Service for managing storage quota with race condition prevention.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StorageQuotaService {
    private final FileRepository fileRepository;

    @Value("${app.storage.max-total-size}")
    private long maxTotalSizeBytes;

    /**
     * Check quota with lock to prevent race conditions.
     * Uses SELECT FOR UPDATE to lock user's files during transaction.
     *
     * @param userId        User ID
     * @param fileSizeBytes Size of file to upload
     * @throws StorageQuotaExceededException if quota would be exceeded
     */
    @Transactional
    public void checkQuotaWithLock(Long userId, long fileSizeBytes) {
        // SELECT FOR UPDATE locks rows until transaction commits
        long currentUsage = fileRepository.getTotalSizeWithLock(userId);

        if (currentUsage + fileSizeBytes > maxTotalSizeBytes) {
            log.warn(
                "Storage quota exceeded for user {}: {} + {} > {}",
                userId, currentUsage, fileSizeBytes, maxTotalSizeBytes
            );
            throw new StorageQuotaExceededException(
                currentUsage,
                maxTotalSizeBytes,
                fileSizeBytes
            );
        }
    }

    /**
     * Get current storage usage (without lock, for display).
     */
    public long getUsage(Long userId) {
        return fileRepository.getTotalSizeByUserId(userId);
    }

    /**
     * Get storage usage info (used/total).
     */
    public StorageUsageInfo getUsageInfo(Long userId) {
        long used = getUsage(userId);
        return StorageUsageInfo.builder()
            .usedBytes(used)
            .maxBytes(maxTotalSizeBytes)
            .percentage((double) used / maxTotalSizeBytes * 100)
            .build();
    }

    @lombok.Data
    @lombok.Builder
    public static class StorageUsageInfo {
        private long usedBytes;
        private long maxBytes;
        private double percentage;
    }
}
