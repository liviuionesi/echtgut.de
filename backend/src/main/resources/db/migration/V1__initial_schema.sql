-- V1__initial_schema.sql
-- Placeholder initial schema migration for echtgut.de staging database.

CREATE TABLE IF NOT EXISTS schema_version_check (
    id SERIAL PRIMARY KEY,
    installed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'INITIALIZED'
);

INSERT INTO schema_version_check (status) VALUES ('INITIALIZED');
