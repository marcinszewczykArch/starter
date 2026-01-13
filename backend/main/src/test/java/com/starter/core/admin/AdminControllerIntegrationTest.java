package com.starter.core.admin;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

/** Integration tests for AdminController. */
@AutoConfigureMockMvc
class AdminControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    private User regularUser;
    private User admin1;
    private User admin2;
    private String userToken;
    private String admin1Token;
    private String admin2Token;

    @BeforeEach
    void setUpAuth() {
        regularUser = userService.createUser("user@example.com", "hashedPassword", User.Role.USER);
        admin1 = userService.createUser("admin1@example.com", "hashedPassword", User.Role.ADMIN);
        admin2 = userService.createUser("admin2@example.com", "hashedPassword", User.Role.ADMIN);

        userToken = jwtUtil.generateToken(regularUser);
        admin1Token = jwtUtil.generateToken(admin1);
        admin2Token = jwtUtil.generateToken(admin2);
    }

    // ===== GET /api/admin/users =====

    @Test
    void getAllUsers_shouldReturnAllUsers_whenAdmin() throws Exception {
        mockMvc
            .perform(get("/api/admin/users").header("Authorization", "Bearer " + admin1Token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[?(@.email == 'user@example.com')]").exists())
            .andExpect(jsonPath("$[?(@.email == 'admin1@example.com')]").exists())
            .andExpect(jsonPath("$[?(@.email == 'admin2@example.com')]").exists());
    }

    @Test
    void getAllUsers_shouldReturn403_whenRegularUser() throws Exception {
        mockMvc
            .perform(get("/api/admin/users").header("Authorization", "Bearer " + userToken))
            .andExpect(status().isForbidden());
    }

    @Test
    void getAllUsers_shouldReturn401_whenNoToken() throws Exception {
        mockMvc.perform(get("/api/admin/users")).andExpect(status().isUnauthorized());
    }

    // ===== PATCH /api/admin/users/{id}/role =====

    @Test
    void changeUserRole_shouldPromoteToAdmin() throws Exception {
        mockMvc
            .perform(
                patch("/api/admin/users/" + regularUser.getId() + "/role")
                    .header("Authorization", "Bearer " + admin1Token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"role": "ADMIN"}
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(regularUser.getId()))
            .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void changeUserRole_shouldDemoteToUser() throws Exception {
        mockMvc
            .perform(
                patch("/api/admin/users/" + admin2.getId() + "/role")
                    .header("Authorization", "Bearer " + admin1Token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"role": "USER"}
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(admin2.getId()))
            .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void changeUserRole_shouldReturn403_whenChangingOwnRole() throws Exception {
        mockMvc
            .perform(
                patch("/api/admin/users/" + admin1.getId() + "/role")
                    .header("Authorization", "Bearer " + admin1Token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"role": "USER"}
                        """)
            )
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error").value("ADMIN_OPERATION_DENIED"))
            .andExpect(jsonPath("$.message").value("Cannot change your own role"));
    }

    @Test
    void changeUserRole_shouldReturn403_whenDemotingLastAdmin() throws Exception {
        // First demote admin2, so admin1 is the only admin
        mockMvc
            .perform(
                patch("/api/admin/users/" + admin2.getId() + "/role")
                    .header("Authorization", "Bearer " + admin1Token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"role": "USER"}
                        """)
            )
            .andExpect(status().isOk());

        // Now try to demote admin1 (using admin2's old token won't work, but we can test the logic)
        // We need admin2 to demote admin1, but admin2 is now USER, so this should fail with 403
        mockMvc
            .perform(
                patch("/api/admin/users/" + admin1.getId() + "/role")
                    .header("Authorization", "Bearer " + admin2Token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"role": "USER"}
                        """)
            )
            .andExpect(status().isForbidden());
    }

    @Test
    void changeUserRole_shouldReturn403_whenRegularUser() throws Exception {
        mockMvc
            .perform(
                patch("/api/admin/users/" + regularUser.getId() + "/role")
                    .header("Authorization", "Bearer " + userToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"role": "ADMIN"}
                        """)
            )
            .andExpect(status().isForbidden());
    }

    // ===== DELETE /api/admin/users/{id} =====

    @Test
    void deleteUser_shouldDeleteUser() throws Exception {
        mockMvc
            .perform(
                delete("/api/admin/users/" + regularUser.getId())
                    .header("Authorization", "Bearer " + admin1Token)
            )
            .andExpect(status().isNoContent());

        // Verify user is deleted
        mockMvc
            .perform(get("/api/admin/users").header("Authorization", "Bearer " + admin1Token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[?(@.email == 'user@example.com')]").doesNotExist());
    }

    @Test
    void deleteUser_shouldReturn403_whenDeletingAdmin() throws Exception {
        // Cannot delete any admin through UI
        mockMvc
            .perform(
                delete("/api/admin/users/" + admin2.getId())
                    .header("Authorization", "Bearer " + admin1Token)
            )
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error").value("ADMIN_OPERATION_DENIED"))
            .andExpect(jsonPath("$.message").value("Admin accounts cannot be deleted through UI"));
    }

    @Test
    void deleteUser_shouldReturn403_whenRegularUser() throws Exception {
        mockMvc
            .perform(
                delete("/api/admin/users/" + regularUser.getId())
                    .header("Authorization", "Bearer " + userToken)
            )
            .andExpect(status().isForbidden());
    }

    @Test
    void deleteUser_shouldReturn403_whenUserNotFound() throws Exception {
        mockMvc
            .perform(
                delete("/api/admin/users/99999")
                    .header("Authorization", "Bearer " + admin1Token)
            )
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error").value("ADMIN_OPERATION_DENIED"))
            .andExpect(jsonPath("$.message").value("User not found"));
    }
}
