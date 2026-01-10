-- Truncate all application tables (preserves flyway schema history)
-- CASCADE handles foreign key constraints automatically
TRUNCATE TABLE users CASCADE;
TRUNCATE TABLE examples CASCADE;

