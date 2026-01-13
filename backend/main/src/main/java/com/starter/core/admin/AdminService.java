package com.starter.core.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.starter.core.admin.dto.AdminUserDto;
import com.starter.core.exception.AdminOperationException;
import com.starter.core.user.User;
import com.starter.core.user.UserRepository;

import java.util.List;

/** Service for admin operations on users. */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;

    /** Get all users as DTOs. */
    public List<AdminUserDto> getAllUsers() {
        log.debug("Fetching all users for admin panel");
        return userRepository.findAll().stream()
            .map(AdminUserDto::fromUser)
            .toList();
    }

    /**
     * Change user role.
     *
     * @param userId         ID of user to change
     * @param newRole        new role to assign
     * @param currentAdminId ID of admin performing the action
     * @return updated user DTO
     * @throws AdminOperationException if operation is not allowed
     */
    @Transactional
    public AdminUserDto changeUserRole(Long userId, User.Role newRole, Long currentAdminId) {
        log.info("Admin {} changing role of user {} to {}", currentAdminId, userId, newRole);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new AdminOperationException("User not found"));

        // Cannot change own role
        if (userId.equals(currentAdminId)) {
            throw new AdminOperationException("Cannot change your own role");
        }

        // If demoting from ADMIN, check if this is the last admin
        if (user.getRole() == User.Role.ADMIN && newRole == User.Role.USER) {
            long adminCount = userRepository.countByRole(User.Role.ADMIN);
            if (adminCount <= 1) {
                throw new AdminOperationException("Cannot demote the last admin");
            }
        }

        userRepository.updateRole(userId, newRole);
        log.info("Successfully changed role of user {} to {}", userId, newRole);

        // Return updated user
        return userRepository.findById(userId)
            .map(AdminUserDto::fromUser)
            .orElseThrow(() -> new AdminOperationException("User not found after update"));
    }

    /**
     * Delete a user. Admin accounts cannot be deleted through UI.
     *
     * @param userId         ID of user to delete
     * @param currentAdminId ID of admin performing the action
     * @throws AdminOperationException if operation is not allowed
     */
    @Transactional
    public void deleteUser(Long userId, Long currentAdminId) {
        log.info("Admin {} attempting to delete user {}", currentAdminId, userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new AdminOperationException("User not found"));

        // Cannot delete admin accounts through UI - must be done directly in database
        if (user.getRole() == User.Role.ADMIN) {
            throw new AdminOperationException("Admin accounts cannot be deleted through UI");
        }

        userRepository.deleteById(userId);
        log.info("Successfully deleted user {}", userId);
    }
}
