# Requirements — echtgut.de

## 1. Product Vision

echtgut.de ("echt gut" — German for "really good") is a **curated** local
deals & experiences marketplace for the German market. Unlike a
volume-based aggregator (Groupon-style), it shows only hand-reviewed,
editorially-polished listings. The trust signal *is* the product: "if
it's on echtgut, it's good."

## 2. Problem Statement

- Consumers suffer decision fatigue from bulk deal aggregators full of
  low-quality, expired, or misleading listings.
- Great small local businesses (a hidden sauna, an obscure museum, an
  amazing but non-marketing-savvy spa) have no channel to reach
  discovery-minded consumers.
- Existing aggregators optimize for listing *volume*, not listing
  *quality* — the opposite of what builds repeat-visit trust.

## 3. Target Users / Roles

| Role | Need |
|---|---|
| **Visitor** (consumer) | Browse trustworthy, high-quality local experiences/deals without wading through junk. |
| **Curator** (Liviu, later a small team) | Review incoming raw listings fast, polish them, publish with minimal friction. |
| **Local Business / Provider** | Get discovered without needing marketing sophistication; optionally, an affiliate/booking channel. |
| **Community Submitter** | Any visitor who wants to nominate a "hidden gem." |

## 4. Functional Requirements

### FR-1 Ingestion (Automated)
- **FR-1.1** Scheduled jobs pull candidate listings from external sources
  (affiliate feeds, OpenStreetMap POIs, scraped sources) into
  `raw_deals`, status `PENDING`.
- **FR-1.2** Each raw deal retains its source and source-native reference
  ID, for dedup and re-sync.
- **FR-1.3** Ingestion never touches `curated_experiences` directly — no
  path bypasses curation.

### FR-2 Ingestion (Manual / Community)
- **FR-2.1** A curator can manually add a raw deal directly (the "Hidden
  Gem" workflow) without waiting for a feed.
- **FR-2.2** A public "Submit a Local Gem" form lets any visitor propose
  a listing; submissions land in `raw_deals`, status `PENDING`, source
  `COMMUNITY`.
- **FR-2.3** Community submissions are rate-limited and spam-guarded
  (captcha or equivalent) before insertion.

### FR-3 Curation Workflow
- **FR-3.1** The curator dashboard (`/admin`) shows one `PENDING` raw
  deal at a time — no list-scanning required to start reviewing
  ("Tinder for deals").
- **FR-3.2** Curator actions: **Reject** (status → `REJECTED`, reason
  optional) or **Edit & Approve** (status → `PROMOTED`).
- **FR-3.3** On approve, the curator can rewrite the title, rewrite the
  description, choose/upload a high-res image, assign one or more
  taxonomy tags, and add editorial notes.
- **FR-3.4** Approval **upserts** a row into `curated_experiences`, keyed
  by a stable link back to the source `raw_deals.id`, so re-approving an
  updated raw deal updates the live listing instead of duplicating it.
- **FR-3.5** A promoted `curated_experiences` row cannot go live without:
  a high-res image, a verified location (lat/long or address), and a
  non-empty editorial description — enforced at the API layer, not just
  the UI.
- **FR-3.6** Curator actions are audit-logged (who, when, what changed) —
  minimum: curator id + timestamp on both tables.

### FR-4 Public Site
- **FR-4.1** Visitors browse curated experiences by tag/category (e.g.
  "Relaxation," "Fix My Back," "Rainy Day") and by city/region.
- **FR-4.2** Each listing has its own statically-generated (SSG/ISR)
  detail page: editorial title, description, image(s), location, and a
  "Book Now" / "View Deal" call-to-action.
- **FR-4.3** "Book Now" resolves to either an affiliate link (tagged for
  attribution) or a direct contact/booking path for gems with no
  affiliate program.
- **FR-4.4** The site is served primarily in German (`de`); the
  architecture must not preclude adding `en` later (i18n-ready routing,
  not necessarily i18n-complete at MVP).
- **FR-4.5** *(added post-MVP-draft, at the project owner's request)* The
  site supports a dark theme ("Ink," the default) and a light theme
  ("Paper"), toggleable and persisted per visitor, with no flash of the
  wrong theme on load. This is a brand decision, not a compliance
  checkbox — see ARCHITECTURE.md §4 for the token system and why dark is
  the default.

### FR-5 Admin / Curator Portal
- **FR-5.1** Authenticated, role-gated (`CURATOR`/`ADMIN`) — never
  publicly reachable.
- **FR-5.2** Pending-queue view, single-item review view, taxonomy
  management (create/rename/retire tags), and a basic list of live
  `curated_experiences` for edits/unpublish.

### FR-6 Monetization
- **FR-6.1** Affiliate click-throughs are tracked (which listing, when)
  before redirecting out.
- **FR-6.2** Minimal reporting: clicks per listing, per tag, per week —
  enough to see what's working.

## 5. Non-Functional Requirements

- **NFR-1 Cost.** Must run on a near-$0 stack while pre-revenue —
  free-tier hosting (Vercel for frontend), free/cheap managed Postgres,
  no paid infra until there's traffic or revenue to justify it.
- **NFR-2 Performance/SEO.** Public pages are statically generated or
  ISR'd; a curated listing changes rarely, so there's no excuse for a
  slow public page. Core Web Vitals matter — SEO is the primary
  acquisition channel given zero ad budget.
- **NFR-3 Data quality gate.** The promotion validation in FR-3.5 is a
  hard invariant, enforced server-side — the whole pitch of the product
  collapses if a junk listing reaches production.
- **NFR-4 Legal / GDPR** *(not in the original pitch — flagged here as a
  gap)*. This is a Germany-facing consumer site processing visitor data
  (community submissions, click tracking, eventually accounts).
  Required before any real traffic: a cookie/consent banner, a privacy
  policy, a lawful basis for click-tracking analytics, and data-handling
  terms with any affiliate network. Treat this as MVP-blocking for a real
  (non-localhost) launch, not a later polish item.
- **NFR-5 Security.** Admin portal behind auth; public write paths
  (community submission) validated and rate-limited server-side, not
  just client-side.
- **NFR-6 Curator throughput.** The single biggest operational risk is
  curation being a manual bottleneck. Every click removed from the
  approve/reject flow compounds — this is a first-class design
  constraint, not a nice-to-have.
- **NFR-7 Observability (lightweight).** Actuator health + structured
  logs from day one; defer full metrics/tracing stacks until there's
  real traffic to justify the operational overhead.
- **NFR-8 Frontend quality gates** *(added post-MVP-draft, at the project
  owner's request — "not only looks good but performs on top score on
  Lighthouse, and is fully tested")*:
  - Lighthouse (desktop): Performance ≥ 90, Accessibility ≥ 95,
    Best Practices ≥ 95, SEO ≥ 95 — enforced in CI
    (`frontend/lighthouserc.json`), not just measured.
  - Test coverage ≥ 85% (lines/branches/functions/statements) via Vitest
    — enforced in CI, not advisory.
  - No low-quality commit reaches `develop`: a Husky pre-commit hook
    (lint-staged: Prettier + ESLint on staged files) plus the existing
    commit-msg issue-number check (`.husky/`, see CLAUDE.md) gate every
    commit, not just CI after the fact.

## 6. Explicit Non-Goals (MVP)

- No native mobile app.
- No user accounts/login for visitors at MVP (browsing and community
  submission are anonymous; only curators authenticate).
- No in-house payments/booking engine — booking happens on the
  provider's or affiliate's side.
- No multi-curator permission nuance at MVP (single curator role is
  enough until there's a team).

---
See [ARCHITECTURE.md](ARCHITECTURE.md) for the technical design this
implies, and [.github/issues/PROJECT_ROADMAP.md](.github/issues/PROJECT_ROADMAP.md)
for the Epics/Stories/Tasks that deliver it.
