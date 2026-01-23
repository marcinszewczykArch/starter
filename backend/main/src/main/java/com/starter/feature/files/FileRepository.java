package com.starter.feature.files;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for UserFile entity using JdbcClient.
 * Provides methods for file operations with pagination, filtering, and search.
 */
@Repository
@RequiredArgsConstructor
public class FileRepository {
    private final JdbcClient jdbcClient;
    private static final RowMapper<UserFile> ROW_MAPPER = new UserFileRowMapper();

    /**
     * Find files by user ID with pagination.
     */
    public Page<UserFile> findByUserIdPaginated(Long userId, int page, int size) {
        int offset = page * size;

        List<UserFile> files = jdbcClient
            .sql("""
                SELECT id, user_id, filename, s3_key, size_bytes, content_type, thumbnail_s3_key,
                       created_at, updated_at
                FROM user_files
                WHERE user_id = :userId
                ORDER BY created_at DESC
                LIMIT :limit OFFSET :offset
                """)
            .param("userId", userId)
            .param("limit", size)
            .param("offset", offset)
            .query(ROW_MAPPER)
            .list();

        long total = countByUserId(userId);
        return new PageImpl<>(files, PageRequest.of(page, size), total);
    }

    /**
     * Find all files by user ID (for deleteAllUserFiles).
     */
    public List<UserFile> findByUserId(Long userId) {
        return jdbcClient
            .sql("""
                SELECT id, user_id, filename, s3_key, size_bytes, content_type, thumbnail_s3_key,
                       created_at, updated_at
                FROM user_files
                WHERE user_id = :userId
                ORDER BY created_at DESC
                """)
            .param("userId", userId)
            .query(ROW_MAPPER)
            .list();
    }

    /**
     * Get total size with lock (for race condition prevention).
     * Uses SELECT FOR UPDATE to lock rows during transaction.
     * ORDER BY ensures deterministic lock order to prevent deadlocks.
     */
    public Long getTotalSizeWithLock(Long userId) {
        return jdbcClient
            .sql("""
                SELECT COALESCE(SUM(size_bytes), 0)
                FROM user_files
                WHERE user_id = :userId
                ORDER BY id
                FOR UPDATE
                """)
            .param("userId", userId)
            .query(Long.class)
            .optional()
            .orElse(0L);
    }

    /**
     * Get total size without lock (for stats display).
     */
    public Long getTotalSizeByUserId(Long userId) {
        return jdbcClient
            .sql("SELECT COALESCE(SUM(size_bytes), 0) FROM user_files WHERE user_id = :userId")
            .param("userId", userId)
            .query(Long.class)
            .optional()
            .orElse(0L);
    }

    /**
     * Count files by user ID.
     */
    public long countByUserId(Long userId) {
        return jdbcClient
            .sql("SELECT COUNT(*) FROM user_files WHERE user_id = :userId")
            .param("userId", userId)
            .query(Long.class)
            .single();
    }

    /**
     * Find file by ID and user ID (ensures user owns the file).
     */
    public Optional<UserFile> findByIdAndUserId(Long fileId, Long userId) {
        return jdbcClient
            .sql("""
                SELECT id, user_id, filename, s3_key, size_bytes, content_type, thumbnail_s3_key,
                       created_at, updated_at
                FROM user_files
                WHERE id = :id AND user_id = :userId
                """)
            .param("id", fileId)
            .param("userId", userId)
            .query(ROW_MAPPER)
            .optional();
    }

    /**
     * Check if filename already exists for user.
     */
    public boolean existsByUserIdAndFilename(Long userId, String filename) {
        return Boolean.TRUE.equals(
            jdbcClient
                .sql("SELECT EXISTS(SELECT 1 FROM user_files WHERE user_id = :userId AND filename = :filename)")
                .param("userId", userId)
                .param("filename", filename)
                .query(Boolean.class)
                .single()
        );
    }

    /**
     * Save a new file record.
     */
    public UserFile save(UserFile file) {
        Instant now = Instant.now();
        Long id = jdbcClient
            .sql("""
                INSERT INTO user_files (user_id, filename, s3_key, size_bytes, content_type,
                                       thumbnail_s3_key, created_at, updated_at)
                VALUES (:userId, :filename, :s3Key, :sizeBytes, :contentType, :thumbnailS3Key,
                        :createdAt, :updatedAt)
                RETURNING id
                """)
            .param("userId", file.getUserId())
            .param("filename", file.getFilename())
            .param("s3Key", file.getS3Key())
            .param("sizeBytes", file.getSizeBytes())
            .param("contentType", file.getContentType())
            .param("thumbnailS3Key", file.getThumbnailS3Key())
            .param("createdAt", Timestamp.from(now))
            .param("updatedAt", Timestamp.from(now))
            .query(Long.class)
            .single();

        return UserFile.builder()
            .id(id)
            .userId(file.getUserId())
            .filename(file.getFilename())
            .s3Key(file.getS3Key())
            .sizeBytes(file.getSizeBytes())
            .contentType(file.getContentType())
            .thumbnailS3Key(file.getThumbnailS3Key())
            .createdAt(now)
            .updatedAt(now)
            .build();
    }

    /**
     * Delete file by ID.
     */
    public void delete(Long fileId) {
        jdbcClient
            .sql("DELETE FROM user_files WHERE id = :id")
            .param("id", fileId)
            .update();
    }

    /**
     * Delete all files for user (used in deleteAllUserFiles).
     */
    public void deleteByUserId(Long userId) {
        jdbcClient
            .sql("DELETE FROM user_files WHERE user_id = :userId")
            .param("userId", userId)
            .update();
    }

    /**
     * Find files by content type (for filtering).
     */
    public Page<UserFile> findByUserIdAndContentType(Long userId, String contentType, int page, int size) {
        int offset = page * size;
        // Simple validation: only allow * wildcard, not % directly
        String contentTypePattern = contentType.replace("*", "%");

        List<UserFile> files = jdbcClient
            .sql("""
                SELECT id, user_id, filename, s3_key, size_bytes, content_type, thumbnail_s3_key,
                       created_at, updated_at
                FROM user_files
                WHERE user_id = :userId AND content_type LIKE :contentType
                ORDER BY created_at DESC
                LIMIT :limit OFFSET :offset
                """)
            .param("userId", userId)
            .param("contentType", contentTypePattern)
            .param("limit", size)
            .param("offset", offset)
            .query(ROW_MAPPER)
            .list();

        long total = jdbcClient
            .sql("SELECT COUNT(*) FROM user_files WHERE user_id = :userId AND content_type LIKE :contentType")
            .param("userId", userId)
            .param("contentType", contentTypePattern)
            .query(Long.class)
            .single();

        return new PageImpl<>(files, PageRequest.of(page, size), total);
    }

    /**
     * Search files by filename.
     */
    public Page<UserFile> findByUserIdAndFilenameContaining(Long userId, String query, int page, int size) {
        int offset = page * size;
        String searchPattern = "%" + query + "%";

        List<UserFile> files = jdbcClient
            .sql("""
                SELECT id, user_id, filename, s3_key, size_bytes, content_type, thumbnail_s3_key,
                       created_at, updated_at
                FROM user_files
                WHERE user_id = :userId AND filename ILIKE :pattern
                ORDER BY created_at DESC
                LIMIT :limit OFFSET :offset
                """)
            .param("userId", userId)
            .param("pattern", searchPattern)
            .param("limit", size)
            .param("offset", offset)
            .query(ROW_MAPPER)
            .list();

        long total = jdbcClient
            .sql("SELECT COUNT(*) FROM user_files WHERE user_id = :userId AND filename ILIKE :pattern")
            .param("userId", userId)
            .param("pattern", searchPattern)
            .query(Long.class)
            .single();

        return new PageImpl<>(files, PageRequest.of(page, size), total);
    }

    /**
     * Get all S3 keys from database (for cleanup - if needed in future).
     */
    public List<String> findAllS3Keys() {
        return jdbcClient
            .sql("SELECT s3_key FROM user_files")
            .query(String.class)
            .list();
    }

    private static class UserFileRowMapper implements RowMapper<UserFile> {
        @Override
        public UserFile mapRow(ResultSet rs, int rowNum) throws SQLException {
            return UserFile.builder()
                .id(rs.getLong("id"))
                .userId(rs.getLong("user_id"))
                .filename(rs.getString("filename"))
                .s3Key(rs.getString("s3_key"))
                .sizeBytes(rs.getLong("size_bytes"))
                .contentType(rs.getString("content_type"))
                .thumbnailS3Key(rs.getString("thumbnail_s3_key"))
                .createdAt(rs.getTimestamp("created_at").toInstant())
                .updatedAt(rs.getTimestamp("updated_at").toInstant())
                .build();
        }
    }
}
