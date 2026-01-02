package com.starter;

import static org.assertj.core.api.Assertions.assertThat;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

/** Test to verify that Flyway migrations run successfully. */
class FlywayMigrationTest extends BaseIntegrationTest {

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcClient jdbcClient;

    @Test
    void flywayMigrationsApplied() {
        var info = flyway.info();

        assertThat(info.applied()).isNotEmpty();
        assertThat(info.pending()).isEmpty();
    }

    @Test
    void examplesTableExists() {
        Long count = jdbcClient.sql("SELECT COUNT(*) FROM examples").query(Long.class).single();

        assertThat(count).isNotNull();
        assertThat(count).isGreaterThanOrEqualTo(0);
    }

    @Test
    void sampleDataLoaded() {
        Long count =
            jdbcClient
                .sql("SELECT COUNT(*) FROM examples WHERE name LIKE 'Sample%'")
                .query(Long.class)
                .single();

        assertThat(count).isGreaterThanOrEqualTo(2L);
    }
}
