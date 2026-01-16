package com.starter.core.user.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/** Request DTO for deleting user account. */
@Data
public class DeleteAccountRequest {

    @NotBlank(message = "Password is required")
    private String password;
}
