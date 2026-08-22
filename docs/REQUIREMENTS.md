# Requirements — echtgut.de

## 1. Product Vision

echtgut.de ("echt gut" — German for "really good") is an **automated discovery engine** for local deals & experiences in the German market. Unlike a volume-based aggregator (Groupon-style) full of low-quality or expired listings, it automatically pulls high-quality, real-world data (Google Places, OpenStreetMap) and presents it in a curated, editorial visual language. The trust signal *is* the product: "if it's on echtgut, it's good."

## 2. Problem Statement

- Consumers suffer decision fatigue from bulk deal aggregators full of misleading listings.
- Great small local businesses (a hidden sauna, an obscure museum, an amazing but non-marketing-savvy spa) have no channel to reach discovery-minded consumers.
- A manual curation model fails the cold-start problem (requires massive community effort to populate the database before showing value). Therefore, data must be aggregated automatically.

## 3. Target Users / Roles

| Role | Need |
|---|---|
| **Visitor** (consumer) | Browse trustworthy, high-quality local experiences/deals without wading through junk. |
| **Platform Engineer** | Ensure external APIs are correctly mapped, cached, and aggregated to present high-quality listings automatically. |
| **Local Business / Provider** | Get discovered without needing marketing sophistication; optionally, an affiliate/booking channel. |

## 4. Functional Requirements

### FR-1 Automated Data Aggregation (ADR-003)
- **FR-1.1** The system fetches local places dynamically using the `PlaceAggregatorService`, pulling data primarily from the **Google Places API (New)**.
- **FR-1.2** In the absence of a Google API Key, or for fallback scenarios, the system seamlessly degrades to use the **OpenStreetMap (Overpass API)** for real-world POIs.
- **FR-1.3** Aggregated data is normalized into a unified DTO layer before being sent to the frontend.
- **FR-1.4** To prevent excessive API costs and respect rate limits, requests to external APIs are cached appropriately.

### FR-2 Public Site
- **FR-2.1** Visitors browse aggregated experiences by tag/category (e.g. "Relaxation," "Fix My Back," "Rainy Day") and by city/region.
- **FR-2.2** The system renders high-quality images directly from the Google Places API (or fallbacks) instead of relying on a curator to upload them.
- **FR-2.3** "Book Now" resolves to either an affiliate link (tagged for attribution) or a direct contact/booking path.
- **FR-2.4** The site is served primarily in German (`de`); the architecture must not preclude adding `en` later (i18n-ready routing, not necessarily i18n-complete at MVP).
- **FR-2.5** *(added post-MVP-draft, at the project owner's request; default flipped by* [ADR-002](architecture/adr/002-atlas-obscura-inspired-editorial-redesign.md)*)* The site supports a light theme ("Paper," **the default**) and a dark theme ("Ink"), toggleable and persisted per visitor.
- **FR-2.6** *(added with the Atlas Obscura-inspired redesign, ADR-002)* Experience listings are browsed primarily through photo-led cards (hero image + taxonomy badge overlay + editorial title/hook), not a text-first list. The site uses a "Glassmorphism" grid view.
- **FR-2.7** *(added with the Atlas Obscura-inspired redesign, ADR-002)* The visual language relies heavily on the quality of external imagery and data. The map feature integrates directly to display the aggregated coordinates.

### FR-3 Monetization
- **FR-3.1** Affiliate click-throughs are tracked (which listing, when) before redirecting out.
- **FR-3.2** Minimal reporting: clicks per listing, per tag, per week — enough to see what's working.

## 5. Non-Functional Requirements

- **NFR-1 Cost.** Must run on a near-$0 stack while pre-revenue. External API usage (Google Places) must be strictly controlled to stay within the free tier.
- **NFR-2 Performance/SEO.** Public pages must be fast. Core Web Vitals matter — SEO is the primary acquisition channel given zero ad budget.
- **NFR-3 Data quality gate.** The aggregation service must filter out poorly rated or incomplete places.
- **NFR-4 Legal / GDPR** *(not in the original pitch — flagged here as a gap)*. This is a Germany-facing consumer site processing visitor data. Required before any real traffic: a cookie/consent banner, a privacy policy, a lawful basis for click-tracking analytics, and data-handling terms with any affiliate network. Treat this as MVP-blocking for a real (non-localhost) launch.
- **NFR-5 Security.** External API keys (e.g., `GOOGLE_PLACES_API_KEY`) must never be committed to the public repository. They must be loaded via `.env` at runtime.
- **NFR-6 Observability (lightweight).** Actuator health + structured logs from day one; defer full metrics/tracing stacks until there's real traffic to justify the operational overhead.
- **NFR-7 Frontend quality gates** *(added post-MVP-draft)*:
  - Lighthouse (desktop): Performance ≥ 90, Accessibility ≥ 95, Best Practices ≥ 95, SEO ≥ 95.
  - Test coverage ≥ 85% (lines/branches/functions/statements) via Vitest.
  - No low-quality commit reaches `develop`: a Husky pre-commit hook (lint-staged: Prettier + ESLint on staged files) plus the existing commit-msg issue-number check (`.husky/`).

## 6. Explicit Non-Goals (MVP)

- No native mobile app.
- No user accounts/login for visitors at MVP.
- No in-house payments/booking engine — booking happens on the provider's or affiliate's side.
- No manual curation workflows, moderation queues, or custom admin portals.

---
See [ARCHITECTURE.md](ARCHITECTURE.md) for the technical design this implies, and [.github/issues/PROJECT_ROADMAP.md](.github/issues/PROJECT_ROADMAP.md) for the Epics/Stories/Tasks that deliver it.
