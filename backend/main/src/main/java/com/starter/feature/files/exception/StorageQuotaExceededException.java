package com.starter.feature.files.exception;

/**
 * Exception thrown when user tries to upload file that would exceed storage quota.
 */
public class StorageQuotaExceededException extends RuntimeException {
    private final long usedBytes;
    private final long maxBytes;
    private final long fileSize;

    public StorageQuotaExceededException(long usedBytes, long maxBytes, long fileSize) {
        super(
            String.format(
                "Storage quota exceeded. Used: %d bytes (%.2f%%), Max: %d bytes, File size: %d bytes",
                usedBytes, (double) usedBytes / maxBytes * 100, maxBytes, fileSize
            )
        );
        this.usedBytes = usedBytes;
        this.maxBytes = maxBytes;
        this.fileSize = fileSize;
    }

    public long getUsedBytes() {
        return usedBytes;
    }

    public long getMaxBytes() {
        return maxBytes;
    }

    public long getFileSize() {
        return fileSize;
    }
}
