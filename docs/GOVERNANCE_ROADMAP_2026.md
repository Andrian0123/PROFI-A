# PROFI-A Governance and Quarterly Roadmap (Canonical)

## Canonical Source of Status

This file is the single source of truth for strategic status.  
When statuses conflict with legacy reports, this document wins.

## Status Model

- `Done` — implemented and integrated in main app flow.
- `InProgress` — implementation started, not fully hardened.
- `Planned` — agreed scope, not started.
- `Blocked` — waiting for external dependency.

## Current Strategic Board

| Stream | Status | Notes |
|---|---|---|
| Product manifesto and mission baseline | Done | See `docs/FUTURE_GENERATION_MANIFESTO_V1.md` |
| Brand color canonicalization | Done | Compose/XML synced to dark olive + dark green |
| P0 backend closure (auth/account/support/scan transport) | Done | Server-first transport + fallback patterns added |
| Quality gates baseline (build/lint/i18n CI) | Done | `android-quality-gates.yml` + parity script |
| Governance + quarterly planning | Done | This document |
| Full production auth backend | Planned | Mobile transport ready; server implementation pending |
| Full support backend processing pipeline | Planned | Mobile transport ready; backend SLA/queue pending |
| Materials/work prices regional catalog | Planned | Data model and UX expansion required |
| End-to-end KPI analytics | Planned | Requires event taxonomy and telemetry backend |

## Quarterly Roadmap

## Q1 (Stabilize Foundation)

- [ ] Lock canonical brand tokens and migrate old alias usage.
- [ ] Keep build/lint/i18n gates green for all feature PRs.
- [ ] Finalize API contracts for auth/account/support with backend team.

Exit criteria:
- zero red build on main branch;
- no new hardcoded user-facing strings in touched files;
- auth/support contracts signed.

**Reference:** `docs/BACKEND_API_CONTRACTS.md`, `server/` (reference backend).

## Q2 (Close Core Product Loop)

- [ ] Production auth/login/register/change-password integration.
- [ ] Support ticket backend with delivery guarantees and status tracking.
- [ ] Room scan server processing hardened with real-device telemetry.

Exit criteria:
- successful E2E happy-path for auth/support/scan;
- fallback path verified for offline/backend outage cases.

**Reference:** `docs/SUPPORT_PIPELINE.md`, `docs/SCAN_SERVER_AND_DEVICE_TESTS.md`, `docs/E2E_TEST_CASES.md`.

## Q3 (Scale Business Value)

- [ ] Materials and work types with regional pricing profiles.
- [ ] Advanced estimate templates and pricing governance.
- [ ] Localization hardening (full translation QA cycle).

Exit criteria:
- regional pricing enabled for at least one target city profile;
- translation parity and UX review in all supported locales.

## Q4 (Operational Excellence)

- [ ] Observability and release health dashboards.
- [ ] Regression prevention through expanded E2E matrix.
- [ ] Release governance with strict quality thresholds.

Exit criteria:
- predictable release cadence;
- measurable reduction in critical regression escapes.

## Governance Rituals

- Weekly: technical triage (P0/P1 blockers).
- Bi-weekly: product + engineering roadmap sync.
- Monthly: quality gate health review.
- Quarterly: strategic reset of priorities and KPI review.

## Ownership Matrix

- Product vision/priority: Product Owner
- Architecture and quality gates: Tech Lead
- Mobile delivery (Android): Android Lead
- Backend contracts and reliability: Backend Lead
- Localization quality: PM + QA

## KPI Baseline (to track every quarter)

- Build stability: % green runs on main.
- P0 flow reliability: auth/support/scan success ratio.
- Estimate cycle speed: time from room input to export.
- Localization quality: missing-key count and critical text defects.
