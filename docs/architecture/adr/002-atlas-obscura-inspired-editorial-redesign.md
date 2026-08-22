# ADR-002: Atlas Obscura-Inspired Editorial Redesign

**Status:** Accepted
**Date:** 2026-08-22
**Deciders:** Project owner

## Context

The Ink/Paper design system shipped in Sprint 0 (Story #41, closed) —
moss-green/gold tokens, Fraunces + Inter, dark "Ink" as the default theme
— was chosen deliberately against the three looks every AI-styled site
defaults to (see ARCHITECTURE.md §4.1's original commentary). It is still
sound as a *token system*; nothing here throws it out.

The project owner has since pointed to
[Atlas Obscura](https://www.atlasobscura.com) as the specific design
reference to adapt toward. Atlas Obscura's own identity is built on a
light, warm-paper background with large-format photography as the
primary hero element, editorial long-form copy, taxonomy badges on
photo-led cards, and a "practical info" box that separates narrative
trust-building from transactional facts (address, hours, how to visit).
That structural language — not Atlas Obscura's specific hex values or
copy — is what's worth adapting: it maps directly onto echtgut's own
pitch ("if it's on echtgut, it's good") better than a generic deals-grid
would.

Two things are explicitly *not* being copied wholesale:
1. Atlas Obscura's own literal palette/typefaces — copying them verbatim
   would just trade one templated look for a different, still-borrowed
   one. The existing moss-green/gold identity is kept as the accent
   system precisely because it was already chosen as the
   anti-generic-AI-cliché move (see below).
2. Atlas Obscura's "cabinet of curiosities / bizarre & wondrous" editorial
   tone — echtgut's pitch is *quality*, not *oddity*. The redesign adopts
   the structural pattern (photo-led cards, badges, editorial narrative +
   practical-facts split) with echtgut's own calmer, trust-first voice.

## Decision

1. **Default theme flips from dark "Ink" to light "Paper."** Confirmed
   explicitly with the project owner — this reverses Story #41's original
   default, on the basis that a light, warm-paper background with
   photography as the first impression is core to the Atlas Obscura
   reference, not an incidental detail. Dark "Ink" is **not** removed —
   it stays as the opt-in toggle, same token values, same no-FOUC
   inline-script mechanism (FR-4.5 / ARCHITECTURE.md §4.1).
2. **No new hue is added to the palette.** The reference's photography and
   layout language is adopted; its literal color choices are not. Moss
   green (accent/CTA) and gold (curation marker) remain the only accent
   hues — doubling down on the palette this project already reasoned
   about, rather than reaching for a third color (e.g. a terracotta/rust
   tag accent) that would recreate the "warm cream + serif + terracotta"
   default the original design-system work explicitly rejected.
3. **New editorial layout patterns**, detailed in ARCHITECTURE.md §4.1.1:
   photo-led experience cards with an overlaid taxonomy badge, an
   "Explore by mood" taxonomy tile grid (extends FR-4.1's existing
   tag/city browsing), and a detail-page "Practical Info" panel that
   visually separates the editorial narrative (title, curator's
   description, imagery) from transactional facts (address, price hint,
   Book Now) — the one signature element of this redesign, because it
   makes the product's actual pitch ("read the curation first, the
   transaction second") a literal, visible layout decision rather than
   just copy.
4. **A public curatorial attribution line** is added to the detail page
   ("Curated by the echtgut team," not an individual curator's name by
   default) — Atlas Obscura's byline pattern is a real trust signal worth
   adopting, but attributing to an individual curator would expose new
   personal data on a page with zero visitor auth (NFR-4/GDPR territory)
   for no product benefit at MVP scale (one curator). Team-level
   attribution gets the trust signal without the exposure. Per-curator
   attribution is left as a future decision once there's an actual team,
   not decided here.
5. **A lightweight, free-tier location map** (Leaflet + OpenStreetMap
   tiles — no API key, no cost, consistent with NFR-1) is proposed for
   the detail page's Practical Info panel, matching Atlas Obscura's
   place-detail maps. Scoped as its own low-priority Task so it doesn't
   block the rest of the redesign if it turns out not to be worth the
   effort once the rest ships.

## Options Considered

**Copy Atlas Obscura's palette/typefaces directly (cream background,
Atlas Obscura's serif, its accent color)** — rejected: this just swaps
one borrowed identity for another and undoes the anti-cliché reasoning
the original design system was built on.

**Keep dark "Ink" as the default, adapt only the structural patterns
(cards, badges, Practical Info panel) onto the existing dark-first
palette** — a real alternative, discussed with the project owner
explicitly; not chosen because the light/photography-first impression is
core to what "Atlas Obscura-inspired" means here, not a detail that
survives independently of the background choice.

**Full re-architecture into a magazine/CMS-style site (articles, trips,
contributor accounts)** — out of scope: echtgut is a curated
deals/experiences marketplace, not an editorial publication; the
redesign borrows Atlas Obscura's *visual and structural* language, not
its content model.

## Consequences

- Easier: the moss-green/gold token values and Fraunces/Inter pairing
  carry over unchanged — this is a default flip and a set of new layout
  patterns, not a rebuild of the token system.
- Harder: every place that assumed dark-as-default (visual QA baselines,
  Lighthouse contrast checks, any screenshot-based test) needs
  re-verification against the new default — tracked as explicit
  Acceptance Criteria on the redesign Epic's first Story, not assumed.
- Harder: photography becomes load-bearing for the first time (cards and
  hero images were previously a secondary element). Image
  optimization/CDN behavior and Core Web Vitals (NFR-2) need active
  attention on the new card/detail components, not just carried over from
  the text-first version.
- Revisit: per-curator public attribution, once there's an actual curator
  team — that's a future ADR + a new `curated_experiences` column, not a
  default to slide into once convenient.

See [ARCHITECTURE.md §4.1](../../ARCHITECTURE.md#41-design-system-fr-45)
for the resulting token/component spec and
[REQUIREMENTS.md §4](../../REQUIREMENTS.md#4-functional-requirements) for
the updated FR-4.5–4.8.
