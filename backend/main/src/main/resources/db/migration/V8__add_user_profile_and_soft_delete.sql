-- V8__add_user_profile_and_soft_delete.sql
-- Add user profile fields, avatar, soft delete, and email change functionality

-- Profile fields
ALTER TABLE users ADD COLUMN display_name VARCHAR(100);
ALTER TABLE users ADD COLUMN bio VARCHAR(500);
ALTER TABLE users ADD COLUMN website VARCHAR(255);
ALTER TABLE users ADD COLUMN company VARCHAR(100);
ALTER TABLE users ADD COLUMN location VARCHAR(100);
ALTER TABLE users ADD COLUMN country VARCHAR(2);  -- ISO 3166-1 alpha-2

-- Avatar
ALTER TABLE users ADD COLUMN avatar BYTEA;
ALTER TABLE users ADD COLUMN avatar_content_type VARCHAR(50);

-- Soft delete
ALTER TABLE users ADD COLUMN archived_at TIMESTAMP;

-- Email change (pending verification)
ALTER TABLE users ADD COLUMN pending_email VARCHAR(255);
ALTER TABLE users ADD COLUMN email_change_token VARCHAR(64);
ALTER TABLE users ADD COLUMN email_change_token_expires_at TIMESTAMP;

-- Drop old unique constraint on email
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_email_key;

-- Create partial unique index - email unique only for active (non-archived) users
CREATE UNIQUE INDEX idx_users_email_active ON users(LOWER(email)) WHERE archived_at IS NULL;

-- Index for email change token lookups
CREATE INDEX idx_users_email_change_token ON users(email_change_token) WHERE email_change_token IS NOT NULL;

COMMENT ON COLUMN users.archived_at IS 'Timestamp when user account was soft-deleted. NULL for active accounts.';
COMMENT ON COLUMN users.pending_email IS 'New email address pending verification.';
COMMENT ON COLUMN users.email_change_token IS 'Token for email change verification.';
COMMENT ON COLUMN users.avatar_content_type IS 'MIME type of avatar image (e.g., image/jpeg, image/png).';
