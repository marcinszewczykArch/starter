package com.starter.feature.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/** Request DTO for creating a new Example. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateExampleRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;
}
