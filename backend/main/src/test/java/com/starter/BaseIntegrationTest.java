package com.starter;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import com.starter.utils.DatabaseCleaner;

/**
 * Base class for integration tests.
 *
 * <p>Uses PostgreSQL from docker-compose.test.yml (port 5433).
 * Cleans database before each test for isolation.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Autowired
    private DatabaseCleaner databaseCleaner;

    // Mock S3 beans for tests (S3 is not needed in integration tests)
    @MockBean
    private S3Client s3Client;

    @MockBean
    private S3Presigner s3Presigner;

    @BeforeEach
    void cleanDatabase() {
        databaseCleaner.truncateAll();
    }
}
