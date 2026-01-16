package com.starter.core.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.starter.core.email.EmailService;
import com.starter.core.exception.EmailAlreadyExistsException;
import com.starter.shared.util.TokenGenerator;

import java.time.Instant;
import java.util.Optional;

/** Unit tests for UserService. */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenGenerator tokenGenerator;

    @Mock
    private EmailService emailService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder, tokenGenerator, emailService);
    }

    @Test
    void findByEmail_shouldDelegateToRepository() {
        // given
        String email = "test@example.com";
        Instant now = Instant.now();
        User user =
            User.builder()
                .id(1L)
                .email(email)
                .password("hash")
                .role(User.Role.USER)
                .createdAt(now)
                .updatedAt(now)
                .build();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // when
        Optional<User> result = userService.findByEmail(email);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(email);
        verify(userRepository).findByEmail(email);
    }

    @Test
    void findById_shouldDelegateToRepository() {
        // given
        Long id = 1L;
        Instant now = Instant.now();
        User user =
            User.builder()
                .id(id)
                .email("test@example.com")
                .password("hash")
                .role(User.Role.USER)
                .createdAt(now)
                .updatedAt(now)
                .build();
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        // when
        Optional<User> result = userService.findById(id);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(id);
        verify(userRepository).findById(id);
    }

    @Test
    void emailExists_shouldDelegateToRepository() {
        // given
        String email = "exists@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // when
        boolean result = userService.emailExists(email);

        // then
        assertThat(result).isTrue();
        verify(userRepository).existsByEmail(email);
    }

    @Test
    void createUser_shouldSaveUserWithProvidedData() {
        // given
        String email = "new@example.com";
        String hashedPassword = "hashedPassword";
        User.Role role = User.Role.USER;
        Instant now = Instant.now();
        User savedUser =
            User.builder()
                .id(1L)
                .email(email)
                .password(hashedPassword)
                .role(role)
                .createdAt(now)
                .updatedAt(now)
                .build();
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // when
        User result = userService.createUser(email, hashedPassword, role);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getRole()).isEqualTo(role);
        verify(userRepository).existsByEmail(email);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_shouldThrowException_whenEmailAlreadyExists() {
        // given
        String email = "existing@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.createUser(email, "password", User.Role.USER))
            .isInstanceOf(EmailAlreadyExistsException.class)
            .hasMessageContaining(email);

        verify(userRepository).existsByEmail(email);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_shouldHandleRaceCondition_whenDuplicateKeyException() {
        // given - simulates race condition: existsByEmail returns false but save throws
        String email = "race@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.save(any(User.class)))
            .thenThrow(new DuplicateKeyException("duplicate key"));

        // when & then - should convert DB exception to our custom exception
        assertThatThrownBy(() -> userService.createUser(email, "password", User.Role.USER))
            .isInstanceOf(EmailAlreadyExistsException.class)
            .hasMessageContaining(email);

        verify(userRepository).existsByEmail(email);
        verify(userRepository).save(any(User.class));
    }
}
