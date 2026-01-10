package com.starter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.starter.domain.User;
import com.starter.security.UserPrincipal;

/** Response DTO for user information. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String email;
    private String role;

    /** Create UserResponse from User entity. */
    public static UserResponse fromUser(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .role(user.getRole().name())
            .build();
    }

    /** Create UserResponse from UserPrincipal. */
    public static UserResponse fromPrincipal(UserPrincipal principal) {
        return UserResponse.builder()
            .id(principal.getId())
            .email(principal.getEmail())
            .role(principal.getRole().name())
            .build();
    }
}
