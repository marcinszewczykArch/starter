package com.starter.feature.files.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Data Transfer Object for file statistics. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileStatsDto {
    private int fileCount;
    private long totalSizeBytes;
}
