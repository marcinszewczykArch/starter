package com.starter.feature.files;

import lombok.extern.slf4j.Slf4j;

/**
 * Utility for sanitizing filenames to prevent path traversal and security issues.
 */
@Slf4j
public class FilenameSanitizer {

    private static final int MAX_FILENAME_LENGTH = 255;
    private static final String[] DANGEROUS_PATTERNS = {"..", "/", "\\", "\0"};

    /**
     * Sanitize filename to prevent path traversal and security issues.
     *
     * @param filename Original filename
     * @return Sanitized filename safe for storage
     * @throws IllegalArgumentException if filename is null or empty
     */
    public static String sanitize(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }

        // Remove path separators and dangerous patterns
        String sanitized = filename;
        for (String pattern : DANGEROUS_PATTERNS) {
            sanitized = sanitized.replace(pattern, "");
        }

        // Remove leading/trailing dots and spaces
        sanitized = sanitized.trim().replaceAll("^\\.+|\\.+$", "");

        // Keep only safe characters: alphanumeric, dots, dashes, underscores
        sanitized = sanitized.replaceAll("[^a-zA-Z0-9._-]", "_");

        // Limit length
        if (sanitized.length() > MAX_FILENAME_LENGTH) {
            String extension = getExtension(sanitized);
            int maxNameLength = MAX_FILENAME_LENGTH - extension.length();
            sanitized = sanitized.substring(0, maxNameLength) + extension;
        }

        // Ensure not empty after sanitization
        if (sanitized.isBlank()) {
            sanitized = "file_" + System.currentTimeMillis();
        }

        log.debug("Sanitized filename: {} -> {}", filename, sanitized);
        return sanitized;
    }

    private static String getExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot) : "";
    }
}
