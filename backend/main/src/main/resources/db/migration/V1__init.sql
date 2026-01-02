-- V1__init.sql
-- Initial database schema

CREATE TABLE IF NOT EXISTS examples (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index on active column for filtering
CREATE INDEX idx_examples_active ON examples(active);

-- Insert sample data
INSERT INTO examples (name, description, active) VALUES
    ('Sample Example 1', 'This is the first sample example', true),
    ('Sample Example 2', 'This is the second sample example', true),
    ('Inactive Example', 'This example is inactive', false);

