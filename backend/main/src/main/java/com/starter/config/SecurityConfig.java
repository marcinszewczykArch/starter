package com.starter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for production. Protects Swagger UI with Basic Auth while keeping API
 * endpoints public.
 */
@Configuration
@EnableWebSecurity
@Profile("prod")
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(
                auth -> auth
                    // Swagger requires authentication
                    .requestMatchers("/swagger-ui/**", "/swagger-ui.html")
                    .authenticated()
                    .requestMatchers("/api-docs/**", "/api-docs")
                    .authenticated()
                    // Everything else is public
                    .anyRequest()
                    .permitAll()
            )
            .httpBasic(Customizer.withDefaults())
            .build();
    }
}
