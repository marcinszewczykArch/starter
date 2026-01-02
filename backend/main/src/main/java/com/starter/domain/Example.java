package com.starter.domain;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain entity representing an example record. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Example {

    private Long id;
    private String name;
    private String description;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
