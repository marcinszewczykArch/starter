package com.starter.core.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Paginated response for login history. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginHistoryPageDto {

    private List<LoginHistoryDto> content;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;
}
