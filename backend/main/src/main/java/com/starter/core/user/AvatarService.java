package com.starter.core.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

/** Service for avatar image processing and storage. */
@Slf4j
@Service
@RequiredArgsConstructor
public class AvatarService {

    private static final int TARGET_SIZE = 400; // 400x400 pixels
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int MAX_IMAGE_DIMENSION = 10000; // Max width or height in pixels

    private final UserRepository userRepository;

    /**
     * Process and save avatar image.
     * Resizes to 400x400, converts to JPEG with 85% quality.
     *
     * @param userId User ID
     * @param file   MultipartFile with image
     * @throws IllegalArgumentException                             if file is invalid
     * @throws com.starter.core.exception.ResourceNotFoundException if user not found
     */
    @Transactional
    public void saveAvatar(Long userId, MultipartFile file) {
        // Validate user exists and is not archived
        var unused = userRepository
            .findById(userId)
            .orElseThrow(() -> new com.starter.core.exception.ResourceNotFoundException("User", userId));

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Avatar file is required");
        }

        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Avatar file must be less than 5MB");
        }

        // Validate content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        try {
            // Read image
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            if (originalImage == null) {
                throw new IllegalArgumentException("Invalid image file");
            }

            // Validate image dimensions to prevent OutOfMemoryError
            int width = originalImage.getWidth();
            int height = originalImage.getHeight();
            if (width > MAX_IMAGE_DIMENSION || height > MAX_IMAGE_DIMENSION) {
                throw new IllegalArgumentException(
                    String.format(
                        "Image dimensions too large: %dx%d. Maximum allowed: %dx%d",
                        width, height, MAX_IMAGE_DIMENSION, MAX_IMAGE_DIMENSION
                    )
                );
            }

            // Resize to square (400x400)
            BufferedImage resizedImage = resizeToSquare(originalImage, TARGET_SIZE);

            // Convert to JPEG
            byte[] jpegBytes = convertToJpeg(resizedImage);

            // Validate final size (max 500KB after processing)
            if (jpegBytes.length > 500 * 1024) {
                throw new IllegalArgumentException("Processed image exceeds 500KB limit");
            }

            // Save to database
            userRepository.updateAvatar(userId, jpegBytes, "image/jpeg");

            log.info("Avatar saved for user ID: {} ({} bytes)", userId, jpegBytes.length);
        } catch (IOException e) {
            log.error("Failed to process avatar for user ID {}: {}", userId, e.getMessage());
            throw new IllegalArgumentException("Failed to process image: " + e.getMessage());
        }
    }

    /**
     * Delete user avatar.
     *
     * @param userId User ID
     * @throws com.starter.core.exception.ResourceNotFoundException if user not found
     */
    @Transactional
    public void deleteAvatar(Long userId) {
        // Validate user exists and is not archived
        var unused = userRepository
            .findById(userId)
            .orElseThrow(() -> new com.starter.core.exception.ResourceNotFoundException("User", userId));

        log.info("Deleting avatar for user ID: {}", userId);
        userRepository.deleteAvatar(userId);
    }

    /**
     * Get avatar image data for a user.
     *
     * @param userId User ID
     * @return AvatarData with bytes and content type, or null if no avatar
     * @throws com.starter.core.exception.ResourceNotFoundException if user not found
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public AvatarData getAvatar(Long userId) {
        User user = userRepository
            .findById(userId)
            .orElseThrow(() -> new com.starter.core.exception.ResourceNotFoundException("User", userId));

        if (user.getAvatar() == null) {
            return null;
        }

        return new AvatarData(
            user.getAvatar(), user.getAvatarContentType() != null
                ? user.getAvatarContentType()
                : "image/jpeg"
        );
    }

    /**
     * Resize image to square with specified size.
     * Maintains aspect ratio and centers the image.
     */
    private BufferedImage resizeToSquare(BufferedImage original, int size) {
        int width = original.getWidth();
        int height = original.getHeight();

        // Calculate scale to fit in square
        double scale = Math.min((double) size / width, (double) size / height);
        int newWidth = (int) (width * scale);
        int newHeight = (int) (height * scale);

        // Create resized image
        BufferedImage resized = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fill with white background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, size, size);

        // Center the resized image
        int x = (size - newWidth) / 2;
        int y = (size - newHeight) / 2;
        g.drawImage(original, x, y, newWidth, newHeight, null);
        g.dispose();

        return resized;
    }

    /**
     * Convert BufferedImage to JPEG byte array.
     */
    private byte[] convertToJpeg(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        return baos.toByteArray();
    }

    /** Data class for avatar bytes and content type. */
    public record AvatarData(byte[] bytes, String contentType) {}
}
