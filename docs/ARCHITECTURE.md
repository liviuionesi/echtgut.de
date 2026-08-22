# Architecture — echtgut.de

Companion to [REQUIREMENTS.md](REQUIREMENTS.md). This is the technical design for the automated data aggregation pipeline (ADR-003).

## 1. System Overview

The system aggregates data from external sources and serves it to the frontend via a unified DTO layer. Manual curation has been deprecated.

```
 External APIs                  Backend                  Frontend
 (Google Places API,          (PlaceAggregatorService)   (Next.js App)
  Overpass API)                      │                       │
        │                            ▼                       ▼
        │                    ┌───────────────┐       ┌──────────────┐
        └───────────────────▶│ Spring Boot   │──────▶│ Public Site  │
             JSON payload    │ API Layer     │ DTOs  │ (Map/Grid)   │
                             │ (Normalized)  │       │ echtgut.de   │
                             └───────────────┘       └──────────────┘
```

Both the public catalog and map view are Next.js route groups talking to the same Spring Boot API — see §4.

## 2. Data Model 

There is no longer a staging (`raw_deals`) or public (`curated_experiences`) persistence layer. Place data is fetched on the fly or cached transiently. 
The core data structure is the `PlaceDto` sent to the frontend:

| Field | Type | Notes |
|---|---|---|
| `id` | string | The Google Place ID or OSM Node ID |
| `name` | string | Name of the place |
| `description` | string | Editorial hook or snippet |
| `heroImageUrl` | string | Fetched via Google Photo reference or fallback |
| `address` | string | Formatted address |
| `lat` / `lng` | number | Coordinates for the Map View |
| `tags` | string[] | Array of tags mapped from API types |
| `source` | string | e.g. "GOOGLE_PLACES", "OSM" |

## 3. Backend (Spring Boot)

- **Versions**: Spring Boot 4.1.1, Java 21 (Temurin), Gradle. Pinned in `backend/build.gradle`.
- **Shape**: a single Spring Boot app for MVP, package-by-feature — `catalog`, `exception`.
- **Aggregation**: The `PlaceAggregatorService` dynamically queries the Google Places API (New) using the `GOOGLE_PLACES_API_KEY` from the environment. If the key is missing or rate limits are exceeded, it seamlessly falls back to the OpenStreetMap (Overpass API).
- **Public API**:
  - `GET /api/places/trending` — Returns a unified list of `PlaceDto` objects based on the configured radius (e.g., Stuttgart 50km).

## 4. Frontend (Next.js)

The frontend is a single Next.js App Router project:

- `app/(public)/…` — React Server Components, Tailwind CSS, tuned for SEO/Core Web Vitals. The data is fetched from the backend API.
- Shares a generated API client/types with the Spring Boot backend.

### 4.1 Design system (FR-2.5)

Two named themes:
**"Paper" (light) is the default** as of [ADR-002](architecture/adr/002-atlas-obscura-inspired-editorial-redesign.md). "Ink" is not removed — it's the opt-in toggle.

| Token | Paper (light, default) | Ink (dark, `[data-theme="dark"]`) |
|---|---|---|
| `bg` / `bg-elevated` | `#F2F4F0` / `#FAFBF9` | `#12201A` / `#182A22` |
| `fg` / `fg-muted` | `#1B2620` / `#5A665F` | `#F3F1EA` / `#A8B2AC` |
| `accent` (brand) | `#2F4B3C` moss green | `#3F6B52` |
| `gold` (marker) | `#B08900` | `#C9A227` |

Implemented as CSS custom properties (`app/globals.css`).
Typography: **Fraunces** (display) paired with **Inter** (body), via `next/font/google`.

#### 4.1.1 Editorial layout patterns (Atlas Obscura-inspired, ADR-002)

- **Photo-led experience card** (`components/catalog/place-card.tsx`) — a 3:2 hero image dominates the card, with a taxonomy badge overlaid. Glassmorphism styling is applied for a premium look.
- **Location map** (`components/catalog/location-map-wrapper.tsx`) — a Leaflet + OpenStreetMap-tiles map that renders the aggregated coordinates from the backend dynamically.

### 4.2 Frontend quality gates (NFR-7)

- **Testing**: Vitest + React Testing Library, 85% coverage.
- **Lighthouse**: `frontend/lighthouserc.json` asserts Performance ≥ 90, Accessibility/Best Practices/SEO ≥ 95 against the public home page.
- **Pre-commit**: Husky runs lint-staged (Prettier + ESLint --fix).

## 5. Hosting (low-budget)

Hybrid, by explicit decision: the frontend stays on a PaaS free tier; the backend reuses a $0-budget deploy mechanism as-is, per [ADR-001](architecture/adr/001-zero-budget-azure-deploy.md).

| Component | Choice | Why |
|---|---|---|
| Next.js | Vercel free tier | matches the pitch |
| Spring Boot API — local dev | Docker Compose | one command |
| Spring Boot API — cloud demo | Terraform → Azure AKS, ephemeral | `terraform apply` → demo → `terraform destroy` |
| CI | GitHub Actions | build + test both apps on every PR |

## 6. Security & Compliance

- External API Keys (`GOOGLE_PLACES_API_KEY`) are managed via `.env` files and never committed.
- Cookie consent + privacy policy must ship before any analytics goes live to real traffic (NFR-4) — this is a legal launch blocker for a `.de` consumer site.

## 7. Deliberate deltas from the lmdb.dev reference architecture

- **One Spring Boot app, not eight microservices** — there's no independent-scaling or independent-deploy need yet.
- **No message broker** — aggregation is synchronous or cached, not an event stream.
- **Observability starts at Actuator health + logs.** Prometheus/Grafana/ELK is a "once there's real traffic" upgrade.
