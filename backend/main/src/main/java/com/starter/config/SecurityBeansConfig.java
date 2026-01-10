package com.starter.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/** Common security beans used across all profiles. */
@Configuration
public class SecurityBeansConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * In-memory user for Basic Auth (Swagger access on production). Credentials come from
     * environment variables: SWAGGER_USER and SWAGGER_PASSWORD.
     */
    @Bean
    @Profile("prod")
    public UserDetailsService swaggerUserDetailsService(
        @Value("${swagger.user:admin}") String username,
        @Value("${swagger.password:admin}") String password,
        PasswordEncoder passwordEncoder
    ) {
        return new InMemoryUserDetailsManager(
            User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .roles("SWAGGER")
                .build()
        );
    }
}
