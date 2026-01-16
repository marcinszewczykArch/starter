package com.starter.core.user.dto;

import lombok.Data;

import jakarta.validation.constraints.Size;

/** Request DTO for updating user profile. */
@Data
public class UpdateProfileRequest {

    @Size(max = 100, message = "Display name must be at most 100 characters")
    private String displayName;

    @Size(max = 500, message = "Bio must be at most 500 characters")
    private String bio;

    @Size(max = 255, message = "Website must be at most 255 characters")
    private String website;

    @Size(max = 100, message = "Company must be at most 100 characters")
    private String company;

    @Size(max = 100, message = "Location must be at most 100 characters")
    private String location;

    @Size(max = 2, message = "Country code must be 2 characters")
    private String country;
}
