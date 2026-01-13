package com.starter.core.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.starter.core.user.User;

import java.util.Collection;
import java.util.List;

/** Represents the authenticated user in Spring Security context. */
@Data
@Builder
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final User.Role role;
    private final boolean emailVerified;

    /** Create UserPrincipal from User entity. */
    public static UserPrincipal fromUser(User user) {
        return UserPrincipal.builder()
            .id(user.getId())
            .email(user.getEmail())
            .role(user.getRole())
            .emailVerified(user.isEmailVerified())
            .build();
    }

    /** Check if user has ADMIN role. */
    public boolean isAdmin() {
        return role == User.Role.ADMIN;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return null; // Not needed for JWT auth
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
