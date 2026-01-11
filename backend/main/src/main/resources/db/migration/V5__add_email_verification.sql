-- V5__add_email_verification.sql
-- Add email verification support to users table

ALTER TABLE users ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN verification_token VARCHAR(255);
ALTER TABLE users ADD COLUMN verification_token_expires_at TIMESTAMP;

-- Index for token lookup
CREATE INDEX idx_users_verification_token ON users(verification_token) WHERE verification_token IS NOT NULL;

-- Update existing users to be verified (they were created before verification was required)
UPDATE users SET email_verified = TRUE WHERE email_verified = FALSE;

