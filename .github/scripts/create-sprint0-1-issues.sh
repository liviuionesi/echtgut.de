#!/bin/bash
# echtgut.de — Seeds the initial Epic/Story/Task backlog (Sprint 0-1 plus
# the unscheduled Product Backlog beyond it) as real GitHub issues, wires
# them into the Project board, and cross-links Epic<->Story<->Task.
#
# Style follows liviuionesi/lmdb.dev's own .github/scripts/create-phase*-issues.sh
# (same collaborator, same convention: colored banners, gh issue create
# capturing the returned URL) — updated to the CURRENT methodology
# (.github/ISSUE_TEMPLATE/*.md + Sprint Milestones + Project custom
# fields) rather than the superseded sprint-N-label scheme those scripts
# used. See docs/process/METHODOLOGY.md.
#
# Idempotency: none — this is a one-time seed script, run once against a
# freshly created repo. Re-running it creates duplicate issues.

set -e

REPO="liviuionesi/echtgut.de"
OWNER="liviuionesi"
PROJECT=13

RED='\033[0;31m'; GREEN='\033[0;32m'; BLUE='\033[0;34m'; NC='\033[0m'

if ! command -v gh &> /dev/null; then
  echo -e "${RED}Error: GitHub CLI (gh) is not installed${NC}"; exit 1
fi
if ! gh auth status &> /dev/null; then
  echo -e "${RED}Error: Not authenticated with GitHub${NC}"; exit 1
fi
echo -e "${GREEN}✓ GitHub CLI authenticated${NC}"

# --- helpers ------------------------------------------------------------

# create_issue TITLE LABELS MILESTONE BODY -> prints issue number
create_issue() {
  local title="$1" labels="$2" milestone="$3" body="$4"
  local url
  if [ -n "$milestone" ]; then
    url=$(gh issue create --repo "$REPO" --title "$title" --label "$labels" --milestone "$milestone" --body "$body")
  else
    url=$(gh issue create --repo "$REPO" --title "$title" --label "$labels" --body "$body")
  fi
  echo "${url##*/}"
}

# set_fields URL STATUS PRIORITY SIZE ESTIMATE  (any field empty = skip it)
# NOTE: proper if/fi, not `[ -n "$x" ] && cmd` as a bare statement — under
# `set -e`, that bare form propagates the *test's own* failure (when $x
# is empty) as this function's exit status and aborts the whole script.
# Confirmed live: this exact shape silently killed the first run right
# after Epic A. if/fi's exit status is 0 when the condition is false,
# which sidesteps the trap without swallowing a genuine gh failure.
set_fields() {
  local url="$1" status="$2" priority="$3" size="$4" estimate="$5"
  gh project item-add "$PROJECT" --owner "$OWNER" --url "$url" >/dev/null
  if [ -n "$status" ];   then gh project item-edit "$PROJECT" --owner "$OWNER" --url "$url" --field "Status"   --value "$status"    >/dev/null; fi
  if [ -n "$priority" ]; then gh project item-edit "$PROJECT" --owner "$OWNER" --url "$url" --field "Priority" --value "$priority"  >/dev/null; fi
  if [ -n "$size" ];     then gh project item-edit "$PROJECT" --owner "$OWNER" --url "$url" --field "Size"     --value "$size"      >/dev/null; fi
  if [ -n "$estimate" ]; then gh project item-edit "$PROJECT" --owner "$OWNER" --url "$url" --field "Estimate" --number "$estimate" >/dev/null; fi
}

echo -e "${BLUE}=== Milestones (Sprints) ===${NC}"
SPRINT0_DUE=$(date -u -d "+1 week" +%Y-%m-%dT00:00:00Z 2>/dev/null || date -u -v+1w +%Y-%m-%dT00:00:00Z)
SPRINT1_DUE=$(date -u -d "+3 weeks" +%Y-%m-%dT00:00:00Z 2>/dev/null || date -u -v+3w +%Y-%m-%dT00:00:00Z)

gh api repos/$REPO/milestones -f title="Sprint 0" -f due_on="$SPRINT0_DUE" \
  -f description="Bootstrap: scaffolded Spring Boot + Next.js monorepo, CI green, Docker Compose + Terraform/Azure deploy path proven end-to-end — nothing user-facing yet, but Sprint 1 starts on solid ground." >/dev/null
gh api repos/$REPO/milestones -f title="Sprint 1" -f due_on="$SPRINT1_DUE" \
  -f description="Ship the airlock: raw_deals/curated_experiences schema live, ingestion pipeline wired, and a curator can review-and-promote one deal end-to-end through the admin API." >/dev/null
echo -e "${GREEN}✓ Sprint 0 and Sprint 1 milestones created${NC}"

# =========================================================================
echo -e "${BLUE}=== Epic A: Foundational Setup & Infrastructure ===${NC}"
EPIC_A=$(create_issue "[EPIC] Foundational Setup & Infrastructure" "epic,P1-high,infrastructure,backend,frontend" "" "$(cat <<'EOF'
## Epic
Scaffold the Spring Boot backend and Next.js frontend, get CI green, and
prove the local-and-cloud deploy path (Docker Compose -> Terraform/Azure
AKS) end-to-end — before any feature work starts.

## Business Value
Every later Story assumes a working build, a working deploy path, and a
CI that actually gates quality. Doing this first means feature Sprints
spend their time on the product, not on infrastructure surprises.

## Product Goal alignment
Direct prerequisite — see docs/process/PRODUCT_GOAL.md.

## Child Stories
- [ ] #TBD_A1
- [ ] #TBD_A2

## Notes
Architecture reference: ARCHITECTURE.md. Deploy mechanism and why it's
Docker Compose + Terraform/Azure specifically:
docs/architecture/adr/001-zero-budget-azure-deploy.md.
EOF
)")
set_fields "https://github.com/$REPO/issues/$EPIC_A" "Backlog" "P1" "" ""
echo -e "${GREEN}✓ Epic A: #$EPIC_A${NC}"

STORY_A1=$(create_issue "[STORY] As a developer, I want scaffolded Spring Boot + Next.js projects with CI, so that feature work starts on a consistent, tested foundation" "user-story,P1-high,backend,frontend" "Sprint 0" "$(cat <<EOF
## User Story
**As a** developer
**I want** scaffolded Spring Boot and Next.js projects with CI wired up
**So that** feature work starts on a consistent, tested foundation

## Acceptance Criteria (Given/When/Then)
- [ ] Given a fresh clone, when I run \`cd backend && ./gradlew build\`, then it succeeds with no source changes needed
- [ ] Given a fresh clone, when I run \`cd frontend && npm ci && npm run build\`, then it succeeds
- [ ] Given a trivial commit, when CI runs, then both Backend CI and Frontend CI (\`.github/workflows/\`) go green

## Definition of Ready
- [x] Meets [Definition of Ready](../../docs/process/DEFINITION_OF_READY.md)

## Story Points
**Estimate:** 5

## Sprint
**Milestone:** Sprint 0

## Technical Tasks
- [ ] #TBD_A1_1
- [ ] #TBD_A1_2
- [ ] #TBD_A1_3

## Definition of Done
- [ ] Meets [Definition of Done](../../docs/process/DEFINITION_OF_DONE.md)

## Notes
Parent epic: #$EPIC_A
EOF
)")
set_fields "https://github.com/$REPO/issues/$STORY_A1" "Backlog" "P1" "M" "5"
echo -e "${GREEN}✓ Story A1: #$STORY_A1${NC}"

TASK_A1_1=$(create_issue "[TASK] Scaffold Spring Boot Gradle project (backend/)" "task,backend" "Sprint 0" "$(cat <<EOF
## Task
Scaffold the single-module Spring Boot Gradle project under \`backend/\`
following the package layout in ARCHITECTURE.md §3
(ingestion/curation/catalog/taxonomy/submission), with Flyway and the
Postgres driver wired to the local Docker Compose instance.

## Parent Story
Parent: #$STORY_A1

## Acceptance Criteria
- [ ] \`./gradlew build\` succeeds from a fresh clone
- [ ] Flyway runs an initial (empty/placeholder) migration successfully against the Compose Postgres
- [ ] Actuator health endpoint responds

## Estimate
**Hours:** 4

## Notes
EOF
)")
set_fields "https://github.com/$REPO/issues/$TASK_A1_1" "Backlog" "" "" "4"
echo -e "${GREEN}✓ Task A1.1: #$TASK_A1_1${NC}"

TASK_A1_2=$(create_issue "[TASK] Scaffold Next.js App Router project (frontend/)" "task,frontend" "Sprint 0" "$(cat <<EOF
## Task
Scaffold the Next.js App Router project under \`frontend/\` with
\`(public)\` and \`(admin)\` route groups (ARCHITECTURE.md §4), Tailwind
CSS and shadcn/ui installed.

## Parent Story
Parent: #$STORY_A1

## Acceptance Criteria
- [ ] \`npm run build\` succeeds from a fresh clone
- [ ] Both route groups render a placeholder page
- [ ] Tailwind + shadcn/ui confirmed working (one styled component in each route group)

## Estimate
**Hours:** 3

## Notes
EOF
)")
set_fields "https://github.com/$REPO/issues/$TASK_A1_2" "Backlog" "" "" "3"
echo -e "${GREEN}✓ Task A1.2: #$TASK_A1_2${NC}"

TASK_A1_3=$(create_issue "[TASK] Verify GitHub Actions CI green on a trivial commit" "task,devops" "Sprint 0" "$(cat <<EOF
## Task
Push a trivial commit and confirm \`backend-ci.yml\` and \`frontend-ci.yml\`
both go green end-to-end (build, test, lint, coverage-threshold steps
that don't require SonarQube/OWASP secrets yet).

## Parent Story
Parent: #$STORY_A1

## Acceptance Criteria
- [ ] Backend CI green
- [ ] Frontend CI green
- [ ] Any step that needs a not-yet-configured secret (SonarQube, NVD_API_KEY) fails soft, not red, matching the workflow's own documented behavior

## Estimate
**Hours:** 2

## Notes
EOF
)")
set_fields "https://github.com/$REPO/issues/$TASK_A1_3" "Backlog" "" "" "2"
echo -e "${GREEN}✓ Task A1.3: #$TASK_A1_3${NC}"

# Backfill Story A1's Technical Tasks checklist now that task numbers exist
gh issue edit "$STORY_A1" --repo "$REPO" --body "$(cat <<EOF
## User Story
**As a** developer
**I want** scaffolded Spring Boot and Next.js projects with CI wired up
**So that** feature work starts on a consistent, tested foundation

## Acceptance Criteria (Given/When/Then)
- [ ] Given a fresh clone, when I run \`cd backend && ./gradlew build\`, then it succeeds with no source changes needed
- [ ] Given a fresh clone, when I run \`cd frontend && npm ci && npm run build\`, then it succeeds
- [ ] Given a trivial commit, when CI runs, then both Backend CI and Frontend CI (\`.github/workflows/\`) go green

## Definition of Ready
- [x] Meets [Definition of Ready](../../docs/process/DEFINITION_OF_READY.md)

## Story Points
**Estimate:** 5

## Sprint
**Milestone:** Sprint 0

## Technical Tasks
- [ ] #$TASK_A1_1
- [ ] #$TASK_A1_2
- [ ] #$TASK_A1_3

## Definition of Done
- [ ] Meets [Definition of Done](../../docs/process/DEFINITION_OF_DONE.md)

## Notes
Parent epic: #$EPIC_A
EOF
)" >/dev/null

STORY_A2=$(create_issue "[STORY] As a developer, I want the local-and-cloud deploy path proven end-to-end before feature work starts, so that shipping later is just a merge, not a first-time deploy exercise" "user-story,P1-high,infrastructure,devops" "Sprint 0" "$(cat <<EOF
## User Story
**As a** developer
**I want** the local-and-cloud deploy path proven end-to-end
**So that** shipping later is just "merge," not "first figure out deploy"

## Acceptance Criteria (Given/When/Then)
- [ ] Given \`backend/Dockerfile\` exists, when I run \`docker compose up\` in \`infrastructure/docker/\`, then backend and postgres both report healthy
- [ ] Given the Terraform bootstrap steps in \`infrastructure/terraform/README.md\`, when I run \`terraform apply\` then \`terraform destroy\`, then both complete cleanly against a real Azure subscription
- [ ] Given \`frontend/\`, when connected to a new Vercel project, then it deploys successfully

## Definition of Ready
- [x] Meets [Definition of Ready](../../docs/process/DEFINITION_OF_READY.md)

## Story Points
**Estimate:** 5

## Sprint
**Milestone:** Sprint 0

## Technical Tasks
- [ ] #TBD_A2_1
- [ ] #TBD_A2_2
- [ ] #TBD_A2_3

## Definition of Done
- [ ] Meets [Definition of Done](../../docs/process/DEFINITION_OF_DONE.md)

## Notes
Parent epic: #$EPIC_A. Depends on Story #$STORY_A1's backend/Dockerfile-adjacent
scaffolding existing first (the docker-compose \`backend\` service builds
from \`../../backend\`).
EOF
)")
set_fields "https://github.com/$REPO/issues/$STORY_A2" "Backlog" "P1" "M" "5"
echo -e "${GREEN}✓ Story A2: #$STORY_A2${NC}"

TASK_A2_1=$(create_issue "[TASK] Verify docker compose up — backend + postgres healthy end-to-end" "task,infrastructure" "Sprint 0" "$(cat <<EOF
## Task
Once \`backend/Dockerfile\` exists (Story #$STORY_A1), run
\`infrastructure/docker/docker-compose.yml\` end-to-end and confirm both
services report healthy.

## Parent Story
Parent: #$STORY_A2

## Acceptance Criteria
- [ ] \`docker compose up -d\` brings up postgres + backend
- [ ] Both containers' healthchecks report healthy within their start_period
- [ ] \`curl localhost:8080/actuator/health\` returns UP

## Estimate
**Hours:** 2

## Notes
EOF
)")
set_fields "https://github.com/$REPO/issues/$TASK_A2_1" "Backlog" "" "" "2"
echo -e "${GREEN}✓ Task A2.1: #$TASK_A2_1${NC}"

TASK_A2_2=$(create_issue "[TASK] Terraform Azure bootstrap — one apply/destroy cycle proven" "task,infrastructure,devops" "Sprint 0" "$(cat <<EOF
## Task
Follow \`infrastructure/terraform/README.md\`'s bootstrap steps against a
real Azure subscription: create the tfstate storage account, run
\`terraform init\`/\`apply\`, verify AKS access via \`kubectl get nodes\`,
then \`terraform destroy\` cleanly.

## Parent Story
Parent: #$STORY_A2

## Acceptance Criteria
- [ ] \`terraform apply\` succeeds and \`kubectl get nodes\` shows the node Ready
- [ ] \`terraform destroy\` succeeds with no orphaned resources left in the Azure portal
- [ ] Repo variables/secrets needed for the \`deploy.yml\`/\`destroy.yml\` workflows are documented as actually set (not just listed)

## Estimate
**Hours:** 4

## Notes
See docs/architecture/adr/001-zero-budget-azure-deploy.md for context.
EOF
)")
set_fields "https://github.com/$REPO/issues/$TASK_A2_2" "Backlog" "" "" "4"
echo -e "${GREEN}✓ Task A2.2: #$TASK_A2_2${NC}"

TASK_A2_3=$(create_issue "[TASK] Provision Vercel project for frontend/" "task,frontend,devops" "Sprint 0" "$(cat <<EOF
## Task
Connect \`frontend/\` to a new Vercel project and confirm a successful
deploy of the Sprint 0 scaffold.

## Parent Story
Parent: #$STORY_A2

## Acceptance Criteria
- [ ] Vercel project created, linked to the repo, building from \`frontend/\`
- [ ] A push to \`main\` triggers a successful deploy
- [ ] Preview deploys work on a branch push

## Estimate
**Hours:** 2

## Notes
EOF
)")
set_fields "https://github.com/$REPO/issues/$TASK_A2_3" "Backlog" "" "" "2"
echo -e "${GREEN}✓ Task A2.3: #$TASK_A2_3${NC}"

gh issue edit "$STORY_A2" --repo "$REPO" --body "$(cat <<EOF
## User Story
**As a** developer
**I want** the local-and-cloud deploy path proven end-to-end
**So that** shipping later is just "merge," not "first figure out deploy"

## Acceptance Criteria (Given/When/Then)
- [ ] Given \`backend/Dockerfile\` exists, when I run \`docker compose up\` in \`infrastructure/docker/\`, then backend and postgres both report healthy
- [ ] Given the Terraform bootstrap steps in \`infrastructure/terraform/README.md\`, when I run \`terraform apply\` then \`terraform destroy\`, then both complete cleanly against a real Azure subscription
- [ ] Given \`frontend/\`, when connected to a new Vercel project, then it deploys successfully

## Definition of Ready
- [x] Meets [Definition of Ready](../../docs/process/DEFINITION_OF_READY.md)

## Story Points
**Estimate:** 5

## Sprint
**Milestone:** Sprint 0

## Technical Tasks
- [ ] #$TASK_A2_1
- [ ] #$TASK_A2_2
- [ ] #$TASK_A2_3

## Definition of Done
- [ ] Meets [Definition of Done](../../docs/process/DEFINITION_OF_DONE.md)

## Notes
Parent epic: #$EPIC_A. Depends on Story #$STORY_A1's backend/Dockerfile-adjacent
scaffolding existing first (the docker-compose \`backend\` service builds
from \`../../backend\`).
EOF
)" >/dev/null

gh issue edit "$EPIC_A" --repo "$REPO" --body "$(cat <<EOF
## Epic
Scaffold the Spring Boot backend and Next.js frontend, get CI green, and
prove the local-and-cloud deploy path (Docker Compose -> Terraform/Azure
AKS) end-to-end — before any feature work starts.

## Business Value
Every later Story assumes a working build, a working deploy path, and a
CI that actually gates quality. Doing this first means feature Sprints
spend their time on the product, not on infrastructure surprises.

## Product Goal alignment
Direct prerequisite — see docs/process/PRODUCT_GOAL.md.

## Child Stories
- [ ] #$STORY_A1
- [ ] #$STORY_A2

## Notes
Architecture reference: ARCHITECTURE.md. Deploy mechanism and why it's
Docker Compose + Terraform/Azure specifically:
docs/architecture/adr/001-zero-budget-azure-deploy.md.
EOF
)" >/dev/null
echo -e "${GREEN}✓ Epic A fully wired (2 stories, 6 tasks)${NC}"

echo "EPIC_A=$EPIC_A STORY_A1=$STORY_A1 STORY_A2=$STORY_A2 TASK_A1_1=$TASK_A1_1 TASK_A1_2=$TASK_A1_2 TASK_A1_3=$TASK_A1_3 TASK_A2_1=$TASK_A2_1 TASK_A2_2=$TASK_A2_2 TASK_A2_3=$TASK_A2_3" >> /tmp/echtgut-issue-numbers.env

# --- native sub-issue links (populates the Project's real "Parent issue" /
# "Sub-issues progress" fields — the markdown checklists above are for
# human readability in the issue body itself, this is what the board's
# rollup actually tracks) ---
link_parent() { gh issue edit "$1" --repo "$REPO" --parent "$2" >/dev/null; }
link_parent "$STORY_A1" "$EPIC_A"
link_parent "$STORY_A2" "$EPIC_A"
link_parent "$TASK_A1_1" "$STORY_A1"
link_parent "$TASK_A1_2" "$STORY_A1"
link_parent "$TASK_A1_3" "$STORY_A1"
link_parent "$TASK_A2_1" "$STORY_A2"
link_parent "$TASK_A2_2" "$STORY_A2"
link_parent "$TASK_A2_3" "$STORY_A2"
echo -e "${GREEN}✓ Epic A native sub-issue links set${NC}"

# =========================================================================
# Epics B-F use a leaner helper: short bodies, native --parent linking
# instead of manually maintained checklists (the checklist in Epic A above
# is kept for readability; sub-issue links are what the board rolls up).
# =========================================================================

new_epic() { # title labels body -> number
  create_issue "[EPIC] $1" "epic,$2" "" "$3"
}
new_story() { # title labels(incl. priority) milestone points size priority body(=$7) -> number
  create_issue "[STORY] $1" "user-story,$2" "$3" "$7"
}
new_task() { # title labels milestone hours body -> number
  create_issue "[TASK] $1" "task,$2" "$3" "$5"
}

story_body() { # role goal benefit ac1 ac2 ac3 points milestone parent_epic notes
  cat <<EOF
## User Story
**As a** $1
**I want** $2
**So that** $3

## Acceptance Criteria (Given/When/Then)
- [ ] $4
- [ ] $5
$(if [ -n "$6" ]; then echo "- [ ] $6"; fi)

## Definition of Ready
- [x] Meets [Definition of Ready](../../docs/process/DEFINITION_OF_READY.md)

## Story Points
**Estimate:** $7

## Sprint
**Milestone:** ${8:-unassigned — not yet pulled into a Sprint}

## Technical Tasks
(tracked via linked GitHub sub-issues — see the Sub-issues panel on this issue)

## Definition of Done
- [ ] Meets [Definition of Done](../../docs/process/DEFINITION_OF_DONE.md)

## Notes
Parent epic: #$9${10:+. $10}
EOF
}

task_body() { # desc parent_story ac1 ac2 hours notes
  cat <<EOF
## Task
$1

## Parent Story
Parent: #$2

## Acceptance Criteria
- [ ] $3
- [ ] $4

## Estimate
**Hours:** $5

## Notes
${6:-}
EOF
}

epic_body() { # desc value goal notes
  cat <<EOF
## Epic
$1

## Business Value
$2

## Product Goal alignment
$3

## Child Stories
(tracked via linked GitHub sub-issues — see the Sub-issues panel on this issue)

## Notes
${4:-}
EOF
}

# =========================================================================
echo -e "${BLUE}=== Epic B: Curation Data Model & Ingestion Pipeline ===${NC}"
EPIC_B=$(new_epic "Curation Data Model & Ingestion Pipeline" "P0-critical,backend,ingestion,curation" "$(epic_body \
  "Stand up the raw_deals/curated_experiences schema and the ingestion pipeline (automated + manual) that feeds raw_deals, with the PENDING/REJECTED/PROMOTED state machine enforced." \
  "This is the airlock itself — REQUIREMENTS.md's core pitch collapses without it." \
  "Direct prerequisite to the curation pipeline — docs/process/PRODUCT_GOAL.md.")")
set_fields "https://github.com/$REPO/issues/$EPIC_B" "Backlog" "P0" "" ""
echo -e "${GREEN}✓ Epic B: #$EPIC_B${NC}"

STORY_B1=$(new_story "As a curator, I want raw candidate deals staged with a clear review status, so that nothing reaches the public site unreviewed" \
  "P0-critical,backend,ingestion,curation" "Sprint 1" "5" "M" "P0" \
  "$(story_body "curator" "raw candidate deals staged in one place with a clear review status" "nothing reaches the public site unreviewed" \
    "Given an ingestion job runs, when it finds a new listing, then it inserts a row into raw_deals with status PENDING" \
    "Given a curator adds a listing manually, when saved, then it also lands in raw_deals as PENDING" \
    "Given the same source_ref is re-ingested, when it already exists, then the existing row is updated, not duplicated" \
    "5" "Sprint 1" "$EPIC_B" "")")
set_fields "https://github.com/$REPO/issues/$STORY_B1" "Backlog" "P0" "M" "5"
link_parent "$STORY_B1" "$EPIC_B"
echo -e "${GREEN}✓ Story B1: #$STORY_B1${NC}"

TASK_B1_1=$(new_task "raw_deals table + Flyway migration + JPA entity + repository test" "backend,curation" "Sprint 1" "3" \
  "$(task_body "Create the raw_deals table (ARCHITECTURE.md §2.1): Flyway migration, JPA entity, Spring Data repository, and a repository test proving the PENDING/REJECTED/PROMOTED state machine." "$STORY_B1" \
    "Migration applies cleanly on an empty database" "Repository test covers insert + status transition" "3" "")")
set_fields "https://github.com/$REPO/issues/$TASK_B1_1" "Backlog" "" "" "3"
link_parent "$TASK_B1_1" "$STORY_B1"

TASK_B1_2=$(new_task "curated_experiences table + Flyway migration + JPA entity + repository test" "backend,curation" "Sprint 1" "3" \
  "$(task_body "Create the curated_experiences table (ARCHITECTURE.md §2.2): Flyway migration, JPA entity, Spring Data repository, and a repository test." "$STORY_B1" \
    "Migration applies cleanly on an empty database" "Repository test covers a basic insert + slug uniqueness constraint" "3" "")")
set_fields "https://github.com/$REPO/issues/$TASK_B1_2" "Backlog" "" "" "3"
link_parent "$TASK_B1_2" "$STORY_B1"

TASK_B1_3=$(new_task "RawDealSource adapter interface + one manual/seed adapter" "backend,ingestion" "Sprint 1" "3" \
  "$(task_body "Define the RawDealSource adapter interface (ARCHITECTURE.md §3) and implement one concrete adapter (a manual/seed source) proving the interface shape before any real external feed is wired." "$STORY_B1" \
    "Interface compiles and is used by at least one adapter" "Adding a second adapter later requires no scheduler changes" "3" "")")
set_fields "https://github.com/$REPO/issues/$TASK_B1_3" "Backlog" "" "" "3"
link_parent "$TASK_B1_3" "$STORY_B1"

TASK_B1_4=$(new_task "@Scheduled ingestion job wiring + dedup-by-source_ref logic" "backend,ingestion" "Sprint 1" "4" \
  "$(task_body "Wire the @Scheduled ingestion job that runs configured RawDealSource adapters and dedups by source_ref (update existing row, don't duplicate) per FR-1.2." "$STORY_B1" \
    "Running the same adapter twice does not create duplicate raw_deals rows" "A disabled/misconfigured adapter logs and skips rather than crashing the job" "4" "")")
set_fields "https://github.com/$REPO/issues/$TASK_B1_4" "Backlog" "" "" "4"
link_parent "$TASK_B1_4" "$STORY_B1"
echo -e "${GREEN}✓ Epic B fully wired (1 story, 4 tasks)${NC}"

# =========================================================================
echo -e "${BLUE}=== Epic C: Curator Admin Portal ===${NC}"
EPIC_C=$(new_epic "Curator Admin Portal" "P0-critical,backend,frontend,curation,admin-portal" "$(epic_body \
  "Build the single-card review UI and its backing API — approve/reject one PENDING deal at a time (FR-3.1-3.6), plus the editorial tools (title/description rewrite, image, tagging) on the approval screen." \
  "The whole product thesis is 'fast to curate' (NFR-6) — this Epic is where that either holds up or doesn't." \
  "Direct prerequisite — docs/process/PRODUCT_GOAL.md.")")
set_fields "https://github.com/$REPO/issues/$EPIC_C" "Backlog" "P0" "" ""
echo -e "${GREEN}✓ Epic C: #$EPIC_C${NC}"

STORY_C1=$(new_story "As a curator, I want to review one pending deal at a time and approve or reject it, so that curating is fast enough to sustain daily" \
  "P0-critical,backend,frontend,curation,admin-portal" "Sprint 1" "8" "XL" "P0" \
  "$(story_body "curator" "to review one pending deal at a time and approve or reject it" "curating is fast enough to sustain daily" \
    "Given I am authenticated as CURATOR, when I open the admin dashboard, then I see exactly one PENDING deal to review" \
    "Given I click Approve with required fields filled, then the deal is upserted into curated_experiences and disappears from the queue" \
    "Given I click Reject, then the deal's status becomes REJECTED and the next PENDING deal appears" \
    "8" "Sprint 1" "$EPIC_C" "")")
set_fields "https://github.com/$REPO/issues/$STORY_C1" "Backlog" "P0" "XL" "8"
link_parent "$STORY_C1" "$EPIC_C"
echo -e "${GREEN}✓ Story C1: #$STORY_C1${NC}"

TASK_C1_1=$(new_task "GET /api/admin/pending-deals + POST /api/admin/deals/{id}/reject" "backend,curation" "Sprint 1" "3" \
  "$(task_body "Implement the pending-queue read endpoint and the reject endpoint (ARCHITECTURE.md §3)." "$STORY_C1" \
    "GET returns the next unreviewed PENDING deal (or 204 if none)" "POST reject sets status=REJECTED with an optional reason" "3" "")")
set_fields "https://github.com/$REPO/issues/$TASK_C1_1" "Backlog" "" "" "3"; link_parent "$TASK_C1_1" "$STORY_C1"

TASK_C1_2=$(new_task "POST /api/admin/deals/{id}/promote — validated transform + upsert" "backend,curation" "Sprint 1" "5" \
  "$(task_body "Implement the promote endpoint: transforms curator-edited fields into curated_experiences, enforcing the FR-3.5 validation invariant (image + verified location + non-empty description) server-side, and upserts keyed by raw_deal_id (FR-3.4)." "$STORY_C1" \
    "Promoting without a hero image, location, or description is rejected with a 400, not silently accepted" "Re-promoting the same raw_deals row updates the existing curated_experiences row, not a duplicate" "5" "This endpoint is the single most important quality gate in the whole system — see NFR-3.")")
set_fields "https://github.com/$REPO/issues/$TASK_C1_2" "Backlog" "" "" "5"; link_parent "$TASK_C1_2" "$STORY_C1"

TASK_C1_3=$(new_task "Spring Security JWT auth — CURATOR/ADMIN roles" "backend" "Sprint 1" "4" \
  "$(task_body "Wire Spring Security with JWT auth gating every /api/admin/** endpoint to the CURATOR or ADMIN role (FR-5.1)." "$STORY_C1" \
    "An unauthenticated request to any /api/admin/** endpoint returns 401" "A valid CURATOR-role JWT can reach every admin endpoint" "4" "")")
set_fields "https://github.com/$REPO/issues/$TASK_C1_3" "Backlog" "" "" "4"; link_parent "$TASK_C1_3" "$STORY_C1"

TASK_C1_4=$(new_task "Next.js admin single-card review UI (approve/reject actions)" "frontend,admin-portal" "Sprint 1" "5" \
  "$(task_body "Build the (admin) route group's single-card review screen (FR-3.1): shows one pending deal, Approve and Reject actions, calls the Sprint 1 backend endpoints." "$STORY_C1" \
    "Approving advances to the next pending deal without a full page reload" "Rejecting advances to the next pending deal" "5" "")")
set_fields "https://github.com/$REPO/issues/$TASK_C1_4" "Backlog" "" "" "5"; link_parent "$TASK_C1_4" "$STORY_C1"
echo -e "${GREEN}✓ Story C1 fully wired (4 tasks)${NC}"

STORY_C2=$(new_story "As a curator, I want editorial tools on the approval screen, so that I can turn a boring raw listing into an engaging published one" \
  "P1-high,backend,frontend,taxonomy,admin-portal" "" "5" "M" "P1" \
  "$(story_body "curator" "editorial tools on the approval screen" "I can turn a boring raw listing into an engaging published one" \
    "Given I am reviewing a deal, when I rewrite the title/description, then those are what gets promoted, not the raw feed text" \
    "Given I assign one or more tags, when I promote, then the curated_experiences row carries those tags" \
    "" "5" "" "$EPIC_C" "Not yet scheduled into a Sprint — pulled in once Story #$STORY_C1 is done.")")
set_fields "https://github.com/$REPO/issues/$STORY_C2" "Backlog" "P1" "M" "5"
link_parent "$STORY_C2" "$EPIC_C"
echo -e "${GREEN}✓ Story C2: #$STORY_C2${NC}"

TASK_C2_1=$(new_task "Taxonomy (tags) data model + GET/POST /api/admin/tags" "backend,taxonomy" "" "3" \
  "$(task_body "Implement the tag taxonomy data model and admin CRUD-lite endpoints (create/rename/retire, FR-5.2)." "$STORY_C2" \
    "A curator can create a new tag and assign it to a deal at promote time" "Retiring a tag doesn't break existing curated_experiences rows that reference it" "3" "")")
set_fields "https://github.com/$REPO/issues/$TASK_C2_1" "Backlog" "" "" "3"; link_parent "$TASK_C2_1" "$STORY_C2"

TASK_C2_2=$(new_task "Editorial approval form — title/description rewrite, image upload, tag picker" "frontend,admin-portal" "" "5" \
  "$(task_body "Extend the review screen's Approve flow into a full editorial form: rewrite title/description, upload/choose a hero image, pick tags." "$STORY_C2" \
    "Every field required by FR-3.5 is editable before promoting" "Tag picker reflects the taxonomy from Task #$TASK_C2_1" "5" "")")
set_fields "https://github.com/$REPO/issues/$TASK_C2_2" "Backlog" "" "" "5"; link_parent "$TASK_C2_2" "$STORY_C2"
echo -e "${GREEN}✓ Epic C fully wired (2 stories, 6 tasks)${NC}"

# =========================================================================
echo -e "${BLUE}=== Epic D: Public Marketplace Site ===${NC}"
EPIC_D=$(new_epic "Public Marketplace Site" "P0-critical,backend,frontend,catalog" "$(epic_body \
  "The consumer-facing site: browse curated experiences by tag/city (FR-4.1), fast SSG/ISR detail pages (FR-4.2), and the Book Now call-to-action (FR-4.3)." \
  "This is the actual product a visitor sees — no acquisition (SEO, NFR-2) or conversion (Book Now) happens without it." \
  "Direct prerequisite — docs/process/PRODUCT_GOAL.md.")")
set_fields "https://github.com/$REPO/issues/$EPIC_D" "Backlog" "P0" "" ""
echo -e "${GREEN}✓ Epic D: #$EPIC_D${NC}"

STORY_D1=$(new_story "As a visitor, I want to browse curated experiences by tag and city with fast-loading pages, so that I can find something good without wading through junk" \
  "P0-critical,backend,frontend,catalog" "" "8" "L" "P0" \
  "$(story_body "visitor" "to browse curated experiences by tag and city with fast-loading pages" "I can find something good without wading through junk" \
    "Given I filter by a tag, when the page loads, then I see only published curated_experiences with that tag" \
    "Given a listing's detail page, when I load it, then it's statically generated (SSG/ISR), not rendered fresh per request" \
    "Given a curator just promoted a new listing, when I visit its page within seconds, then it's already live (on-demand revalidation)" \
    "8" "" "$EPIC_D" "Not yet scheduled — planned for the Sprint after Epic C ships (Sprint Planning pulls this in per docs/process/SCRUM_EVENTS.md).")")
set_fields "https://github.com/$REPO/issues/$STORY_D1" "Backlog" "P0" "L" "8"
link_parent "$STORY_D1" "$EPIC_D"
echo -e "${GREEN}✓ Story D1: #$STORY_D1${NC}"

TASK_D1_1=$(new_task "GET /api/experiences (filterable, paginated)" "backend,catalog" "" "3" \
  "$(task_body "Implement the public read endpoint: list curated_experiences, filterable by tag/city, paginated." "$STORY_D1" \
    "Filtering by an unknown tag returns an empty list, not an error" "Only is_published=true rows are ever returned" "3" "")")
set_fields "https://github.com/$REPO/issues/$TASK_D1_1" "Backlog" "" "" "3"; link_parent "$TASK_D1_1" "$STORY_D1"

TASK_D1_2=$(new_task "Next.js SSG/ISR listing + detail page templates, on-demand revalidation hook" "frontend,catalog" "" "6" \
  "$(task_body "Build the (public) route group's listing and detail page templates (SSG/ISR), and the on-demand-revalidation webhook the promote endpoint calls (ARCHITECTURE.md §4)." "$STORY_D1" \
    "A fresh Vercel build serves listing + detail pages with no client-side data fetch needed for first paint" "Promoting a deal triggers revalidation of its detail page within seconds" "6" "")")
set_fields "https://github.com/$REPO/issues/$TASK_D1_2" "Backlog" "" "" "6"; link_parent "$TASK_D1_2" "$STORY_D1"

TASK_D1_3=$(new_task "SEO metadata — OpenGraph, sitemap.xml, JSON-LD structured data" "frontend" "" "3" \
  "$(task_body "Add OpenGraph tags, a generated sitemap.xml, and JSON-LD structured data to every public listing page (NFR-2)." "$STORY_D1" \
    "Sitemap includes every published listing" "A listing page validates against Google's Rich Results structured-data test" "3" "")")
set_fields "https://github.com/$REPO/issues/$TASK_D1_3" "Backlog" "" "" "3"; link_parent "$TASK_D1_3" "$STORY_D1"
echo -e "${GREEN}✓ Story D1 fully wired (3 tasks)${NC}"

STORY_D2=$(new_story "As a visitor, I want a clear Book Now action, so that I can act on a listing immediately" \
  "P1-high,backend,frontend,catalog" "" "3" "S" "P1" \
  "$(story_body "visitor" "a clear Book Now action on every listing" "I can act on a listing immediately" \
    "Given a listing has an affiliate_url, when I click Book Now, then the click is tracked and I land on the affiliate link" \
    "Given a listing has no affiliate link, when I click Book Now, then I see the booking_contact fallback instead" \
    "" "3" "" "$EPIC_D" "")")
set_fields "https://github.com/$REPO/issues/$STORY_D2" "Backlog" "P1" "S" "3"
link_parent "$STORY_D2" "$EPIC_D"
echo -e "${GREEN}✓ Story D2: #$STORY_D2${NC}"

TASK_D2_1=$(new_task "POST /api/track/click/{id} + redirect to affiliate/booking contact" "backend,catalog" "" "3" \
  "$(task_body "Implement the click-tracking-then-redirect endpoint backing the Book Now button (FR-6.1)." "$STORY_D2" \
    "A click is recorded (listing id + timestamp) before redirecting" "Missing affiliate_url falls back to booking_contact, never a dead link" "3" "")")
set_fields "https://github.com/$REPO/issues/$TASK_D2_1" "Backlog" "" "" "3"; link_parent "$TASK_D2_1" "$STORY_D2"
echo -e "${GREEN}✓ Epic D fully wired (2 stories, 4 tasks)${NC}"

# =========================================================================
echo -e "${BLUE}=== Epic E: Community Sourcing ===${NC}"
EPIC_E=$(new_epic "Community Sourcing" "P1-high,backend,frontend,submission" "$(epic_body \
  "A public 'Submit a Local Gem' form so visitors can nominate listings the automated feeds and manual curator sourcing miss (FR-2.2/2.3)." \
  "Automation and manual hunting both have blind spots; this is the third sourcing channel the original pitch called for." \
  "Supports the Product Goal's curation-quality bar by widening the sourcing funnel.")")
set_fields "https://github.com/$REPO/issues/$EPIC_E" "Backlog" "P1" "" ""
echo -e "${GREEN}✓ Epic E: #$EPIC_E${NC}"

STORY_E1=$(new_story "As a visitor, I want to submit a Local Gem I know about, so that echtgut can discover businesses its own feeds miss" \
  "P1-high,backend,frontend,submission" "" "5" "M" "P1" \
  "$(story_body "visitor" "to submit a Local Gem I know about" "echtgut can discover businesses its own feeds miss" \
    "Given I fill in the submission form and submit, then a new raw_deals row is created with status PENDING and source COMMUNITY" \
    "Given I submit more than the rate limit allows, when I try again, then I'm rejected, not silently queued" \
    "" "5" "" "$EPIC_E" "")")
set_fields "https://github.com/$REPO/issues/$STORY_E1" "Backlog" "P1" "M" "5"
link_parent "$STORY_E1" "$EPIC_E"
echo -e "${GREEN}✓ Story E1: #$STORY_E1${NC}"

TASK_E1_1=$(new_task "Public submission form (Next.js)" "frontend,submission" "" "3" \
  "$(task_body "Build the public 'Submit a Local Gem' form on the (public) site (FR-2.2)." "$STORY_E1" \
    "Form validates required fields client-side before submit" "A successful submission shows clear confirmation, not just a redirect" "3" "")")
set_fields "https://github.com/$REPO/issues/$TASK_E1_1" "Backlog" "" "" "3"; link_parent "$TASK_E1_1" "$STORY_E1"

TASK_E1_2=$(new_task "POST /api/submissions — writes raw_deals, rate-limited + captcha" "backend,submission" "" "4" \
  "$(task_body "Implement the submission endpoint: writes a PENDING/COMMUNITY raw_deals row, enforces rate limiting and a captcha/equivalent check (FR-2.3)." "$STORY_E1" \
    "A captcha failure never reaches the database write" "Rate limit is enforced server-side, not just in the frontend form" "4" "")")
set_fields "https://github.com/$REPO/issues/$TASK_E1_2" "Backlog" "" "" "4"; link_parent "$TASK_E1_2" "$STORY_E1"
echo -e "${GREEN}✓ Epic E fully wired (1 story, 2 tasks)${NC}"

# =========================================================================
echo -e "${BLUE}=== Epic F: Legal, Privacy & Launch Readiness ===${NC}"
EPIC_F=$(new_epic "Legal, Privacy & Launch Readiness" "P0-critical,frontend,backend" "$(epic_body \
  "GDPR-required consent/privacy posture (cookie banner, privacy policy, a lawful basis for click tracking) before any real traffic reaches the site (NFR-4)." \
  "Not in the original pitch — added because echtgut is a Germany-facing consumer site handling visitor data (submissions, click tracking); this is a launch blocker, not later polish." \
  "Supports the Product Goal by making the site legally launchable, not just technically complete.")")
set_fields "https://github.com/$REPO/issues/$EPIC_F" "Backlog" "P0" "" ""
echo -e "${GREEN}✓ Epic F: #$EPIC_F${NC}"

STORY_F1=$(new_story "As the business owner, I want the site GDPR-compliant before real traffic, so that launch isn't blocked or penalized later" \
  "P0-critical,frontend,backend" "" "5" "M" "P0" \
  "$(story_body "business owner" "the site GDPR-compliant before real traffic" "launch isn't blocked or penalized later" \
    "Given a first-time visitor, when they land on the site, then they see a cookie/consent banner before any tracking script fires" \
    "Given a visitor wants to know how their data (submission, click) is used, when they read the privacy policy, then it accurately describes real behavior, not boilerplate" \
    "" "5" "" "$EPIC_F" "Blocks Epic D's click-tracking (Story #$STORY_D2) and Epic E's submissions (Story #$STORY_E1) from going live to real traffic — see NFR-4.")")
set_fields "https://github.com/$REPO/issues/$STORY_F1" "Backlog" "P0" "M" "5"
link_parent "$STORY_F1" "$EPIC_F"
echo -e "${GREEN}✓ Story F1: #$STORY_F1${NC}"

TASK_F1_1=$(new_task "Cookie/consent banner + privacy policy page" "frontend" "" "4" \
  "$(task_body "Add a cookie/consent banner (blocking non-essential scripts until accepted) and a real privacy policy page to the public site." "$STORY_F1" \
    "No tracking script fires before consent is given" "Privacy policy page describes the actual data flows in this architecture, not a generic template" "4" "")")
set_fields "https://github.com/$REPO/issues/$TASK_F1_1" "Backlog" "" "" "4"; link_parent "$TASK_F1_1" "$STORY_F1"

TASK_F1_2=$(new_task "Data processing review — click tracking + community submission contact info" "backend" "" "3" \
  "$(task_body "Review click-tracking (Task #$TASK_D2_1) and community submissions (Task #$TASK_E1_2) for retention limits and an erasure path for any personal data captured (submitter contact info)." "$STORY_F1" \
    "A documented retention period exists for click-tracking data" "A submitter can request their submission's contact info be deleted" "3" "")")
set_fields "https://github.com/$REPO/issues/$TASK_F1_2" "Backlog" "" "" "3"; link_parent "$TASK_F1_2" "$STORY_F1"
echo -e "${GREEN}✓ Epic F fully wired (1 story, 2 tasks)${NC}"

echo -e "${GREEN}=================================================${NC}"
echo -e "${GREEN}✓ Backlog seeded: 6 epics, 9 stories, 24 tasks${NC}"
echo -e "${GREEN}=================================================${NC}"
