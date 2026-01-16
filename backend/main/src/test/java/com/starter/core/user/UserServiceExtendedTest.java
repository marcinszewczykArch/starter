package com.starter.core.user;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.starter.core.email.EmailService;
import com.starter.core.exception.EmailAlreadyExistsException;
import com.starter.core.exception.InvalidCredentialsException;
import com.starter.shared.util.TokenGenerator;

import java.time.Instant;
import java.util.Optional;

/** Unit tests for UserService extended methods (email change, delete account). */
@ExtendWith(MockitoExtension.class)
class UserServiceExtendedTest {

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
    void requestEmailChange_shouldSendVerificationEmail_whenPasswordCorrect() {
        // given
        Long userId = 1L;
        String currentPassword = "password123";
        String newEmail = "newemail@example.com";
        String hashedPassword = "$2a$10$hashed";
        Instant now = Instant.now();

        User user = User.builder()
            .id(userId)
            .email("old@example.com")
            .password(hashedPassword)
            .role(User.Role.USER)
            .emailVerified(true)
            .createdAt(now)
            .updatedAt(now)
            .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(currentPassword, hashedPassword)).thenReturn(true);
        when(userRepository.existsByEmailIncludingArchived(newEmail.toLowerCase())).thenReturn(false);
        when(tokenGenerator.generate()).thenReturn("token123");

        // when
        userService.requestEmailChange(userId, newEmail, currentPassword);

        // then
        verify(passwordEncoder).matches(currentPassword, hashedPassword);
        verify(userRepository).existsByEmailIncludingArchived(newEmail.toLowerCase());
        verify(userRepository).setEmailChangeToken(eq(userId), eq(newEmail.toLowerCase()), any(), any());
        verify(emailService).sendEmailChangeVerificationEmail(eq(newEmail.toLowerCase()), eq("token123"));
    }

    @Test
    void requestEmailChange_shouldThrowException_whenPasswordIncorrect() {
        // given
        Long userId = 1L;
        String wrongPassword = "wrong";
        String newEmail = "newemail@example.com";
        String hashedPassword = "$2a$10$hashed";
        Instant now = Instant.now();

        User user = User.builder()
            .id(userId)
            .email("old@example.com")
            .password(hashedPassword)
            .role(User.Role.USER)
            .emailVerified(true)
            .createdAt(now)
            .updatedAt(now)
            .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(wrongPassword, hashedPassword)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.requestEmailChange(userId, newEmail, wrongPassword))
            .isInstanceOf(InvalidCredentialsException.class)
            .hasMessageContaining("Password is incorrect");

        verify(userRepository, never()).setEmailChangeToken(any(), any(), any(), any());
        verify(emailService, never()).sendEmailChangeVerificationEmail(any(), any());
    }

    @Test
    void requestEmailChange_shouldThrowException_whenEmailAlreadyTaken() {
        // given
        Long userId = 1L;
        String currentPassword = "password123";
        String newEmail = "taken@example.com";
        String hashedPassword = "$2a$10$hashed";
        Instant now = Instant.now();

        User user = User.builder()
            .id(userId)
            .email("old@example.com")
            .password(hashedPassword)
            .role(User.Role.USER)
            .emailVerified(true)
            .createdAt(now)
            .updatedAt(now)
            .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(currentPassword, hashedPassword)).thenReturn(true);
        when(userRepository.existsByEmailIncludingArchived(newEmail.toLowerCase())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.requestEmailChange(userId, newEmail, currentPassword))
            .isInstanceOf(EmailAlreadyExistsException.class)
            .hasMessageContaining("Email is already taken");

        verify(userRepository, never()).setEmailChangeToken(any(), any(), any(), any());
        verify(emailService, never()).sendEmailChangeVerificationEmail(any(), any());
    }

    @Test
    void deleteAccount_shouldArchiveUser_whenPasswordCorrect() {
        // given
        Long userId = 1L;
        String password = "password123";
        String hashedPassword = "$2a$10$hashed";
        Instant now = Instant.now();

        User user = User.builder()
            .id(userId)
            .email("test@example.com")
            .password(hashedPassword)
            .role(User.Role.USER)
            .emailVerified(true)
            .createdAt(now)
            .updatedAt(now)
            .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, hashedPassword)).thenReturn(true);

        // when
        userService.deleteAccount(userId, password);

        // then
        verify(passwordEncoder).matches(password, hashedPassword);
        verify(userRepository).archiveUser(userId);
    }

    @Test
    void deleteAccount_shouldThrowException_whenPasswordIncorrect() {
        // given
        Long userId = 1L;
        String wrongPassword = "wrong";
        String hashedPassword = "$2a$10$hashed";
        Instant now = Instant.now();

        User user = User.builder()
            .id(userId)
            .email("test@example.com")
            .password(hashedPassword)
            .role(User.Role.USER)
            .emailVerified(true)
            .createdAt(now)
            .updatedAt(now)
            .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(wrongPassword, hashedPassword)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.deleteAccount(userId, wrongPassword))
            .isInstanceOf(InvalidCredentialsException.class)
            .hasMessageContaining("Password is incorrect");

        verify(userRepository, never()).archiveUser(any());
    }
}
