package com.starter.feature.files.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for storage usage information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageUsageResponse {
    private long usedBytes;
    private long maxBytes;
    private double percentage;
}
