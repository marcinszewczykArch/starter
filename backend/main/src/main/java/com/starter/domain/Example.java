package com.starter.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

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
    private Instant createdAt;
    private Instant updatedAt;
}
