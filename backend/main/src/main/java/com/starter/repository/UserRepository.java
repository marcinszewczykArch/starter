package com.starter.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.starter.domain.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

/** Repository for User entity using JdbcClient. */
@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final JdbcClient jdbcClient;

    private static final RowMapper<User> ROW_MAPPER = new UserRowMapper();

    /** Find user by email. */
    public Optional<User> findByEmail(String email) {
        return jdbcClient
            .sql(
                "SELECT id, email, password, role, created_at, updated_at FROM users WHERE email = :email"
            )
            .param("email", email)
            .query(ROW_MAPPER)
            .optional();
    }

    /** Find user by ID. */
    public Optional<User> findById(Long id) {
        return jdbcClient
            .sql(
                "SELECT id, email, password, role, created_at, updated_at FROM users WHERE id = :id"
            )
            .param("id", id)
            .query(ROW_MAPPER)
            .optional();
    }

    /** Check if user with email exists. Uses EXISTS for optimal performance. */
    public boolean existsByEmail(String email) {
        return Boolean.TRUE.equals(
            jdbcClient
                .sql("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)")
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
                        INSERT INTO users (email, password, role, created_at, updated_at)
                        VALUES (:email, :password, :role, :createdAt, :updatedAt)
                        RETURNING id
                        """
                )
                .param("email", user.getEmail())
                .param("password", user.getPassword())
                .param("role", user.getRole().name())
                .param("createdAt", Timestamp.from(now))
                .param("updatedAt", Timestamp.from(now))
                .query(Long.class)
                .single();

        return User.builder()
            .id(id)
            .email(user.getEmail())
            .password(user.getPassword())
            .role(user.getRole())
            .createdAt(now)
            .updatedAt(now)
            .build();
    }

    private static final class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            return User.builder()
                .id(rs.getLong("id"))
                .email(rs.getString("email"))
                .password(rs.getString("password"))
                .role(User.Role.valueOf(rs.getString("role")))
                .createdAt(rs.getTimestamp("created_at").toInstant())
                .updatedAt(rs.getTimestamp("updated_at").toInstant())
                .build();
        }
    }
}
