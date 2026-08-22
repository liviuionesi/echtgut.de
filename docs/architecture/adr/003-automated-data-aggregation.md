# ADR-003: Automated Data Aggregation via External APIs

**Status:** Accepted
**Date:** 2026-08-22
**Deciders:** Project owner

## Context

The initial architecture of `echtgut.de` relied on a manual curation model, where users or curators would submit local businesses and places, which would then be stored in our Postgres database as `CuratedExperience` entities. 

Given the goal to build a compelling showcase prototype rapidly (e.g., for interviews) with zero initial community to bootstrap submissions, a manual curation model resulted in an empty, non-functional platform. To solve this cold-start problem and demonstrate immediate value, the application needs to automatically source high-quality, real-world data (places, ratings, images, and live status) for the Stuttgart region.

## Decision

1. **Pivot to Aggregation:** We deprecate the manual curation/submission workflows (`PublicSubmissionController`, `ExperienceEntity`, etc.). We pivot the platform to an automated, real-time discovery engine (Aggregator).
2. **Primary Data Source - Google Places API (New):** We adopt the Google Places API (`https://places.googleapis.com/v1/places:searchNearby` and related endpoints) as our primary data source. This provides authentic user ratings, review counts, live `openNow` status, and high-quality photo references, which are critical for the UI's "wow factor."
3. **Fallback/Alternative - OpenStreetMap (Overpass API):** In environments where a Google API key is missing, or to circumvent API billing limits during initial development, we utilize the free OpenStreetMap Overpass API as a fallback. 
4. **Stateless Aggregator:** The backend `PlaceAggregatorService` will fetch, normalize (into `PlaceDto`), and serve these places dynamically. We avoid caching the entire dataset permanently in our relational database to respect API terms of service and ensure data (like `openNow` status and ratings) remains fresh, relying instead on short-lived application-level caching (Spring `@Cacheable`).

## Options Considered

**Manual Curation (Original Architecture)** — Rejected. Requires significant community effort and time to populate the database, failing the immediate showcase requirement.

**Web Crawling** — Rejected. Building a custom scraper for local listings is fragile, legally ambiguous, and computationally expensive for an MVP.

**Pure OpenStreetMap (Overpass API) Only** — Rejected as the primary source because it lacks qualitative data (authentic review scores and rich user photos), which are essential for a premium user experience.

## Consequences

- **Easier:** The application instantly populates with hundreds of real, high-quality locations without any manual data entry.
- **Harder:** We introduce external API dependencies. The backend must handle API rate limits, timeouts, and fallback logic gracefully. We also introduce a requirement for a Google Cloud API key (with billing enabled) to unlock the full potential of the platform.
- **Data Model:** We transition from relational database entities (`ExperienceEntity`) to transient Data Transfer Objects (`PlaceDto`). The Postgres database will be preserved for taxonomy tags, affiliate links, and future features (like user bookmarks), but the core catalog is now ephemeral.
