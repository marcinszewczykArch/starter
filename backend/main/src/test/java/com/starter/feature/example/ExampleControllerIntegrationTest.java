package com.starter.feature.example;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.starter.BaseIntegrationTest;
import com.starter.core.security.JwtUtil;
import com.starter.core.user.User;
import com.starter.core.user.UserService;

/** Integration tests for ExampleController with row-level security. */
@AutoConfigureMockMvc
class ExampleControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    private User user1;
    private User user2;
    private User admin;
    private String user1Token;
    private String user2Token;
    private String adminToken;

    @BeforeEach
    void setUpAuth() {
        // Create test users
        user1 = userService.createUser("user1@example.com", "hashedPassword", User.Role.USER);
        user2 = userService.createUser("user2@example.com", "hashedPassword", User.Role.USER);
        admin = userService.createUser("admin@example.com", "hashedPassword", User.Role.ADMIN);

        user1Token = jwtUtil.generateToken(user1);
        user2Token = jwtUtil.generateToken(user2);
        adminToken = jwtUtil.generateToken(admin);
    }

    @Test
    void getExamples_shouldReturn401_whenNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/example")).andExpect(status().isUnauthorized());
    }

    @Test
    void createExample_shouldAssignCurrentUser() throws Exception {
        mockMvc
            .perform(
                post("/api/v1/example")
                    .header("Authorization", "Bearer " + user1Token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"name": "User1 Example", "description": "Created by user1"}
                        """)
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.userId").value(user1.getId()))
            .andExpect(jsonPath("$.name").value("User1 Example"));
    }

    @Test
    void getExamples_userSeesOnlyOwnExamples() throws Exception {
        // User1 creates an example
        mockMvc
            .perform(
                post("/api/v1/example")
                    .header("Authorization", "Bearer " + user1Token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"name": "User1 Example", "description": "Created by user1"}
                        """)
            )
            .andExpect(status().isCreated());

        // User2 creates an example
        mockMvc
            .perform(
                post("/api/v1/example")
                    .header("Authorization", "Bearer " + user2Token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"name": "User2 Example", "description": "Created by user2"}
                        """)
            )
            .andExpect(status().isCreated());

        // User1 sees only their example
        mockMvc
            .perform(get("/api/v1/example").header("Authorization", "Bearer " + user1Token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].name").value("User1 Example"))
            .andExpect(jsonPath("$[0].userId").value(user1.getId()));

        // User2 sees only their example
        mockMvc
            .perform(get("/api/v1/example").header("Authorization", "Bearer " + user2Token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].name").value("User2 Example"))
            .andExpect(jsonPath("$[0].userId").value(user2.getId()));
    }

    @Test
    void getExamples_adminSeesAllExamples() throws Exception {
        // User1 creates an example
        mockMvc
            .perform(
                post("/api/v1/example")
                    .header("Authorization", "Bearer " + user1Token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"name": "User1 Example", "description": "Created by user1"}
                        """)
            )
            .andExpect(status().isCreated());

        // User2 creates an example
        mockMvc
            .perform(
                post("/api/v1/example")
                    .header("Authorization", "Bearer " + user2Token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"name": "User2 Example", "description": "Created by user2"}
                        """)
            )
            .andExpect(status().isCreated());

        // Admin sees all examples
        mockMvc
            .perform(get("/api/v1/example").header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getExamples_userSeesEmptyList_whenNoExamples() throws Exception {
        // User1 has no examples
        mockMvc
            .perform(get("/api/v1/example").header("Authorization", "Bearer " + user1Token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }
}
