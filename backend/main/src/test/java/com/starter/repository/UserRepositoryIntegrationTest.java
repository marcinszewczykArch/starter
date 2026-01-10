package com.starter.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;

import com.starter.BaseIntegrationTest;
import com.starter.domain.User;

import java.util.Optional;

/** Integration tests for UserRepository. */
class UserRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void save_shouldCreateUserAndReturnWithId() {
        // given
        User user =
            User.builder()
                .email("test@example.com")
                .password("hashedPassword123")
                .role(User.Role.USER)
                .build();

        // when
        User saved = userRepository.save(user);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("test@example.com");
        assertThat(saved.getPassword()).isEqualTo("hashedPassword123");
        assertThat(saved.getRole()).isEqualTo(User.Role.USER);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findByEmail_shouldReturnUserWhenExists() {
        // given
        User user =
            User.builder()
                .email("find@example.com")
                .password("hashedPassword123")
                .role(User.Role.ADMIN)
                .build();
        userRepository.save(user);

        // when
        Optional<User> found = userRepository.findByEmail("find@example.com");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("find@example.com");
        assertThat(found.get().getRole()).isEqualTo(User.Role.ADMIN);
    }

    @Test
    void findByEmail_shouldReturnEmptyWhenNotExists() {
        // when
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void findById_shouldReturnUserWhenExists() {
        // given
        User user =
            User.builder()
                .email("byid@example.com")
                .password("hashedPassword123")
                .role(User.Role.USER)
                .build();
        User saved = userRepository.save(user);

        // when
        Optional<User> found = userRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    void existsByEmail_shouldReturnTrueWhenExists() {
        // given
        User user =
            User.builder()
                .email("exists@example.com")
                .password("hashedPassword123")
                .role(User.Role.USER)
                .build();
        userRepository.save(user);

        // when & then
        assertThat(userRepository.existsByEmail("exists@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("notexists@example.com")).isFalse();
    }

    @Test
    void save_shouldThrowException_whenDuplicateEmail() {
        // given
        User user1 =
            User.builder()
                .email("duplicate@example.com")
                .password("hashedPassword123")
                .role(User.Role.USER)
                .build();
        userRepository.save(user1);

        User user2 =
            User.builder()
                .email("duplicate@example.com")
                .password("differentPassword")
                .role(User.Role.ADMIN)
                .build();

        // when & then - database unique constraint violation
        assertThatThrownBy(() -> userRepository.save(user2))
            .isInstanceOf(DuplicateKeyException.class);
    }
}
