package com.starter.core.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.starter.core.common.dto.MessageResponse;
import com.starter.core.security.UserPrincipal;
import com.starter.core.user.dto.*;

import jakarta.validation.Valid;

/** REST controller for user profile and account management. */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserProfileService userProfileService;
    private final AvatarService avatarService;
    private final UserService userService;

    /**
     * Get current user's profile.
     */
    @GetMapping("/me/profile")
    public ResponseEntity<UserProfileDto> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        UserProfileDto profile = userProfileService.getProfile(principal.getId());
        return ResponseEntity.ok(profile);
    }

    /**
     * Update current user's profile.
     */
    @PutMapping("/me/profile")
    public ResponseEntity<UserProfileDto> updateProfile(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody UpdateProfileRequest request
    ) {
        UserProfileDto updated = userProfileService.updateProfile(principal.getId(), request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Upload avatar image for current user.
     */
    @PostMapping("/me/avatar")
    public ResponseEntity<MessageResponse> uploadAvatar(
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestParam("file") MultipartFile file
    ) {
        avatarService.saveAvatar(principal.getId(), file);
        return ResponseEntity.ok(new MessageResponse("Avatar uploaded successfully"));
    }

    /**
     * Delete current user's avatar.
     */
    @DeleteMapping("/me/avatar")
    public ResponseEntity<MessageResponse> deleteAvatar(@AuthenticationPrincipal UserPrincipal principal) {
        avatarService.deleteAvatar(principal.getId());
        return ResponseEntity.ok(new MessageResponse("Avatar deleted successfully"));
    }

    /**
     * Get avatar image for a user.
     * Requires authentication - prevents user enumeration and unauthorized access.
     * Returns 404 if user not found or no avatar.
     */
    @GetMapping("/{userId}/avatar")
    public ResponseEntity<byte[]> getAvatar(@PathVariable Long userId) {
        AvatarService.AvatarData avatarData = avatarService.getAvatar(userId);

        if (avatarData == null) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(avatarData.contentType()));
        headers.setContentLength(avatarData.bytes().length);
        headers.setCacheControl("private, max-age=3600"); // Private cache for authenticated users

        return ResponseEntity.ok().headers(headers).body(avatarData.bytes());
    }

    /**
     * Request email change (sends verification to new email).
     */
    @PostMapping("/me/change-email")
    public ResponseEntity<MessageResponse> changeEmail(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody ChangeEmailRequest request
    ) {
        userService.requestEmailChange(principal.getId(), request.getNewEmail(), request.getPassword());
        return ResponseEntity.ok(
            new MessageResponse("Verification email sent to " + request.getNewEmail())
        );
    }

    /**
     * Delete (archive) current user's account.
     */
    @DeleteMapping("/me")
    public ResponseEntity<MessageResponse> deleteAccount(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody DeleteAccountRequest request
    ) {
        userService.deleteAccount(principal.getId(), request.getPassword());
        return ResponseEntity.ok(new MessageResponse("Account deleted successfully"));
    }
}
