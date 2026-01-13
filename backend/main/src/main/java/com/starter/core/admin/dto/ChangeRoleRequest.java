package com.starter.core.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.starter.core.user.User;

import jakarta.validation.constraints.NotNull;

/** Request DTO for changing user role. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeRoleRequest {

    @NotNull(message = "Role cannot be null") private User.Role role;
}
