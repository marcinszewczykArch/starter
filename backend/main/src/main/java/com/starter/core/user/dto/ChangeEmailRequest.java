package com.starter.core.user.dto;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Request DTO for changing user email. */
@Data
public class ChangeEmailRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String newEmail;

    @NotBlank(message = "Password is required")
    private String password;
}
