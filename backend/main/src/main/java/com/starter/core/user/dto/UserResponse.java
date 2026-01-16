package com.starter.core.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.starter.core.security.UserPrincipal;
import com.starter.core.user.User;

/** Response DTO for user information. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String email;
    private String role;
    private boolean emailVerified;
    private String avatarUrl; // URL to fetch avatar, null if no avatar

    /** Create UserResponse from User entity. */
    public static UserResponse fromUser(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .role(user.getRole().name())
            .emailVerified(user.isEmailVerified())
            .avatarUrl(user.getAvatar() != null ? "/api/users/" + user.getId() + "/avatar" : null)
            .build();
    }

    /** Create UserResponse from UserPrincipal (without avatar check - use fromUser for full data). */
    public static UserResponse fromPrincipal(UserPrincipal principal) {
        return UserResponse.builder()
            .id(principal.getId())
            .email(principal.getEmail())
            .role(principal.getRole().name())
            .emailVerified(principal.isEmailVerified())
            .avatarUrl(null) // Avatar not available from principal, use fromUser instead
            .build();
    }
}
