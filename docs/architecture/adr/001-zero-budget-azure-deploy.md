# ADR-001: $0 Cloud Budget — Docker Compose Locally, Ephemeral Azure via Terraform

**Status:** Accepted
**Date:** 2026-08-21
**Deciders:** Project owner

## Context

echtgut is pre-revenue and must run at effectively $0 infrastructure
cost. The project owner already runs
[liviuionesi/lmdb.dev](https://github.com/liviuionesi/lmdb.dev) on
exactly this pattern (its own ADR-004) and asked explicitly for the same
deploy mechanism here, rather than picking a different one — a deliberate
reuse decision, not a default.

An earlier draft of this document proposed a PaaS backend (Render/
Fly.io free tier) alongside a managed Postgres (Neon/Supabase). That was
superseded mid-session by explicit instruction to match lmdb.dev's
mechanism instead.

## Decision

1. **Local-first**: `docker compose up` (`infrastructure/docker/
   docker-compose.yml`) runs backend + Postgres for day-to-day
   development — no cloud dependency to iterate.
2. **Frontend stays on Vercel** (explicit instruction) — a static/ISR
   Next.js site gains nothing from self-hosting, so this is not part of
   the $0-Azure-reuse decision below; it's simply the right tool.
3. **Backend + Postgres reuse lmdb.dev's Azure path as-is**: Terraform
   (`infrastructure/terraform/azure/`, calling the same `network`/
   `cluster-aks`/`budget-guard` modules lmdb.dev uses, copied verbatim)
   provisions an ephemeral AKS cluster; `kubectl apply -k
   infrastructure/kubernetes/overlays/azure` rolls out the backend
   Deployment and Postgres StatefulSet; `terraform destroy` (or the idle
   auto-stop watchdog) releases it back to $0.
4. **Sized down, not re-architected**: echtgut is one Spring Boot app +
   one Postgres instance, against lmdb.dev's eight services + four
   datastores. The VM size drops accordingly (`Standard_D2ls_v7`, the
   size lmdb.dev itself used for its own single-service slice) but the
   mechanism — Terraform → AKS, NodePort + Caddy for TLS instead of a
   billable Load Balancer, budget-guard tripwire as the first resource
   applied — is unchanged.
5. **AWS is out of scope.** lmdb.dev supports both Azure and AWS
   (k3s-on-EC2); echtgut only needs one cloud target, so only the Azure
   path was ported.

## Options Considered

**PaaS backend (Render/Fly.io) + managed Postgres (Neon/Supabase)** —
this session's own first draft; superseded by explicit instruction to
reuse the proven mechanism instead of introducing a second, different
one to operate.

**Always-on paid cloud (~$20/month)** — rejected: the budget is $0, not
"low."

## Consequences

- Easier: the deploy/destroy/idle-stop GitHub Actions workflows, the
  Terraform modules, and the Kustomize overlay pattern are all reused
  nearly verbatim from a project that has already debugged them against
  real Azure behavior (region availability, VM size quota, resource
  provider registration, NodePort-vs-LoadBalancer billing, IPv6 DNS
  resolution inside the AKS overlay network) — see the inline comments
  in `infrastructure/terraform/azure/` and
  `infrastructure/kubernetes/overlays/azure/` for what each one is
  defending against.
- Harder: AKS is arguably oversized for a single container — a
  container-instance-style Azure service would be simpler. Accepted
  anyway because "the same way" was the explicit ask, and the proven
  mechanism's reliability is worth more right now than a smaller
  footprint for one container.
- Revisit: if a single-container Azure service (e.g. Container Apps)
  turns out meaningfully simpler in practice once this is actually run,
  that's a future ADR — not a default to switch to silently.
