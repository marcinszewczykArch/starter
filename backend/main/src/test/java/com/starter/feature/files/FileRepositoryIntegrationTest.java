package com.starter.feature.files;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.starter.BaseIntegrationTest;
import com.starter.core.user.User;
import com.starter.core.user.UserService;

import java.time.Instant;

/**
 * Integration tests for FileRepository.
 */
class FileRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private UserService userService;

    @Test
    void getTotalSizeWithLock_shouldReturnZero_whenNoFiles() {
        // when
        Long totalSize = fileRepository.getTotalSizeWithLock(1L);

        // then
        assertThat(totalSize).isEqualTo(0L);
    }

    @Test
    @Transactional
    void getTotalSizeWithLock_shouldReturnSumOfFileSizes() {
        // given
        User user = userService.createUser("test1@example.com", "hashedPassword", User.Role.USER);
        Long userId = user.getId();

        UserFile file1 = UserFile.builder()
            .userId(userId)
            .filename("file1.txt")
            .s3Key("users/" + userId + "/files/file1.txt")
            .sizeBytes(100L)
            .contentType("text/plain")
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        UserFile file2 = UserFile.builder()
            .userId(userId)
            .filename("file2.txt")
            .s3Key("users/" + userId + "/files/file2.txt")
            .sizeBytes(200L)
            .contentType("text/plain")
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        fileRepository.save(file1);
        fileRepository.save(file2);

        // when
        Long totalSize = fileRepository.getTotalSizeWithLock(userId);

        // then
        assertThat(totalSize).isEqualTo(300L);
    }

    @Test
    @Transactional
    void getTotalSizeWithLock_shouldOnlyCountFilesForSpecificUser() {
        // given
        User user1 = userService.createUser("test2@example.com", "hashedPassword", User.Role.USER);
        User user2 = userService.createUser("test3@example.com", "hashedPassword", User.Role.USER);
        Long userId1 = user1.getId();
        Long userId2 = user2.getId();

        UserFile file1 = UserFile.builder()
            .userId(userId1)
            .filename("file1.txt")
            .s3Key("users/" + userId1 + "/files/file1.txt")
            .sizeBytes(100L)
            .contentType("text/plain")
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        UserFile file2 = UserFile.builder()
            .userId(userId2)
            .filename("file2.txt")
            .s3Key("users/" + userId2 + "/files/file2.txt")
            .sizeBytes(200L)
            .contentType("text/plain")
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        fileRepository.save(file1);
        fileRepository.save(file2);

        // when
        Long totalSize1 = fileRepository.getTotalSizeWithLock(userId1);
        Long totalSize2 = fileRepository.getTotalSizeWithLock(userId2);

        // then
        assertThat(totalSize1).isEqualTo(100L);
        assertThat(totalSize2).isEqualTo(200L);
    }
}
