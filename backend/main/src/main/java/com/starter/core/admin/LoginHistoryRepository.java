package com.starter.core.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

/** Repository for login history records. */
@Repository
@RequiredArgsConstructor
public class LoginHistoryRepository {

    private final JdbcClient jdbcClient;
    private static final RowMapper<LoginHistory> ROW_MAPPER = new LoginHistoryRowMapper();

    /** Save a login history record. */
    public LoginHistory save(LoginHistory history) {
        Long id = jdbcClient
            .sql("""
                INSERT INTO login_history (
                    user_id, logged_in_at, success, failure_reason, attempted_email,
                    latitude, longitude, location_source, country, city,
                    ip_address, user_agent, device_type, browser, os
                ) VALUES (
                    :userId, :loggedInAt, :success, :failureReason, :attemptedEmail,
                    :latitude, :longitude, :locationSource, :country, :city,
                    :ipAddress, :userAgent, :deviceType, :browser, :os
                )
                RETURNING id
                """)
            .param("userId", history.getUserId())
            .param(
                "loggedInAt", Timestamp.from(
                    history.getLoggedInAt() != null
                        ? history.getLoggedInAt()
                        : Instant.now()
                )
            )
            .param("success", history.isSuccess())
            .param("failureReason", history.getFailureReason())
            .param("attemptedEmail", history.getAttemptedEmail())
            .param("latitude", history.getLatitude())
            .param("longitude", history.getLongitude())
            .param(
                "locationSource", history.getLocationSource() != null
                    ? history.getLocationSource().name()
                    : null
            )
            .param("country", history.getCountry())
            .param("city", history.getCity())
            .param("ipAddress", history.getIpAddress())
            .param("userAgent", history.getUserAgent())
            .param("deviceType", history.getDeviceType())
            .param("browser", history.getBrowser())
            .param("os", history.getOs())
            .query(Long.class)
            .single();

        history.setId(id);
        return history;
    }

    /** Find login history for a user with pagination (newest first). */
    public List<LoginHistory> findByUserId(Long userId, int page, int size) {
        int offset = page * size;
        return jdbcClient
            .sql("""
                SELECT * FROM login_history
                WHERE user_id = :userId
                ORDER BY logged_in_at DESC
                LIMIT :limit OFFSET :offset
                """)
            .param("userId", userId)
            .param("limit", size)
            .param("offset", offset)
            .query(ROW_MAPPER)
            .list();
    }

    /** Count total login history records for a user. */
    public long countByUserId(Long userId) {
        return jdbcClient
            .sql("SELECT COUNT(*) FROM login_history WHERE user_id = :userId")
            .param("userId", userId)
            .query(Long.class)
            .single();
    }

    /** Get the last successful login for a user. */
    public Instant getLastSuccessfulLogin(Long userId) {
        return jdbcClient
            .sql("""
                SELECT logged_in_at FROM login_history
                WHERE user_id = :userId AND success = TRUE
                ORDER BY logged_in_at DESC
                LIMIT 1
                """)
            .param("userId", userId)
            .query((rs, rowNum) -> rs.getTimestamp("logged_in_at").toInstant())
            .optional()
            .orElse(null);
    }

    /** Count failed login attempts for an email in the last N minutes. */
    public long countRecentFailedAttempts(String email, int minutes) {
        return jdbcClient
            .sql("""
                SELECT COUNT(*) FROM login_history
                WHERE attempted_email = :email
                  AND success = FALSE
                  AND logged_in_at > NOW() - INTERVAL '%d minutes'
                """.formatted(minutes))
            .param("email", email)
            .query(Long.class)
            .single();
    }

    private static final class LoginHistoryRowMapper implements RowMapper<LoginHistory> {
        @Override
        public LoginHistory mapRow(ResultSet rs, int rowNum) throws SQLException {
            String locationSourceStr = rs.getString("location_source");
            BigDecimal lat = rs.getBigDecimal("latitude");
            BigDecimal lng = rs.getBigDecimal("longitude");

            return LoginHistory.builder()
                .id(rs.getLong("id"))
                .userId(rs.getObject("user_id", Long.class))
                .loggedInAt(rs.getTimestamp("logged_in_at").toInstant())
                .success(rs.getBoolean("success"))
                .failureReason(rs.getString("failure_reason"))
                .attemptedEmail(rs.getString("attempted_email"))
                .latitude(lat)
                .longitude(lng)
                .locationSource(
                    locationSourceStr != null
                        ? LoginHistory.LocationSource.valueOf(locationSourceStr)
                        : null
                )
                .country(rs.getString("country"))
                .city(rs.getString("city"))
                .ipAddress(rs.getString("ip_address"))
                .userAgent(rs.getString("user_agent"))
                .deviceType(rs.getString("device_type"))
                .browser(rs.getString("browser"))
                .os(rs.getString("os"))
                .build();
        }
    }
}
