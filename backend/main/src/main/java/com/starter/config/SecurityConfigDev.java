package com.starter.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for local development and tests. Uses JWT authentication for API
 * endpoints, but allows Swagger without auth.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile({"local", "test"})
@RequiredArgsConstructor
public class SecurityConfigDev {

    private final SecurityConfigurer securityConfigurer;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Apply common settings (stateless, 401 entry point, JWT filter)
        securityConfigurer.applyCommonSecuritySettings(http);
        securityConfigurer.applyPublicEndpointRules(http);

        // Dev-specific: Swagger is public
        http.authorizeHttpRequests(
            auth -> auth.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**").permitAll()
        );

        securityConfigurer.applyApiAuthenticationRules(http);

        return http.build();
    }
}
