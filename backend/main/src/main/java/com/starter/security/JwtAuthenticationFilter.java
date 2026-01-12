package com.starter.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.starter.domain.User;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Filter that validates JWT tokens and sets up Spring Security context. */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        // No auth header or not Bearer token
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        // Validate token and get claims in one call (no double parsing)
        Claims claims = jwtUtil.validateToken(token);
        if (claims == null) {
            log.debug("Invalid JWT token");
            filterChain.doFilter(request, response);
            return;
        }

        // Create UserPrincipal directly from token claims (no DB query!)
        UserPrincipal principal = createPrincipalFromClaims(claims);
        if (principal == null) {
            log.debug("Could not create principal from token claims");
            filterChain.doFilter(request, response);
            return;
        }

        // Set up security context
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("Authenticated user: {} with role: {}", principal.getEmail(), principal.getRole());

        filterChain.doFilter(request, response);
    }

    /** Creates UserPrincipal from JWT claims without database query. */
    private UserPrincipal createPrincipalFromClaims(Claims claims) {
        try {
            Long userId = Long.parseLong(claims.getSubject());
            String email = claims.get("email", String.class);
            String roleStr = claims.get("role", String.class);
            Boolean emailVerified = claims.get("emailVerified", Boolean.class);

            if (email == null || roleStr == null) {
                return null;
            }

            User.Role role = User.Role.valueOf(roleStr);
            return UserPrincipal.builder()
                .id(userId)
                .email(email)
                .role(role)
                .emailVerified(emailVerified != null && emailVerified)
                .build();
        } catch (IllegalArgumentException e) {
            log.debug("Failed to parse token claims: {}", e.getMessage());
            return null;
        }
    }
}
