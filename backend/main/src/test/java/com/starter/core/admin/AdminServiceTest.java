package com.starter.core.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.starter.core.admin.dto.AdminUserDto;
import com.starter.core.exception.AdminOperationException;
import com.starter.core.user.User;
import com.starter.core.user.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Unit tests for AdminService. */
@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    private AdminService adminService;

    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        adminService = new AdminService(userRepository);

        testUser = User.builder()
            .id(1L)
            .email("user@example.com")
            .role(User.Role.USER)
            .emailVerified(true)
            .createdAt(Instant.now())
            .build();

        adminUser = User.builder()
            .id(2L)
            .email("admin@example.com")
            .role(User.Role.ADMIN)
            .emailVerified(true)
            .createdAt(Instant.now())
            .build();
    }

    @Test
    void getAllUsers_shouldReturnUserDtos() {
        when(userRepository.findAll()).thenReturn(List.of(testUser, adminUser));

        List<AdminUserDto> result = adminService.getAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getEmail()).isEqualTo("user@example.com");
        assertThat(result.get(1).getEmail()).isEqualTo("admin@example.com");
    }

    @Test
    void changeUserRole_shouldUpdateRole() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User updatedUser = User.builder()
            .id(1L)
            .email("user@example.com")
            .role(User.Role.ADMIN)
            .emailVerified(true)
            .createdAt(testUser.getCreatedAt())
            .build();
        when(userRepository.findById(1L))
            .thenReturn(Optional.of(testUser))
            .thenReturn(Optional.of(updatedUser));

        AdminUserDto result = adminService.changeUserRole(1L, User.Role.ADMIN, 2L);

        verify(userRepository).updateRole(1L, User.Role.ADMIN);
        assertThat(result.getRole()).isEqualTo("ADMIN");
    }

    @Test
    void changeUserRole_shouldThrow_whenChangingOwnRole() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));

        assertThatThrownBy(() -> adminService.changeUserRole(2L, User.Role.USER, 2L))
            .isInstanceOf(AdminOperationException.class)
            .hasMessage("Cannot change your own role");

        verify(userRepository, never()).updateRole(any(), any());
    }

    @Test
    void changeUserRole_shouldThrow_whenDemotingLastAdmin() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));
        when(userRepository.countByRole(User.Role.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> adminService.changeUserRole(2L, User.Role.USER, 1L))
            .isInstanceOf(AdminOperationException.class)
            .hasMessage("Cannot demote the last admin");

        verify(userRepository, never()).updateRole(any(), any());
    }

    @Test
    void changeUserRole_shouldThrow_whenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.changeUserRole(999L, User.Role.ADMIN, 2L))
            .isInstanceOf(AdminOperationException.class)
            .hasMessage("User not found");
    }

    @Test
    void deleteUser_shouldDeleteRegularUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        adminService.deleteUser(1L, 2L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_shouldThrow_whenDeletingAdmin() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));

        assertThatThrownBy(() -> adminService.deleteUser(2L, 1L))
            .isInstanceOf(AdminOperationException.class)
            .hasMessage("Admin accounts cannot be deleted through UI");

        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void deleteUser_shouldThrow_whenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.deleteUser(999L, 2L))
            .isInstanceOf(AdminOperationException.class)
            .hasMessage("User not found");
    }
}
