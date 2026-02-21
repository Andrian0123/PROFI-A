# Quality Gates (Future-Generation Baseline)

## Purpose

Quality gates guarantee predictable releases and prevent regression drift while the product scales.

## Mandatory Gates

1. **Build gate**
   - `:app:assembleDebug` must pass.
2. **Lint gate**
   - `:app:lintDebug` must pass for new changes.
3. **Localization parity gate**
   - Default `values/strings.xml` and `values-en/strings.xml` must have identical key sets.
   - Verified by `tools/check_i18n_parity.py`.
4. **P0 flow gate**
   - Critical flows must keep success and fallback behavior:
     - auth/login/register
     - support ticket submission (server-first, email fallback)
     - room scan server processing with local fallback

## CI Automation

- Workflow: `.github/workflows/android-quality-gates.yml`
- Automated checks:
  - i18n parity check
  - Android assembleDebug
  - Android lintDebug

## Engineering Rules

- Use only semantic colors from `MaterialTheme.colorScheme`.
- No new hardcoded user-facing strings in feature screens.
- Any new backend integration must expose:
  - request contract
  - failure fallback
  - user-visible error state

## E2E Test Baseline (must be expanded)

Current minimum E2E scenarios for each release train:

1. Login/Register path (demo fallback when backend URL is empty).
2. Support submission path (backend success / mail fallback).
3. Room scan path (server success / local fallback values).

## Warnings

- **kapt:** Global `kapt.arguments` are not used so that Room-only options (e.g. `room.incremental`) are not passed to Hilt, avoiding "options not recognized" warnings. Room incremental processing can be re-enabled via Room-specific configuration if needed.

## Release Decision Rule

A release candidate is acceptable only when all mandatory gates are green.
