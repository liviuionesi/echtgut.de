-- V7__drop_manual_curation_tables.sql
-- Drops the manual curation "airlock" pipeline's tables (ADR-003: the public
-- catalog now aggregates live from Google Places/OSM, never from a locally
-- persisted, curator-reviewed table). Dropped in FK-dependency order.
-- `tags` is kept — public tag browsing is still served from it.

DROP TABLE IF EXISTS click_events;
DROP TABLE IF EXISTS experience_tags;
DROP TABLE IF EXISTS curated_experiences;
DROP TABLE IF EXISTS raw_deals;
