-- V4__create_taxonomy_tags.sql
-- Flyway migration creating tags and experience_tags join table.

CREATE TABLE IF NOT EXISTS tags (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    slug VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL DEFAULT 'MOOD',
    is_retired BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS experience_tags (
    experience_id UUID NOT NULL REFERENCES curated_experiences(id) ON DELETE CASCADE,
    tag_id UUID NOT NULL REFERENCES tags(id) ON DELETE RESTRICT,
    PRIMARY KEY (experience_id, tag_id)
);

CREATE INDEX IF NOT EXISTS idx_tags_slug ON tags(slug);
CREATE INDEX IF NOT EXISTS idx_tags_is_retired ON tags(is_retired);
CREATE INDEX IF NOT EXISTS idx_experience_tags_tag_id ON experience_tags(tag_id);

-- Seed FR-4.1 initial mood taxonomy tags
INSERT INTO tags (slug, name, category) VALUES
    ('auszeit', 'Auszeit', 'MOOD'),
    ('romantik', 'Romantik', 'MOOD'),
    ('feinschmecker', 'Feinschmecker', 'MOOD'),
    ('natur', 'Natur & Abenteuer', 'MOOD'),
    ('kultur', 'Kunst & Kultur', 'MOOD'),
    ('wellness', 'Wellness & Spa', 'MOOD');
