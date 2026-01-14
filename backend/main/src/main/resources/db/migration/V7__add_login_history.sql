-- V7__add_login_history.sql
-- Track all login attempts (successful and failed) with location data

CREATE TABLE login_history (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT REFERENCES users(id) ON DELETE CASCADE,
    
    -- Timestamp
    logged_in_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Success/Failure
    success         BOOLEAN NOT NULL DEFAULT TRUE,
    failure_reason  VARCHAR(50),         -- 'INVALID_PASSWORD', 'USER_NOT_FOUND', etc.
    attempted_email VARCHAR(255),        -- For failed attempts where user doesn't exist
    
    -- Location (GPS or IP-based)
    latitude        DECIMAL(10, 7),
    longitude       DECIMAL(10, 7),
    location_source VARCHAR(10),         -- 'GPS', 'IP', or NULL if unknown
    country         VARCHAR(100),
    city            VARCHAR(100),
    
    -- Device info
    ip_address      VARCHAR(45),         -- Supports IPv6
    user_agent      VARCHAR(500),
    device_type     VARCHAR(20),         -- 'desktop', 'mobile', 'tablet'
    browser         VARCHAR(100),
    os              VARCHAR(100)
);

-- Indexes for common queries
CREATE INDEX idx_login_history_user_id ON login_history(user_id);
CREATE INDEX idx_login_history_logged_in_at ON login_history(logged_in_at DESC);
CREATE INDEX idx_login_history_success ON login_history(success);

-- Add last_login_at to users table for quick access
ALTER TABLE users ADD COLUMN last_login_at TIMESTAMP;

COMMENT ON TABLE login_history IS 'Tracks all login attempts with location and device information';
COMMENT ON COLUMN login_history.location_source IS 'GPS = browser geolocation, IP = ip-api.com lookup';
COMMENT ON COLUMN login_history.failure_reason IS 'Reason for failed login: INVALID_PASSWORD, USER_NOT_FOUND';

