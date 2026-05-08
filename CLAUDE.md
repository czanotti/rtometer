# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Purpose

Terra is a **production-grade GCP full-stack template** to be forked for future apps. Target stack: React frontend + Django REST API (JWT auth) + PostgreSQL, deployed to Google Cloud Run. Currently a single container; future evolution splits into 3.

## Agentic Workflow (read this first)

This repo follows Matt Pocock's agentic development principles. Before writing any code:

1. Pull [`ai/agentic_development.md`](ai/agentic_development.md) for the full operating protocol.
2. Run the `grill-me` skill ([`ai/skills/grill-me/SKILL.md`](ai/skills/grill-me/SKILL.md)) to reach shared design alignment.
3. Write or reference a PRD in [`ai/prds/`](ai/prds/) before creating GitHub Issues.
4. Break the PRD into vertical-slice GitHub Issues (each touches UI + API + DB).
5. Implement via TDD (Red → Green → Refactor). Never write implementation before a failing test.

## `ai/` Folder

| Path | Purpose |
|------|---------|
| [`ai/agentic_development.md`](ai/agentic_development.md) | Matt Pocock principles — smart zone, tracer bullets, deep modules, TDD |
| [`ai/skills/`](ai/skills/) | Agent skills invoked by name (e.g. `grill-me`) |
| [`ai/prds/`](ai/prds/) | PRD per Epic/Feature; each links to its GitHub Issues |

## Dev Commands

```bash
# Run unit tests (no DB required)
pytest helloapp/

# Run a single test
pytest helloapp/test_views.py::TestViews::test_home_page_response

# Run unit tests via tox
tox -e py312

# Build Docker image
tox -e docker-build

# Run integration tests against the Docker container
tox -e docker-test

# Deploy to GCP Cloud Run
tox -e deploy
```

## CI/CD

Defined in [`.github/workflows/ci.yml`](.github/workflows/ci.yml):

- **On PR to main**: unit tests → Docker build → Docker integration tests
- **On merge to main**: unit tests → Docker build → Docker integration tests → deploy to Cloud Run

Required GitHub secrets/vars: `SECRET_KEY`, `GCP_SA_KEY`, `GCP_PROJECT`, `GCP_SERVICE`, `GCP_REGIONS`.
