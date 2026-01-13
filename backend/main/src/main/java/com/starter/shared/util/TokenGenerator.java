package com.starter.shared.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility component for generating secure random tokens.
 * Uses SecureRandom for cryptographically strong random number generation.
 */
@Component
public class TokenGenerator {

    private static final int DEFAULT_TOKEN_LENGTH = 32;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generate a URL-safe Base64-encoded token with default length (32 bytes).
     *
     * @return URL-safe Base64-encoded token string
     */
    public String generate() {
        return generate(DEFAULT_TOKEN_LENGTH);
    }

    /**
     * Generate a URL-safe Base64-encoded token with specified length.
     *
     * @param byteLength number of random bytes to generate
     * @return URL-safe Base64-encoded token string
     */
    public String generate(int byteLength) {
        byte[] bytes = new byte[byteLength];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
