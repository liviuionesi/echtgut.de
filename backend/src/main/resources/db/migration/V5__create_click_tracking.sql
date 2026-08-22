-- V5__create_click_tracking.sql
-- Flyway migration creating click_events table for affiliate tracking.

CREATE TABLE IF NOT EXISTS click_events (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    experience_id UUID NOT NULL REFERENCES curated_experiences(id) ON DELETE CASCADE,
    clicked_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    referrer_url TEXT,
    user_agent TEXT
);

CREATE INDEX IF NOT EXISTS idx_click_events_experience_id ON click_events(experience_id);
CREATE INDEX IF NOT EXISTS idx_click_events_clicked_at ON click_events(clicked_at);
