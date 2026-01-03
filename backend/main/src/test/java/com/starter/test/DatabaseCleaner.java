package com.starter.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/** Utility to clean database between tests. */
@Component
public class DatabaseCleaner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /** Truncates all application tables. */
    public void truncateAll() {
        try {
            String sql = new ClassPathResource("db/truncate-tables.sql")
                .getContentAsString(StandardCharsets.UTF_8);
            jdbcTemplate.execute(sql);
        } catch (Exception e) {
            throw new RuntimeException("Failed to truncate tables", e);
        }
    }
}
