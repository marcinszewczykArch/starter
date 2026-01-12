package com.starter.config;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import com.starter.security.JwtAuthenticationFilter;

/**
 * Shared security configuration logic used by both production and development configs. Reduces code
 * duplication and ensures consistent security setup across environments.
 */
@Component
@RequiredArgsConstructor
public class SecurityConfigurer {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /** Apply common security settings: stateless sessions, 401 entry point, JWT filter. */
    public void applyCommonSecuritySettings(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(
                ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }

    /** Apply authorization rules for public endpoints (auth, actuator). */
    public void applyPublicEndpointRules(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(
            auth -> auth.requestMatchers(
                "/api/auth/login",
                "/api/auth/register",
                "/api/auth/verify-email",
                "/api/auth/resend-verification",
                "/api/auth/forgot-password",
                "/api/auth/reset-password"
            )
                .permitAll()
                .requestMatchers("/actuator/**")
                .permitAll()
        );
    }

    /** Apply rules requiring authentication for API endpoints. */
    public void applyApiAuthenticationRules(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(
            auth -> auth.requestMatchers("/api/**").authenticated().anyRequest().permitAll()
        );
    }
}
