package com.starter.core.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.starter.core.user.User;

import java.time.Instant;

/** DTO for user data in admin panel. Excludes sensitive fields like password and tokens. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDto {

    private Long id;
    private String email;
    private String role;
    private boolean emailVerified;
    private Instant lastLoginAt;
    private Instant createdAt;

    /** Create AdminUserDto from User entity. */
    public static AdminUserDto fromUser(User user) {
        return AdminUserDto.builder()
            .id(user.getId())
            .email(user.getEmail())
            .role(user.getRole().name())
            .emailVerified(user.isEmailVerified())
            .lastLoginAt(user.getLastLoginAt())
            .createdAt(user.getCreatedAt())
            .build();
    }
}
