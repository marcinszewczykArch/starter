package com.starter.core.user;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Repository for User entity using JdbcClient. */
@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final JdbcClient jdbcClient;

    private static final RowMapper<User> ROW_MAPPER = new UserRowMapper();

    private static final String SELECT_FIELDS =
        "id, email, password, role, email_verified, verification_token, "
            + "verification_token_expires_at, password_reset_token, "
            + "password_reset_token_expires_at, last_login_at, created_at, updated_at, "
            + "display_name, bio, website, company, location, country, "
            + "avatar, avatar_content_type, archived_at, "
            + "pending_email, email_change_token, email_change_token_expires_at";

    /** Find user by email (case-insensitive, active users only). */
    public Optional<User> findByEmail(String email) {
        return jdbcClient
            .sql("SELECT " + SELECT_FIELDS + " FROM users WHERE LOWER(email) = LOWER(:email) AND archived_at IS NULL")
            .param("email", email)
            .query(ROW_MAPPER)
            .optional();
    }

    /** Find user by email including archived (for reactivation). */
    public Optional<User> findByEmailIncludingArchived(String email) {
        return jdbcClient
            .sql("SELECT " + SELECT_FIELDS + " FROM users WHERE LOWER(email) = LOWER(:email)")
            .param("email", email)
            .query(ROW_MAPPER)
            .optional();
    }

    /** Find user by ID (active users only). */
    public Optional<User> findById(Long id) {
        return jdbcClient
            .sql("SELECT " + SELECT_FIELDS + " FROM users WHERE id = :id AND archived_at IS NULL")
            .param("id", id)
            .query(ROW_MAPPER)
            .optional();
    }

    /** Find user by ID including archived. */
    public Optional<User> findByIdIncludingArchived(Long id) {
        return jdbcClient
            .sql("SELECT " + SELECT_FIELDS + " FROM users WHERE id = :id")
            .param("id", id)
            .query(ROW_MAPPER)
            .optional();
    }

    /** Find user by verification token. */
    public Optional<User> findByVerificationToken(String token) {
        return jdbcClient
            .sql("SELECT " + SELECT_FIELDS + " FROM users WHERE verification_token = :token")
            .param("token", token)
            .query(ROW_MAPPER)
            .optional();
    }

    /** Check if user with email exists (active users only). Uses EXISTS for optimal performance. */
    public boolean existsByEmail(String email) {
        return Boolean.TRUE.equals(
            jdbcClient
                .sql("SELECT EXISTS(SELECT 1 FROM users WHERE LOWER(email) = LOWER(:email) AND archived_at IS NULL)")
                .param("email", email)
                .query(Boolean.class)
                .single()
        );
    }

    /** Check if user with email exists including archived. */
    public boolean existsByEmailIncludingArchived(String email) {
        return Boolean.TRUE.equals(
            jdbcClient
                .sql("SELECT EXISTS(SELECT 1 FROM users WHERE LOWER(email) = LOWER(:email))")
                .param("email", email)
                .query(Boolean.class)
                .single()
        );
    }

    /** Save a new user. */
    public User save(User user) {
        Instant now = Instant.now();
        Long id =
            jdbcClient
                .sql(
                    """
                        INSERT INTO users (email, password, role, email_verified,
                            verification_token, verification_token_expires_at, created_at, updated_at)
                        VALUES (:email, :password, :role, :emailVerified,
                            :verificationToken, :verificationTokenExpiresAt, :createdAt, :updatedAt)
                        RETURNING id
                        """
                )
                .param("email", user.getEmail())
                .param("password", user.getPassword())
                .param("role", user.getRole().name())
                .param("emailVerified", user.isEmailVerified())
                .param(
                    "verificationToken",
                    user.getVerificationToken()
                )
                .param(
                    "verificationTokenExpiresAt",
                    user.getVerificationTokenExpiresAt() != null
                        ? Timestamp.from(user.getVerificationTokenExpiresAt())
                        : null
                )
                .param("createdAt", Timestamp.from(now))
                .param("updatedAt", Timestamp.from(now))
                .query(Long.class)
                .single();

        return User.builder()
            .id(id)
            .email(user.getEmail())
            .password(user.getPassword())
            .role(user.getRole())
            .emailVerified(user.isEmailVerified())
            .verificationToken(user.getVerificationToken())
            .verificationTokenExpiresAt(user.getVerificationTokenExpiresAt())
            .createdAt(now)
            .updatedAt(now)
            .build();
    }

    /** Update verification token for a user. */
    public void updateVerificationToken(Long userId, String token, Instant expiresAt) {
        jdbcClient
            .sql(
                """
                    UPDATE users
                    SET verification_token = :token,
                        verification_token_expires_at = :expiresAt,
                        updated_at = :updatedAt
                    WHERE id = :userId
                    """
            )
            .param("token", token)
            .param("expiresAt", expiresAt != null ? Timestamp.from(expiresAt) : null)
            .param("updatedAt", Timestamp.from(Instant.now()))
            .param("userId", userId)
            .update();
    }

    /** Mark user email as verified and clear token. */
    public void markEmailVerified(Long userId) {
        jdbcClient
            .sql(
                """
                    UPDATE users
                    SET email_verified = TRUE,
                        verification_token = NULL,
                        verification_token_expires_at = NULL,
                        updated_at = :updatedAt
                    WHERE id = :userId
                    """
            )
            .param("updatedAt", Timestamp.from(Instant.now()))
            .param("userId", userId)
            .update();
    }

    /** Find user by password reset token. */
    public Optional<User> findByPasswordResetToken(String token) {
        return jdbcClient
            .sql("SELECT " + SELECT_FIELDS + " FROM users WHERE password_reset_token = :token")
            .param("token", token)
            .query(ROW_MAPPER)
            .optional();
    }

    /** Update password reset token for a user. */
    public void updatePasswordResetToken(Long userId, String token, Instant expiresAt) {
        jdbcClient
            .sql(
                """
                    UPDATE users
                    SET password_reset_token = :token,
                        password_reset_token_expires_at = :expiresAt,
                        updated_at = :updatedAt
                    WHERE id = :userId
                    """
            )
            .param("token", token)
            .param("expiresAt", expiresAt != null ? Timestamp.from(expiresAt) : null)
            .param("updatedAt", Timestamp.from(Instant.now()))
            .param("userId", userId)
            .update();
    }

    /** Update user password and clear reset token. */
    public void updatePassword(Long userId, String hashedPassword) {
        jdbcClient
            .sql(
                """
                    UPDATE users
                    SET password = :password,
                        password_reset_token = NULL,
                        password_reset_token_expires_at = NULL,
                        updated_at = :updatedAt
                    WHERE id = :userId
                    """
            )
            .param("password", hashedPassword)
            .param("updatedAt", Timestamp.from(Instant.now()))
            .param("userId", userId)
            .update();
    }

    /** Find all users ordered by creation date (active users only). */
    public List<User> findAll() {
        return jdbcClient
            .sql("SELECT " + SELECT_FIELDS + " FROM users WHERE archived_at IS NULL ORDER BY created_at DESC")
            .query(ROW_MAPPER)
            .list();
    }

    /** Update user role. */
    public void updateRole(Long userId, User.Role role) {
        jdbcClient
            .sql(
                """
                    UPDATE users
                    SET role = :role,
                        updated_at = :updatedAt
                    WHERE id = :userId
                    """
            )
            .param("role", role.name())
            .param("updatedAt", Timestamp.from(Instant.now()))
            .param("userId", userId)
            .update();
    }

    /** Delete user by ID. */
    public void deleteById(Long userId) {
        jdbcClient
            .sql("DELETE FROM users WHERE id = :userId")
            .param("userId", userId)
            .update();
    }

    /** Count users with a specific role. */
    public long countByRole(User.Role role) {
        return jdbcClient
            .sql("SELECT COUNT(*) FROM users WHERE role = :role")
            .param("role", role.name())
            .query(Long.class)
            .single();
    }

    /** Update last login timestamp for a user. */
    public void updateLastLoginAt(Long userId, Instant lastLoginAt) {
        jdbcClient
            .sql("UPDATE users SET last_login_at = :lastLoginAt WHERE id = :userId")
            .param("lastLoginAt", Timestamp.from(lastLoginAt))
            .param("userId", userId)
            .update();
    }

    /** Update user profile fields. */
    public void updateProfile(Long userId, String displayName, String bio, String website, String company, String location, String country) {
        jdbcClient
            .sql(
                """
                    UPDATE users
                    SET display_name = :displayName,
                        bio = :bio,
                        website = :website,
                        company = :company,
                        location = :location,
                        country = :country,
                        updated_at = :updatedAt
                    WHERE id = :userId
                    """
            )
            .param("displayName", displayName)
            .param("bio", bio)
            .param("website", website)
            .param("company", company)
            .param("location", location)
            .param("country", country)
            .param("updatedAt", Timestamp.from(Instant.now()))
            .param("userId", userId)
            .update();
    }

    /** Update user avatar. */
    public void updateAvatar(Long userId, byte[] avatar, String contentType) {
        jdbcClient
            .sql(
                """
                    UPDATE users
                    SET avatar = :avatar,
                        avatar_content_type = :contentType,
                        updated_at = :updatedAt
                    WHERE id = :userId
                    """
            )
            .param("avatar", avatar)
            .param("contentType", contentType)
            .param("updatedAt", Timestamp.from(Instant.now()))
            .param("userId", userId)
            .update();
    }

    /** Delete user avatar. */
    public void deleteAvatar(Long userId) {
        jdbcClient
            .sql(
                """
                    UPDATE users
                    SET avatar = NULL,
                        avatar_content_type = NULL,
                        updated_at = :updatedAt
                    WHERE id = :userId
                    """
            )
            .param("updatedAt", Timestamp.from(Instant.now()))
            .param("userId", userId)
            .update();
    }

    /** Soft delete user (set archived_at). */
    public void archiveUser(Long userId) {
        jdbcClient
            .sql(
                """
                    UPDATE users
                    SET archived_at = :archivedAt,
                        updated_at = :updatedAt
                    WHERE id = :userId
                    """
            )
            .param("archivedAt", Timestamp.from(Instant.now()))
            .param("updatedAt", Timestamp.from(Instant.now()))
            .param("userId", userId)
            .update();
    }

    /** Reactivate archived user (clear archived_at). */
    public void reactivateUser(Long userId) {
        jdbcClient
            .sql(
                """
                    UPDATE users
                    SET archived_at = NULL,
                        email_verified = FALSE,
                        updated_at = :updatedAt
                    WHERE id = :userId
                    """
            )
            .param("updatedAt", Timestamp.from(Instant.now()))
            .param("userId", userId)
            .update();
    }

    /** Set email change token. */
    public void setEmailChangeToken(Long userId, String newEmail, String token, Instant expiresAt) {
        jdbcClient
            .sql(
                """
                    UPDATE users
                    SET pending_email = :newEmail,
                        email_change_token = :token,
                        email_change_token_expires_at = :expiresAt,
                        updated_at = :updatedAt
                    WHERE id = :userId
                    """
            )
            .param("newEmail", newEmail)
            .param("token", token)
            .param("expiresAt", expiresAt != null ? Timestamp.from(expiresAt) : null)
            .param("updatedAt", Timestamp.from(Instant.now()))
            .param("userId", userId)
            .update();
    }

    /** Find user by email change token. */
    public Optional<User> findByEmailChangeToken(String token) {
        return jdbcClient
            .sql("SELECT " + SELECT_FIELDS + " FROM users WHERE email_change_token = :token")
            .param("token", token)
            .query(ROW_MAPPER)
            .optional();
    }

    /** Confirm email change (update email and clear pending fields). */
    public void confirmEmailChange(Long userId, String newEmail) {
        jdbcClient
            .sql(
                """
                    UPDATE users
                    SET email = :newEmail,
                        pending_email = NULL,
                        email_change_token = NULL,
                        email_change_token_expires_at = NULL,
                        email_verified = TRUE,
                        updated_at = :updatedAt
                    WHERE id = :userId
                    """
            )
            .param("newEmail", newEmail)
            .param("updatedAt", Timestamp.from(Instant.now()))
            .param("userId", userId)
            .update();
    }

    private static final class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            Timestamp verificationExpires = rs.getTimestamp("verification_token_expires_at");
            Timestamp passwordResetExpires = rs.getTimestamp("password_reset_token_expires_at");
            Timestamp lastLogin = rs.getTimestamp("last_login_at");
            Timestamp archivedAt = rs.getTimestamp("archived_at");
            Timestamp emailChangeExpires = rs.getTimestamp("email_change_token_expires_at");

            return User.builder()
                .id(rs.getLong("id"))
                .email(rs.getString("email"))
                .password(rs.getString("password"))
                .role(User.Role.valueOf(rs.getString("role")))
                .emailVerified(rs.getBoolean("email_verified"))
                .verificationToken(rs.getString("verification_token"))
                .verificationTokenExpiresAt(
                    verificationExpires != null ? verificationExpires.toInstant() : null
                )
                .passwordResetToken(rs.getString("password_reset_token"))
                .passwordResetTokenExpiresAt(
                    passwordResetExpires != null ? passwordResetExpires.toInstant() : null
                )
                .lastLoginAt(lastLogin != null ? lastLogin.toInstant() : null)
                .createdAt(rs.getTimestamp("created_at").toInstant())
                .updatedAt(rs.getTimestamp("updated_at").toInstant())
                .displayName(rs.getString("display_name"))
                .bio(rs.getString("bio"))
                .website(rs.getString("website"))
                .company(rs.getString("company"))
                .location(rs.getString("location"))
                .country(rs.getString("country"))
                .avatar(rs.getBytes("avatar"))
                .avatarContentType(rs.getString("avatar_content_type"))
                .archivedAt(archivedAt != null ? archivedAt.toInstant() : null)
                .pendingEmail(rs.getString("pending_email"))
                .emailChangeToken(rs.getString("email_change_token"))
                .emailChangeTokenExpiresAt(
                    emailChangeExpires != null ? emailChangeExpires.toInstant() : null
                )
                .build();
        }
    }
}
