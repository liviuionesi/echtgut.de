# Non-Functional Requirements (standing checklist)

Applied where relevant, not restated per issue — a Story/Task only needs
to justify skipping one of these, not repeat all of them every time.

- **Cost**: stays within the near-$0 free-tier stack (REQUIREMENTS.md
  NFR-1) unless the issue explicitly says otherwise.
- **Performance/SEO**: public pages stay statically generated or ISR'd;
  no regression that forces a public route to full SSR (NFR-2).
- **Data quality**: the `curated_experiences` promotion invariant (image
  + verified location + non-empty description, FR-3.5) is never
  bypassed, including by new ingestion sources (NFR-3).
- **Legal/GDPR**: any change touching visitor data (submissions, click
  tracking, future accounts) is checked against the consent/privacy
  posture (Epic F) before it ships to real traffic (NFR-4).
- **Security**: no secrets in code/commits/logs; inputs validated at
  service boundaries; admin endpoints stay behind the
  `CURATOR`/`ADMIN`-gated JWT auth pattern (NFR-5).
- **Curator throughput**: a change to the approve/reject flow is
  justified by *removing* friction, not adding a click "just this once"
  (NFR-6).
- **Observability**: new endpoints get Actuator health + structured
  logging, matching what already exists — no premature metrics/tracing
  stack (NFR-7). A new architectural decision gets its own ADR (see
  `docs/architecture/adr/`), not a comment.
- **Documentation**: Javadoc/TSDoc per `CLAUDE.md`'s standard.

Full detail and rationale for each NFR:
[REQUIREMENTS.md §5](../../REQUIREMENTS.md#5-non-functional-requirements).
