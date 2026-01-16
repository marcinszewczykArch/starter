package com.starter.core.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/** Domain entity representing a user. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;
    private String email;
    private String password;
    private Role role;
    private boolean emailVerified;
    private String verificationToken;
    private Instant verificationTokenExpiresAt;
    private String passwordResetToken;
    private Instant passwordResetTokenExpiresAt;
    private Instant lastLoginAt;
    private Instant createdAt;
    private Instant updatedAt;

    // Profile fields
    private String displayName;
    private String bio;
    private String website;
    private String company;
    private String location;
    private String country;

    // Avatar
    private byte[] avatar;
    private String avatarContentType;

    // Soft delete
    private Instant archivedAt;

    // Email change
    private String pendingEmail;
    private String emailChangeToken;
    private Instant emailChangeTokenExpiresAt;

    /** User roles. */
    public enum Role {
        USER,
        ADMIN
    }
}
