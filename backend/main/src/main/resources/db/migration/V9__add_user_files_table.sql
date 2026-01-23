-- V9__add_user_files_table.sql
-- User files storage table

CREATE TABLE user_files (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    filename VARCHAR(255) NOT NULL,
    s3_key VARCHAR(500) NOT NULL,
    size_bytes BIGINT NOT NULL,
    content_type VARCHAR(100),
    thumbnail_s3_key VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- One file per name per user (user can't have two files with same name)
    UNIQUE(user_id, filename)
);

-- Indexes for performance
CREATE INDEX idx_user_files_user_id ON user_files(user_id);
CREATE INDEX idx_user_files_created_at ON user_files(created_at DESC);

-- Critical: Index for SUM queries (used_bytes queries)
CREATE INDEX idx_user_files_user_id_size ON user_files(user_id, size_bytes);

-- Index for content type filtering
CREATE INDEX idx_user_files_content_type ON user_files(content_type) WHERE content_type IS NOT NULL;

COMMENT ON TABLE user_files IS 'User uploaded files metadata. Files are stored in S3, metadata in PostgreSQL.';
COMMENT ON COLUMN user_files.s3_key IS 'Full S3 key: users/{userId}/files/{uuid}-{sanitizedFilename}';
COMMENT ON COLUMN user_files.size_bytes IS 'File size in bytes';
COMMENT ON COLUMN user_files.content_type IS 'MIME type (e.g., image/jpeg, application/pdf)';
