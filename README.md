# echtgut.de

A curated local deals & experiences marketplace for the German market —
"echt gut" (really good): trust through hand-review, not volume.

## Start here

- [REQUIREMENTS.md](REQUIREMENTS.md) — product vision, functional and
  non-functional requirements.
- [ARCHITECTURE.md](ARCHITECTURE.md) — technical design: data model,
  backend/frontend shape, hosting.
- [.github/issues/PROJECT_ROADMAP.md](.github/issues/PROJECT_ROADMAP.md) —
  the Epic/Story/Task backlog and Sprint plan.
- [docs/process/](docs/process/) — the Scrum methodology this project
  runs (copied identically from
  [liviuionesi/lmdb.dev](https://github.com/liviuionesi/lmdb.dev), at the
  project owner's request).
- [CLAUDE.md](CLAUDE.md) — the working contract for any Claude Code
  session (human-driven or scheduled) picking up work here.

## Stack

- **Backend**: Spring Boot (Gradle), PostgreSQL, Flyway.
- **Frontend**: Next.js (App Router), Tailwind CSS, shadcn/ui.
- **Hosting**: frontend on Vercel; backend + Postgres via Docker Compose
  locally and Terraform-provisioned Azure AKS for cloud demos (see
  ARCHITECTURE.md §5 and
  [docs/architecture/adr/001-zero-budget-azure-deploy.md](docs/architecture/adr/001-zero-budget-azure-deploy.md)).

## Local development

```bash
cd infrastructure/docker
cp .env.example .env   # fill in POSTGRES_PASSWORD, JWT_SECRET
docker compose up -d
```

Backend and frontend project scaffolding is Sprint 0 work — see the
roadmap linked above.
