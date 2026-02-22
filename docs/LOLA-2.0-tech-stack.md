# LOLA 2.0 — Утверждённый стек (v1)

## Mobile
- Framework: **Flutter** (быстрый единый код для iOS/Android)
- State management: **Riverpod**
- Routing: **go_router**
- Local storage: **Hive** (настройки), secure storage для токенов
- Audio/STT/TTS:
  - STT: `speech_to_text` (нативные провайдеры устройства)
  - TTS: `flutter_tts` (нативные voice engines)
  - Playback: `just_audio`

## Backend
- Runtime: **Python 3.11**
- Framework: **FastAPI**
- DB: **PostgreSQL**
- ORM: **SQLAlchemy + Alembic**
- Cache/queues: **Redis**
- Object storage for audio: **S3-compatible** (например, Cloudflare R2 / MinIO)

## AI Layer
- Primary LLM: **OpenAI GPT-4.1 mini** (баланс цены/качества для MVP)
- Fallback LLM: **GPT-4o mini** (или второй провайдер при сбоях/лимитах)
- Prompt source: `prompts/lola-mvp-system-prompts.yaml`

## Infra/DevOps
- Containerization: **Docker + docker-compose**
- CI: **GitHub Actions** (lint/test/build)
- Observability:
  - Backend logs: structured JSON
  - Errors: Sentry
  - Metrics: Prometheus/Grafana (минимум на staging)

## Payments
- iOS: StoreKit (In-App Purchases Subscriptions)
- Android: Google Play Billing
- Server: webhook verification + subscription state sync

## Decision Notes
- Flutter выбран для сокращения time-to-market.
- STT/TTS на устройстве выбран для MVP, чтобы снизить стоимость и упростить privacy.
- Серверный TTS оставить как опцию этапа роста (для более стабильных голосов).
