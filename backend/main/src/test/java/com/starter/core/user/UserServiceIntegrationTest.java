package com.starter.core.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.starter.BaseIntegrationTest;
import com.starter.core.exception.EmailAlreadyExistsException;

/** Integration tests for UserService. */
class UserServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserService userService;

    @Test
    void createUser_shouldCreateAndReturnUser() {
        // when
        User user = userService.createUser("test@example.com", "hashedPassword", User.Role.USER);

        // then
        assertThat(user.getId()).isNotNull();
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getRole()).isEqualTo(User.Role.USER);
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
    }

    @Test
    void createUser_shouldThrowException_whenEmailAlreadyExists() {
        // given
        userService.createUser("duplicate@example.com", "password1", User.Role.USER);

        // when & then
        assertThatThrownBy(
            () -> userService.createUser("duplicate@example.com", "password2", User.Role.USER)
        )
            .isInstanceOf(EmailAlreadyExistsException.class)
            .hasMessageContaining("duplicate@example.com");
    }

    @Test
    void findByEmail_shouldReturnUser_afterCreation() {
        // given
        userService.createUser("find@example.com", "hashedPassword", User.Role.ADMIN);

        // when
        var found = userService.findByEmail("find@example.com");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getRole()).isEqualTo(User.Role.ADMIN);
    }
}
