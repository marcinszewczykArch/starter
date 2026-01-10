-- V3__add_user_id_to_examples.sql
-- Add user_id column to examples table for row-level security

-- Add user_id column (nullable initially for existing data)
ALTER TABLE examples ADD COLUMN user_id BIGINT;

-- Add foreign key constraint
ALTER TABLE examples 
    ADD CONSTRAINT fk_examples_user 
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Create index for efficient filtering by user_id
CREATE INDEX idx_examples_user_id ON examples(user_id);

-- Note: Existing sample data will have NULL user_id
-- In production, you might want to assign them to a default admin user

