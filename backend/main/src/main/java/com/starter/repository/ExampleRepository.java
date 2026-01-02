package com.starter.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.starter.domain.Example;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/** Repository for Example entity using JdbcClient. */
@Repository
@RequiredArgsConstructor
public class ExampleRepository {

    private final JdbcClient jdbcClient;

    private static final RowMapper<Example> ROW_MAPPER = new ExampleRowMapper();

    /** Find all examples. */
    public List<Example> findAll() {
        return jdbcClient
            .sql(
                "SELECT id, name, description, active, created_at, updated_at FROM examples ORDER BY id"
            )
            .query(ROW_MAPPER)
            .list();
    }

    /** Save a new example. */
    public Example save(Example example) {
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        Long id =
            jdbcClient
                .sql(
                    """
                        INSERT INTO examples (name, description, active, created_at, updated_at)
                        VALUES (:name, :description, :active, :createdAt, :updatedAt)
                        RETURNING id
                        """
                )
                .param("name", example.getName())
                .param("description", example.getDescription())
                .param("active", example.isActive())
                .param("createdAt", now)
                .param("updatedAt", now)
                .query(Long.class)
                .single();

        return Example.builder()
            .id(id)
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
            return Example.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .active(rs.getBoolean("active"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                .build();
        }
    }
}
