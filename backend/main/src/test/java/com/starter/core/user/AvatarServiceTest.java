package com.starter.core.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import javax.imageio.ImageIO;

/** Unit tests for AvatarService. */
@ExtendWith(MockitoExtension.class)
class AvatarServiceTest {

    @Mock
    private UserRepository userRepository;

    private AvatarService avatarService;

    @BeforeEach
    void setUp() {
        avatarService = new AvatarService(userRepository);
    }

    @Test
    void saveAvatar_shouldProcessAndSaveImage() throws IOException {
        // given
        Long userId = 1L;
        byte[] imageBytes = createTestImageBytes();
        MultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            imageBytes
        );

        User user = User.builder()
            .id(userId)
            .email("test@example.com")
            .role(User.Role.USER)
            .emailVerified(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        avatarService.saveAvatar(userId, file);

        // then
        verify(userRepository).updateAvatar(eq(userId), any(byte[].class), eq("image/jpeg"));
    }

    @Test
    void saveAvatar_shouldThrowException_whenFileIsNull() {
        // given
        Long userId = 1L;
        User user = User.builder()
            .id(userId)
            .email("test@example.com")
            .role(User.Role.USER)
            .emailVerified(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> avatarService.saveAvatar(userId, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Avatar file is required");
    }

    @Test
    void saveAvatar_shouldThrowException_whenFileIsEmpty() {
        // given
        Long userId = 1L;
        MultipartFile emptyFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[0]);
        User user = User.builder()
            .id(userId)
            .email("test@example.com")
            .role(User.Role.USER)
            .emailVerified(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> avatarService.saveAvatar(userId, emptyFile))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Avatar file is required");
    }

    @Test
    void saveAvatar_shouldThrowException_whenFileTooLarge() throws IOException {
        // given
        Long userId = 1L;
        // Create a file larger than 5MB
        byte[] largeBytes = new byte[6 * 1024 * 1024]; // 6MB
        MultipartFile largeFile = new MockMultipartFile(
            "file",
            "large.jpg",
            "image/jpeg",
            largeBytes
        );
        User user = User.builder()
            .id(userId)
            .email("test@example.com")
            .role(User.Role.USER)
            .emailVerified(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> avatarService.saveAvatar(userId, largeFile))
            .isInstanceOf(com.starter.core.exception.FileTooLargeException.class)
            .hasMessageContaining("exceeds maximum allowed size");
    }

    @Test
    void saveAvatar_shouldThrowException_whenNotAnImage() {
        // given
        Long userId = 1L;
        MultipartFile textFile = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "not an image".getBytes()
        );
        User user = User.builder()
            .id(userId)
            .email("test@example.com")
            .role(User.Role.USER)
            .emailVerified(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> avatarService.saveAvatar(userId, textFile))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("File must be an image");
    }

    @Test
    void saveAvatar_shouldThrowException_whenInvalidImage() {
        // given
        Long userId = 1L;
        MultipartFile invalidFile = new MockMultipartFile(
            "file",
            "fake.jpg",
            "image/jpeg",
            "fake image data".getBytes()
        );
        User user = User.builder()
            .id(userId)
            .email("test@example.com")
            .role(User.Role.USER)
            .emailVerified(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> avatarService.saveAvatar(userId, invalidFile))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid image file");
    }

    @Test
    void deleteAvatar_shouldCallRepository() {
        // given
        Long userId = 1L;
        User user = User.builder()
            .id(userId)
            .email("test@example.com")
            .role(User.Role.USER)
            .emailVerified(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        avatarService.deleteAvatar(userId);

        // then
        verify(userRepository).findById(userId);
        verify(userRepository).deleteAvatar(userId);
    }

    @Test
    void getAvatar_shouldReturnAvatarData_whenAvatarExists() {
        // given
        Long userId = 1L;
        byte[] avatarBytes = new byte[]{1, 2, 3, 4};
        Instant now = Instant.now();

        User user = User.builder()
            .id(userId)
            .email("test@example.com")
            .role(User.Role.USER)
            .emailVerified(true)
            .avatar(avatarBytes)
            .avatarContentType("image/jpeg")
            .createdAt(now)
            .updatedAt(now)
            .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        AvatarService.AvatarData result = avatarService.getAvatar(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.bytes()).isEqualTo(avatarBytes);
        assertThat(result.contentType()).isEqualTo("image/jpeg");
    }

    @Test
    void getAvatar_shouldReturnNull_whenNoAvatar() {
        // given
        Long userId = 1L;
        Instant now = Instant.now();

        User user = User.builder()
            .id(userId)
            .email("test@example.com")
            .role(User.Role.USER)
            .emailVerified(true)
            .avatar(null)
            .createdAt(now)
            .updatedAt(now)
            .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        AvatarService.AvatarData result = avatarService.getAvatar(userId);

        // then
        assertThat(result).isNull();
    }

    @Test
    void getAvatar_shouldUseDefaultContentType_whenContentTypeIsNull() {
        // given
        Long userId = 1L;
        byte[] avatarBytes = new byte[]{1, 2, 3, 4};
        Instant now = Instant.now();

        User user = User.builder()
            .id(userId)
            .email("test@example.com")
            .role(User.Role.USER)
            .emailVerified(true)
            .avatar(avatarBytes)
            .avatarContentType(null)
            .createdAt(now)
            .updatedAt(now)
            .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        AvatarService.AvatarData result = avatarService.getAvatar(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.contentType()).isEqualTo("image/jpeg");
    }

    @Test
    void getAvatar_shouldThrowException_whenUserNotFound() {
        // given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> avatarService.getAvatar(userId))
            .isInstanceOf(com.starter.core.exception.ResourceNotFoundException.class)
            .hasMessageContaining("User not found");
    }

    /**
     * Create a simple test image as byte array.
     */
    private byte[] createTestImageBytes() throws IOException {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        return baos.toByteArray();
    }
}
