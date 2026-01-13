package com.starter.feature.example;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.List;

/** Repository for Example entity using JdbcClient. */
@Repository
@RequiredArgsConstructor
public class ExampleRepository {

    private final JdbcClient jdbcClient;

    private static final RowMapper<Example> ROW_MAPPER = new ExampleRowMapper();

    /** Find all examples (for admin). */
    public List<Example> findAll() {
        return jdbcClient
            .sql(
                "SELECT id, user_id, name, description, active, created_at, updated_at FROM examples ORDER BY id"
            )
            .query(ROW_MAPPER)
            .list();
    }

    /** Find examples by user ID (for regular users). */
    public List<Example> findByUserId(Long userId) {
        return jdbcClient
            .sql(
                """
                    SELECT id, user_id, name, description, active, created_at, updated_at
                    FROM examples
                    WHERE user_id = :userId
                    ORDER BY id
                    """
            )
            .param("userId", userId)
            .query(ROW_MAPPER)
            .list();
    }

    /** Save a new example. */
    public Example save(Example example) {
        Instant now = Instant.now();
        Long id =
            jdbcClient
                .sql(
                    """
                        INSERT INTO examples (user_id, name, description, active, created_at, updated_at)
                        VALUES (:userId, :name, :description, :active, :createdAt, :updatedAt)
                        RETURNING id
                        """
                )
                .param("userId", example.getUserId(), Types.BIGINT)
                .param("name", example.getName())
                .param("description", example.getDescription())
                .param("active", example.isActive())
                .param("createdAt", Timestamp.from(now))
                .param("updatedAt", Timestamp.from(now))
                .query(Long.class)
                .single();

        return Example.builder()
            .id(id)
            .userId(example.getUserId())
            .name(example.getName())
            .description(example.getDescription())
            .active(example.isActive())
            .createdAt(now)
            .updatedAt(now)
            .build();
    }

    private static final class ExampleRowMapper implements RowMapper<Example> {
        @Override
        public Example mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long userId = rs.getLong("user_id");
            if (rs.wasNull()) {
                userId = null;
            }
            return Example.builder()
                .id(rs.getLong("id"))
                .userId(userId)
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .active(rs.getBoolean("active"))
                .createdAt(rs.getTimestamp("created_at").toInstant())
                .updatedAt(rs.getTimestamp("updated_at").toInstant())
                .build();
        }
    }
}
