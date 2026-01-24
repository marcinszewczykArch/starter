package com.starter.feature.files;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.starter.core.security.UserPrincipal;
import com.starter.feature.files.dto.*;

import java.io.IOException;

/**
 * REST controller for file operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "Files", description = "File storage and management API")
@SecurityRequirement(name = "bearerAuth")
public class FileController {
    private final FileService fileService;

    @PostMapping
    @Operation(
        summary = "Upload file",
        description = "Upload a new file. Max 100MB per file, 1GB total per user. " +
            "Allowed types: images, PDF, ZIP, text files."
    )
    public ResponseEntity<FileDto> uploadFile(
        @AuthenticationPrincipal UserPrincipal principal,
        @Parameter(description = "File to upload", required = true)
        @RequestParam("file") MultipartFile file
    ) throws IOException {
        FileDto uploaded = fileService.uploadFile(principal.getId(), file);
        return ResponseEntity.status(HttpStatus.CREATED).body(uploaded);
    }

    @GetMapping
    @Operation(
        summary = "List files",
        description = "Get paginated list of files for current user"
    )
    public ResponseEntity<Page<FileDto>> getFiles(
        @AuthenticationPrincipal UserPrincipal principal,
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size")
        @RequestParam(defaultValue = "20") int size,
        @Parameter(description = "Filter by content type (e.g., 'image/*')")
        @RequestParam(required = false) String contentType,
        @Parameter(description = "Search by filename")
        @RequestParam(required = false) String search
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<FileDto> files;

        if (search != null && !search.isBlank()) {
            files = fileService.searchFiles(principal.getId(), search, pageable);
        } else if (contentType != null && !contentType.isBlank()) {
            files = fileService.getUserFilesByContentType(principal.getId(), contentType, pageable);
        } else {
            files = fileService.getUserFiles(principal.getId(), pageable);
        }

        return ResponseEntity.ok(files);
    }

    @GetMapping("/stats")
    @Operation(
        summary = "Get file statistics",
        description = "Get file count and total size for current user"
    )
    public ResponseEntity<FileStatsDto> getFileStats(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.debug("GET /api/files/stats called for user: {}", principal.getId());
        try {
            FileStatsDto stats = fileService.getFileStats(principal.getId());
            log.debug("File stats retrieved successfully for user: {}", principal.getId());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error in getFileStats endpoint for user: {}", principal.getId(), e);
            throw e;
        }
    }

    @GetMapping("/storage/usage")
    @Operation(
        summary = "Get storage usage",
        description = "Get storage usage info (used/total/percentage)"
    )
    public ResponseEntity<StorageUsageResponse> getStorageUsage(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        StorageQuotaService.StorageUsageInfo info = fileService.getStorageUsage(principal.getId());
        StorageUsageResponse response = StorageUsageResponse.builder()
            .usedBytes(info.getUsedBytes())
            .maxBytes(info.getMaxBytes())
            .percentage(info.getPercentage())
            .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{fileId}/download")
    @Operation(
        summary = "Get download URL",
        description = "Get presigned URL to download file (valid for 60 minutes)"
    )
    public ResponseEntity<FileDownloadResponse> getDownloadUrl(
        @AuthenticationPrincipal UserPrincipal principal,
        @Parameter(description = "File ID")
        @PathVariable Long fileId
    ) {
        String url = fileService.getDownloadUrl(principal.getId(), fileId);
        return ResponseEntity.ok(FileDownloadResponse.builder().downloadUrl(url).build());
    }

    @DeleteMapping("/{fileId}")
    @Operation(
        summary = "Delete file",
        description = "Delete a file permanently"
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFile(
        @AuthenticationPrincipal UserPrincipal principal,
        @Parameter(description = "File ID")
        @PathVariable Long fileId
    ) {
        fileService.deleteFile(principal.getId(), fileId);
    }
}
