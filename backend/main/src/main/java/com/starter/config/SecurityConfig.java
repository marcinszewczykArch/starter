package com.starter.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for production. Protects API endpoints with JWT and Swagger UI with Basic
 * Auth.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("prod")
@RequiredArgsConstructor
public class SecurityConfig {

    private final SecurityConfigurer securityConfigurer;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Apply common settings (stateless, 401 entry point, JWT filter)
        securityConfigurer.applyCommonSecuritySettings(http);
        securityConfigurer.applyPublicEndpointRules(http);

        // Prod-specific: Swagger requires Basic Auth
        http.authorizeHttpRequests(
            auth -> auth.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**")
                .authenticated()
        )
            .httpBasic(Customizer.withDefaults());

        securityConfigurer.applyApiAuthenticationRules(http);

        return http.build();
    }
}
