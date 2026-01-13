package com.starter.core.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.starter.core.admin.dto.AdminUserDto;
import com.starter.core.admin.dto.ChangeRoleRequest;
import com.starter.core.security.UserPrincipal;

import jakarta.validation.Valid;

import java.util.List;

/** REST controller for admin operations. */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin", description = "Admin operations for user management")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    @Operation(summary = "Get all users")
    public List<AdminUserDto> getAllUsers() {
        return adminService.getAllUsers();
    }

    @PatchMapping("/users/{id}/role")
    @Operation(summary = "Change user role")
    public AdminUserDto changeUserRole(
        @PathVariable Long id,
        @Valid @RequestBody ChangeRoleRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return adminService.changeUserRole(id, request.getRole(), principal.getId());
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a user")
    public void deleteUser(
        @PathVariable Long id,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        adminService.deleteUser(id, principal.getId());
    }
}
