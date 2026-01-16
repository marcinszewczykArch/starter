package com.starter.core.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.starter.core.user.User;

import java.time.Instant;

/** DTO for user profile information. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {

    private Long id;
    private String email;
    private String role;
    private boolean emailVerified;
    private String displayName;
    private String bio;
    private String website;
    private String company;
    private String location;
    private String country;
    private String avatarUrl; // URL to fetch avatar
    private Instant createdAt;

    /** Create UserProfileDto from User entity. */
    public static UserProfileDto fromUser(User user) {
        return UserProfileDto.builder()
            .id(user.getId())
            .email(user.getEmail())
            .role(user.getRole().name())
            .emailVerified(user.isEmailVerified())
            .displayName(user.getDisplayName())
            .bio(user.getBio())
            .website(user.getWebsite())
            .company(user.getCompany())
            .location(user.getLocation())
            .country(user.getCountry())
            .avatarUrl(user.getAvatar() != null ? "/api/users/" + user.getId() + "/avatar" : null)
            .createdAt(user.getCreatedAt())
            .build();
    }
}
