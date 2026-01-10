package com.starter.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import com.starter.BaseIntegrationTest;
import com.starter.domain.User;
import com.starter.security.JwtUtil;
import com.starter.service.UserService;

/** Simple integration test for ExampleController. */
@AutoConfigureMockMvc
class ExampleControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    private String authToken;

    @BeforeEach
    void setUpAuth() {
        // Create a test user and get token
        User user = userService.createUser("test@example.com", "hashedPassword", User.Role.USER);
        authToken = jwtUtil.generateToken(user);
    }

    @Test
    void getAllExamples_shouldReturnOk_whenAuthenticated() throws Exception {
        mockMvc.perform(
            get("/api/v1/example")
                .header("Authorization", "Bearer " + authToken)
        )
            .andExpect(status().isOk());
    }

    @Test
    void getAllExamples_shouldReturn401_whenNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/example"))
            .andExpect(status().isUnauthorized());
    }
}
