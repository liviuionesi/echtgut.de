# Architecture — echtgut.de

Companion to [REQUIREMENTS.md](REQUIREMENTS.md). This is the technical
design for the curation "airlock" pipeline described there.

## 1. System Overview

Three actors, one pipeline — raw data never reaches the public site
without passing through a human curator:

```
 External sources          Curator                    Visitor
 (affiliate feeds,       reviews / edits /          browses / submits
  OSM POIs, scrapes)      tags / promotes              a "gem"
        │                      ▲  │                       │
        ▼                      │  ▼                       ▼
  ┌─────────────┐        ┌───────────────┐         ┌──────────────┐
  │  raw_deals  │──────▶ │ Curator Admin │         │ Public Site  │
  │  (staging)  │ review │ Portal (SPA)  │         │ (SSG / ISR)  │
  │  PENDING /  │        │ admin.echtgut │         │ echtgut.de   │
  │  REJECTED / │        └───────┬───────┘         └──────▲───────┘
  │  PROMOTED   │                │ promote                │ reads
  └──────▲──────┘                ▼                        │
         │            ┌───────────────────┐                │
         └────────────│ curated_experiences│───────────────┘
        community      │  (public, pristine)│
        submission ───▶│  validated at write│
                        └───────────────────┘
```

Both the admin portal and the public site are Next.js route groups
talking to the same Spring Boot API — see §4.

## 2. Data Model (PostgreSQL)

### 2.1 `raw_deals` (staging)

| Column | Type | Notes |
|---|---|---|
| `id` | uuid pk | |
| `source` | text | `OSM` \| `AFFILIATE_<NETWORK>` \| `SCRAPE_<SITE>` \| `COMMUNITY` \| `MANUAL` |
| `source_ref` | text | source-native id/url — used for dedup + re-sync |
| `raw_title` | text | unformatted, as ingested |
| `raw_description` | text | |
| `raw_image_url` | text, nullable | often missing/bad — curator replaces it |
| `location_text` | text, nullable | unverified address/text as ingested |
| `lat` / `lng` | numeric, nullable | unverified |
| `price_hint` | text, nullable | free-form; feeds are inconsistent |
| `status` | enum | `PENDING` \| `REJECTED` \| `PROMOTED` |
| `rejection_reason` | text, nullable | |
| `submitted_by` | text, nullable | community submitter contact, if applicable |
| `promoted_experience_id` | uuid, nullable, fk → `curated_experiences.id` | set on promote; enables upsert-on-reapprove |
| `ingested_at` / `reviewed_at` | timestamptz | |

### 2.2 `curated_experiences` (public, pristine)

| Column | Type | Notes |
|---|---|---|
| `id` | uuid pk | |
| `raw_deal_id` | uuid, fk → `raw_deals.id` | traceability back to source |
| `slug` | text, unique | for SSG routing |
| `editorial_title` | text, not null | |
| `editorial_description` | text, not null | |
| `hero_image_url` | text, not null | validated at promote time (FR-3.5) |
| `address` | text, not null | |
| `lat` / `lng` | numeric, not null | validated |
| `tags` | join table (`experience_tags`) | curator-assigned taxonomy |
| `affiliate_url` | text, nullable | |
| `booking_contact` | text, nullable | fallback when there's no affiliate link |
| `curator_notes` | text, nullable | internal, not rendered publicly |
| `is_published` | boolean | curator can unpublish without deleting |
| `created_at` / `updated_at` | timestamptz | |

### 2.3 State machine

`raw_deals.status`: `PENDING → PROMOTED` or `PENDING → REJECTED`, both
terminal. A rejected item is not silently retried — re-ingestion of the
same `source_ref` updates the same row (not a new one), giving the
curator a fresh look rather than a bypass.

## 3. Backend (Spring Boot)

- **Shape**: a single Spring Boot app for MVP, package-by-feature —
  `ingestion`, `curation`, `catalog` (public reads), `taxonomy`,
  `submission`. *Not* microservices; see §7 for why.
- **Persistence**: Spring Data JPA + Flyway migrations. Every migration
  ships with a repository test (matches
  [`DEFINITION_OF_DONE.md`](docs/process/DEFINITION_OF_DONE.md)'s
  "tests alongside code, not after" bar).
- **Ingestion**: `@Scheduled` jobs per source, behind a common
  `RawDealSource` adapter interface — adding a new feed means
  implementing one interface, not touching the scheduler.
- **Curator API** (JWT-gated, role `CURATOR`/`ADMIN`):
  - `GET /api/admin/pending-deals` — next unreviewed item.
  - `POST /api/admin/deals/{id}/promote` — validated transform + upsert
    into `curated_experiences`.
  - `POST /api/admin/deals/{id}/reject` — status update + reason.
  - `GET /api/admin/tags`, `POST /api/admin/tags` — taxonomy management.
- **Public API**:
  - `GET /api/experiences` — filterable by tag/city, paginated; backs
    the Next.js SSG/ISR build.
  - `GET /api/experiences/{slug}` — single listing.
  - `POST /api/submissions` — community "Local Gem" form → `raw_deals`
    (rate-limited, captcha-verified).
  - `POST /api/track/click/{experienceId}` — affiliate click tracking,
    then redirect.
- **Auth**: Spring Security + JWT, curator-only login (visitors are
  anonymous at MVP — see Non-Goals in REQUIREMENTS.md).

## 4. Frontend (Next.js)

Two experiences, one repo, split by route group — not two deployments,
to keep the low-budget stack simple:

- `app/(public)/…` — SSG/ISR, Tailwind CSS, tuned for SEO/Core Web
  Vitals. Revalidates on a schedule (ISR) *and* on-demand, triggered by
  the promote endpoint, so a fresh approval goes live in seconds, not at
  the next full rebuild.
- `app/(admin)/…` — client-rendered, auth-gated, shadcn/ui for fast
  internal-tool UI. The single-card review screen (FR-3.1) lives here.
- Both share a generated API client/types from the Spring Boot OpenAPI
  spec.

## 5. Hosting (low-budget)

Hybrid, by explicit decision: the frontend stays on a PaaS free tier
(nothing to gain from self-hosting a static/ISR site); the backend
reuses [liviuionesi/lmdb.dev](https://github.com/liviuionesi/lmdb.dev)'s
proven $0-budget deploy mechanism as-is, per
[docs/architecture/adr/001-zero-budget-azure-deploy.md](docs/architecture/adr/001-zero-budget-azure-deploy.md).

| Component | Choice | Why |
|---|---|---|
| Next.js (public + admin) | Vercel free tier | matches the pitch; ISR works natively |
| Spring Boot API + PostgreSQL — local dev | Docker Compose (`infrastructure/docker/docker-compose.yml`) | one command, full parity with the cloud shape below |
| Spring Boot API + PostgreSQL — cloud demo | Terraform → Azure AKS, ephemeral (`infrastructure/terraform/azure/`) | `terraform apply` → demo → `terraform destroy`/idle auto-stop; nothing bills while it's not actually up |
| Images | Curator-uploaded, served by the backend (bucket choice deferred — Sprint 1 decision, not blocking) | |
| CI | GitHub Actions | build + test both apps on every PR; `docker-publish.yml` pushes the backend image to GHCR after Backend CI is green on `main`; `deploy.yml`/`destroy.yml` roll it out to/tear it down from AKS |

This is a deliberate reuse, not a coincidence: same Docker Compose shape
locally, same Terraform-provisioned AKS cluster in the cloud, same
ephemeral apply/destroy discipline, same idle auto-stop watchdog. See
`infrastructure/terraform/azure/variables.tf` for what's sized down from
lmdb.dev's original (one backend + one Postgres vs. eight services) and
what's identical (the mechanism itself).

## 6. Security & Compliance

- Admin routes sit behind Spring Security JWT; no admin surface is
  exposed through the public API path.
- Cookie consent + privacy policy must ship before any analytics/
  click-tracking goes live to real traffic (NFR-4) — this is a legal
  launch blocker for a `.de` consumer site, tracked as its own Epic in
  [.github/issues/PROJECT_ROADMAP.md](.github/issues/PROJECT_ROADMAP.md).

## 7. Deliberate deltas from the lmdb.dev reference architecture

The Scrum *methodology* here is copied identically from lmdb.dev (see
[docs/process/](docs/process/)) — but the *system* architecture is not.
Copying microservices/Kafka/Zipkin-grade infrastructure onto a
pre-revenue curation MVP would be over-engineering for what this project
actually needs right now:

- **One Spring Boot app, not eight microservices** — there's no
  independent-scaling or independent-deploy need yet.
- **No message broker** — ingestion → curation → publish is a
  straight-through DB read/write, not an event stream.
- **Observability starts at Actuator health + logs.** Prometheus/
  Grafana/ELK is a "once there's real traffic" upgrade, not a day-one
  requirement.

If echtgut later needs to split a piece out (e.g. the ingestion
scheduler under real load), that's a future ADR — not a default posture
borrowed from a different project at a different scale.
