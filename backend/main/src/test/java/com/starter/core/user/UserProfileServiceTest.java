package com.starter.core.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.starter.core.user.dto.UpdateProfileRequest;
import com.starter.core.user.dto.UserProfileDto;

import java.time.Instant;
import java.util.Optional;

/** Unit tests for UserProfileService. */
@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserProfileService userProfileService;

    @BeforeEach
    void setUp() {
        userProfileService = new UserProfileService(userRepository);
    }

    @Test
    void getProfile_shouldReturnUserProfile() {
        // given
        Long userId = 1L;
        Instant now = Instant.now();
        User user = User.builder()
            .id(userId)
            .email("test@example.com")
            .role(User.Role.USER)
            .emailVerified(true)
            .displayName("Test User")
            .bio("Test bio")
            .website("https://example.com")
            .company("Test Company")
            .location("Wrocław")
            .country("PL")
            .createdAt(now)
            .updatedAt(now)
            .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        UserProfileDto result = userProfileService.getProfile(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getDisplayName()).isEqualTo("Test User");
        assertThat(result.getBio()).isEqualTo("Test bio");
        assertThat(result.getWebsite()).isEqualTo("https://example.com");
        assertThat(result.getCompany()).isEqualTo("Test Company");
        assertThat(result.getLocation()).isEqualTo("Wrocław");
        assertThat(result.getCountry()).isEqualTo("PL");
        verify(userRepository).findById(userId);
    }

    @Test
    void getProfile_shouldThrowException_whenUserNotFound() {
        // given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userProfileService.getProfile(userId))
            .isInstanceOf(com.starter.core.exception.ResourceNotFoundException.class)
            .hasMessageContaining("User not found");
    }

    @Test
    void updateProfile_shouldUpdateAllFields() {
        // given
        Long userId = 1L;
        Instant now = Instant.now();
        User existingUser = User.builder()
            .id(userId)
            .email("test@example.com")
            .role(User.Role.USER)
            .emailVerified(true)
            .createdAt(now)
            .updatedAt(now)
            .build();

        User updatedUser = User.builder()
            .id(userId)
            .email("test@example.com")
            .role(User.Role.USER)
            .emailVerified(true)
            .displayName("Updated Name")
            .bio("Updated bio")
            .website("https://updated.com")
            .company("Updated Company")
            .location("Kraków")
            .country("PL")
            .createdAt(now)
            .updatedAt(now)
            .build();

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setDisplayName("Updated Name");
        request.setBio("Updated bio");
        request.setWebsite("https://updated.com");
        request.setCompany("Updated Company");
        request.setLocation("Kraków");
        request.setCountry("PL");

        when(userRepository.findById(userId))
            .thenReturn(Optional.of(existingUser))
            .thenReturn(Optional.of(updatedUser));

        // when
        UserProfileDto result = userProfileService.updateProfile(userId, request);

        // then
        assertThat(result.getDisplayName()).isEqualTo("Updated Name");
        assertThat(result.getBio()).isEqualTo("Updated bio");
        assertThat(result.getWebsite()).isEqualTo("https://updated.com");
        assertThat(result.getCompany()).isEqualTo("Updated Company");
        assertThat(result.getLocation()).isEqualTo("Kraków");
        assertThat(result.getCountry()).isEqualTo("PL");

        verify(userRepository).updateProfile(
            eq(userId),
            eq("Updated Name"),
            eq("Updated bio"),
            eq("https://updated.com"),
            eq("Updated Company"),
            eq("Kraków"),
            eq("PL")
        );
    }

    @Test
    void updateProfile_shouldNormalizeEmptyStringsToNull() {
        // given
        Long userId = 1L;
        Instant now = Instant.now();
        User user = User.builder()
            .id(userId)
            .email("test@example.com")
            .role(User.Role.USER)
            .emailVerified(true)
            .createdAt(now)
            .updatedAt(now)
            .build();

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setDisplayName("   "); // whitespace only
        request.setBio("");
        request.setWebsite("  ");
        request.setCompany(null);
        request.setLocation(null);
        request.setCountry("");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        userProfileService.updateProfile(userId, request);

        // then
        verify(userRepository).updateProfile(
            eq(userId),
            eq((String) null), // empty string normalized to null
            eq((String) null),
            eq((String) null),
            eq((String) null),
            eq((String) null),
            eq((String) null)
        );
    }
}
