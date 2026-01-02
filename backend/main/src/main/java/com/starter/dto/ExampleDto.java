package com.starter.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Data Transfer Object for Example entity. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExampleDto {

    private Long id;
    private String name;
    private String description;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
