package com.starter.core.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entity representing a login attempt (successful or failed).
 * Tracks location, device info, and timestamp for security auditing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginHistory {

    private Long id;
    private Long userId;
    private Instant loggedInAt;

    // Success/Failure
    private boolean success;
    private String failureReason;
    private String attemptedEmail; // For failed attempts where user doesn't exist

    // Location
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocationSource locationSource;
    private String country;
    private String city;

    // Device info
    private String ipAddress;
    private String userAgent;
    private String deviceType;
    private String browser;
    private String os;

    /** Source of location data. */
    public enum LocationSource {
        GPS, // Browser geolocation API
        IP   // IP-based geolocation lookup
    }

    /** Common failure reasons for login attempts. */
    public static final class FailureReason {
        public static final String INVALID_PASSWORD = "INVALID_PASSWORD";
        public static final String USER_NOT_FOUND = "USER_NOT_FOUND";

        private FailureReason() {}
    }
}
