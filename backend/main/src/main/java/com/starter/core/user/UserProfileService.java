package com.starter.core.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.starter.core.exception.ResourceNotFoundException;
import com.starter.core.user.dto.UpdateProfileRequest;
import com.starter.core.user.dto.UserProfileDto;

/** Service for user profile operations. */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;

    /**
     * Get user profile by user ID.
     *
     * @param userId User ID
     * @return UserProfileDto
     * @throws ResourceNotFoundException if user not found
     */
    public UserProfileDto getProfile(Long userId) {
        User user = userRepository
            .findById(userId)
            .orElseThrow(() -> {
                log.warn("Profile not found for user ID: {}", userId);
                return new ResourceNotFoundException("User", userId);
            });

        return UserProfileDto.fromUser(user);
    }

    /**
     * Update user profile.
     *
     * @param userId  User ID
     * @param request Update request
     * @return Updated UserProfileDto
     */
    @Transactional
    public UserProfileDto updateProfile(Long userId, UpdateProfileRequest request) {
        log.info("Updating profile for user ID: {}", userId);

        // Normalize empty strings to null
        String displayName = request.getDisplayName() != null && request.getDisplayName().isBlank()
            ? null
            : request.getDisplayName();
        String bio = request.getBio() != null && request.getBio().isBlank() ? null : request.getBio();
        String website = request.getWebsite() != null && request.getWebsite().isBlank()
            ? null
            : request.getWebsite();
        String company = request.getCompany() != null && request.getCompany().isBlank()
            ? null
            : request.getCompany();
        String location = request.getLocation() != null && request.getLocation().isBlank()
            ? null
            : request.getLocation();
        String country = request.getCountry() != null && request.getCountry().isBlank()
            ? null
            : request.getCountry();

        // Validate user exists before update
        var unused = userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        userRepository.updateProfile(userId, displayName, bio, website, company, location, country);

        User updatedUser = userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found after update"));

        log.info("Profile updated successfully for user ID: {}", userId);
        return UserProfileDto.fromUser(updatedUser);
    }
}
