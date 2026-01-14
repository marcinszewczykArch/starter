package com.starter.core.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.starter.core.admin.DeviceInfoService.DeviceInfo;
import com.starter.core.admin.GeoLocationService.GeoLocation;
import com.starter.core.user.UserRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Service for recording and retrieving login history.
 * Handles both successful and failed login attempts.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginHistoryService {

    private final LoginHistoryRepository loginHistoryRepository;
    private final GeoLocationService geoLocationService;
    private final DeviceInfoService deviceInfoService;
    private final UserRepository userRepository;

    /**
     * Record a successful login attempt.
     * Runs asynchronously to not block the login response.
     *
     * @param userId       ID of the user who logged in
     * @param ipAddress    IP address of the request
     * @param userAgent    User-Agent header
     * @param gpsLatitude  GPS latitude (if provided by browser)
     * @param gpsLongitude GPS longitude (if provided by browser)
     */
    @Async
    public void recordSuccessfulLogin(
        Long userId,
        String ipAddress,
        String userAgent,
        BigDecimal gpsLatitude,
        BigDecimal gpsLongitude
    ) {
        try {
            LoginHistory history = buildLoginHistory(
                userId, null, ipAddress, userAgent, gpsLatitude, gpsLongitude, true, null
            );
            loginHistoryRepository.save(history);

            // Update last_login_at on user
            userRepository.updateLastLoginAt(userId, Instant.now());

            log.info("Recorded successful login for user {} from {}", userId, ipAddress);
        } catch (Exception e) {
            log.error("Failed to record login history for user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Record a failed login attempt.
     * Runs asynchronously to not block the login response.
     *
     * @param attemptedEmail Email that was attempted
     * @param userId         User ID if found (null if user doesn't exist)
     * @param ipAddress      IP address of the request
     * @param userAgent      User-Agent header
     * @param failureReason  Reason for failure
     */
    @Async
    public void recordFailedLogin(
        String attemptedEmail,
        Long userId,
        String ipAddress,
        String userAgent,
        String failureReason
    ) {
        try {
            LoginHistory history = buildLoginHistory(
                userId, attemptedEmail, ipAddress, userAgent, null, null, false, failureReason
            );
            loginHistoryRepository.save(history);

            log.info(
                "Recorded failed login attempt for {} from {}: {}",
                attemptedEmail, ipAddress, failureReason
            );
        } catch (Exception e) {
            log.error("Failed to record failed login for {}: {}", attemptedEmail, e.getMessage());
        }
    }

    /**
     * Get login history for a user with pagination.
     *
     * @param userId User ID
     * @param page   Page number (0-based)
     * @param size   Page size
     * @return List of login history records
     */
    public List<LoginHistory> getLoginHistory(Long userId, int page, int size) {
        return loginHistoryRepository.findByUserId(userId, page, size);
    }

    /**
     * Get total count of login history records for a user.
     *
     * @param userId User ID
     * @return Total count
     */
    public long getLoginHistoryCount(Long userId) {
        return loginHistoryRepository.countByUserId(userId);
    }

    private LoginHistory buildLoginHistory(
        Long userId,
        String attemptedEmail,
        String ipAddress,
        String userAgent,
        BigDecimal gpsLatitude,
        BigDecimal gpsLongitude,
        boolean success,
        String failureReason
    ) {
        // Parse device info
        DeviceInfo deviceInfo = deviceInfoService.parse(userAgent);

        // Determine location
        BigDecimal latitude = gpsLatitude;
        BigDecimal longitude = gpsLongitude;
        LoginHistory.LocationSource locationSource = null;
        String country = null;
        String city = null;

        if (gpsLatitude != null && gpsLongitude != null) {
            // GPS coordinates provided by browser
            locationSource = LoginHistory.LocationSource.GPS;
            log.debug("Using GPS location: {}, {}", gpsLatitude, gpsLongitude);
        } else {
            // Fall back to IP geolocation
            GeoLocation geoLocation = geoLocationService.lookup(ipAddress);
            if (geoLocation != null) {
                latitude = geoLocation.getLatitude();
                longitude = geoLocation.getLongitude();
                country = geoLocation.getCountry();
                city = geoLocation.getCity();
                locationSource = LoginHistory.LocationSource.IP;
                log.debug(
                    "Using IP location: {}, {} ({}, {})",
                    latitude, longitude, city, country
                );
            }
        }

        return LoginHistory.builder()
            .userId(userId)
            .loggedInAt(Instant.now())
            .success(success)
            .failureReason(failureReason)
            .attemptedEmail(attemptedEmail)
            .latitude(latitude)
            .longitude(longitude)
            .locationSource(locationSource)
            .country(country)
            .city(city)
            .ipAddress(ipAddress)
            .userAgent(
                userAgent != null && userAgent.length() > 500
                    ? userAgent.substring(0, 500)
                    : userAgent
            )
            .deviceType(deviceInfo.getDeviceType())
            .browser(deviceInfo.getBrowser())
            .os(deviceInfo.getOs())
            .build();
    }
}
