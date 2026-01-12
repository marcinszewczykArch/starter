package com.starter.domain;

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
    private Instant createdAt;
    private Instant updatedAt;

    /** User roles. */
    public enum Role {
        USER,
        ADMIN
    }
}
