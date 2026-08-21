-- V2__create_raw_deals.sql
-- Flyway migration creating staging table for candidate raw deals.

CREATE TABLE IF NOT EXISTS raw_deals (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    source VARCHAR(100) NOT NULL,
    source_ref VARCHAR(255) NOT NULL UNIQUE,
    raw_title VARCHAR(500) NOT NULL,
    raw_description TEXT,
    raw_image_url TEXT,
    location_text VARCHAR(500),
    lat NUMERIC(9, 6),
    lng NUMERIC(9, 6),
    price_hint VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    rejection_reason TEXT,
    submitted_by VARCHAR(255),
    promoted_experience_id UUID,
    ingested_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_raw_deals_status ON raw_deals(status);
CREATE INDEX IF NOT EXISTS idx_raw_deals_source_ref ON raw_deals(source_ref);
