package com.starter.feature.files;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Validates content types against allowed patterns.
 */
@Slf4j
@Component
public class ContentTypeValidator {

    private final List<String> allowedPatterns;

    public ContentTypeValidator(@Value("${app.storage.allowed-content-types}") String allowedContentTypes) {
        this.allowedPatterns = Arrays.asList(allowedContentTypes.split(","));
    }

    /**
     * Validate content type against allowed patterns.
     *
     * @param contentType Content type to validate
     * @throws IllegalArgumentException if content type is not allowed
     */
    public void validate(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            throw new IllegalArgumentException("Content type cannot be null or empty");
        }

        boolean allowed = allowedPatterns.stream()
            .anyMatch(pattern -> matchesPattern(contentType, pattern.trim()));

        if (!allowed) {
            log.warn("Content type not allowed: {}", contentType);
            throw new IllegalArgumentException(
                String.format(
                    "Content type '%s' is not allowed. Allowed types: %s",
                    contentType, allowedPatterns
                )
            );
        }
    }

    private boolean matchesPattern(String contentType, String pattern) {
        // Case-insensitive matching
        String contentTypeLower = contentType.toLowerCase(Locale.ROOT);
        String patternLower = pattern.toLowerCase(Locale.ROOT);

        if (patternLower.endsWith("/*")) {
            // Wildcard pattern: "image/*" matches "image/jpeg", "image/png", etc.
            String baseType = patternLower.substring(0, patternLower.length() - 2);
            return contentTypeLower.startsWith(baseType + "/");
        } else {
            // Exact match: "application/pdf" matches only "application/pdf"
            return contentTypeLower.equals(patternLower);
        }
    }
}
