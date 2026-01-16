package com.starter.core.exception;

/** Exception thrown when uploaded file exceeds maximum allowed size. */
public class FileTooLargeException extends RuntimeException {

    private final long fileSize;
    private final long maxSize;

    public FileTooLargeException(long fileSize, long maxSize) {
        super(String.format("File size %d bytes exceeds maximum allowed size of %d bytes", fileSize, maxSize));
        this.fileSize = fileSize;
        this.maxSize = maxSize;
    }

    public FileTooLargeException(String message) {
        super(message);
        this.fileSize = 0;
        this.maxSize = 0;
    }

    public long getFileSize() {
        return fileSize;
    }

    public long getMaxSize() {
        return maxSize;
    }
}
