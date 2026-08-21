-- V3__create_curated_experiences.sql
-- Flyway migration creating public pristine curated_experiences table.

CREATE TABLE IF NOT EXISTS curated_experiences (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    raw_deal_id UUID REFERENCES raw_deals(id) ON DELETE SET NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    editorial_title VARCHAR(500) NOT NULL,
    editorial_description TEXT NOT NULL,
    hero_image_url TEXT NOT NULL,
    address VARCHAR(500) NOT NULL,
    lat NUMERIC(9, 6) NOT NULL,
    lng NUMERIC(9, 6) NOT NULL,
    affiliate_url TEXT,
    booking_contact VARCHAR(255),
    curator_notes TEXT,
    is_published BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_curated_experiences_slug ON curated_experiences(slug);
CREATE INDEX IF NOT EXISTS idx_curated_experiences_published ON curated_experiences(is_published);
CREATE INDEX IF NOT EXISTS idx_curated_experiences_raw_deal_id ON curated_experiences(raw_deal_id);
