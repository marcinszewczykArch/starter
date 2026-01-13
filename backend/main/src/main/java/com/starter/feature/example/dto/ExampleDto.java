package com.starter.feature.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/** Data Transfer Object for Example entity. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExampleDto {

    private Long id;
    private Long userId;
    private String name;
    private String description;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
