# P0 Backend Closure Status

## Scope

This document closes the immediate P0 backend integration gaps for:

- Auth/account transport layer
- Support endpoint transport layer
- Scan E2E handshake from Android to backend

## Implemented in Android

### 1) Auth and account API layer

- Added `AuthAccountApi` with default implementation:
  - `login`
  - `register`
  - `changePassword`
  - `setTwoFaEnabled`
  - `deleteAccount`
- Files:
  - `app/src/main/java/ru/profia/app/data/remote/AuthAccountApi.kt`
  - `app/src/main/java/ru/profia/app/data/repository/AuthAccountRepository.kt`

### 2) Support API layer

- Added `SupportApi` + `SupportRepository` for server-first support submission.
- If backend URL is empty/unavailable, `SupportScreen` falls back to mail client intent.
- Files:
  - `app/src/main/java/ru/profia/app/data/remote/SupportApi.kt`
  - `app/src/main/java/ru/profia/app/data/repository/SupportRepository.kt`
  - `app/src/main/java/ru/profia/app/ui/viewmodel/SupportViewModel.kt`
  - `app/src/main/java/ru/profia/app/ui/screens/SupportScreen.kt`

### 3) Account UX integration

- `AuthScreen` now uses backend-aware `AuthViewModel`.
- `ChangePasswordScreen` now uses backend-aware `ChangePasswordViewModel`.
- Added `TwoFaSettingsScreen` with DataStore persistence and backend sync attempt.
- Files:
  - `app/src/main/java/ru/profia/app/ui/viewmodel/AuthViewModel.kt`
  - `app/src/main/java/ru/profia/app/ui/viewmodel/ChangePasswordViewModel.kt`
  - `app/src/main/java/ru/profia/app/ui/viewmodel/TwoFaSettingsViewModel.kt`
  - `app/src/main/java/ru/profia/app/ui/screens/TwoFaSettingsScreen.kt`
  - `app/src/main/java/ru/profia/app/ui/navigation/ProfiANavHost.kt`

### 4) Scan backend handshake

- `SCAN_SERVER_URL` is used by `ScanProcessingApi`.
- Room scan flow already performs process/finish server calls and has local fallback.
- Files:
  - `app/src/main/java/ru/profia/app/data/remote/ScanProcessingApi.kt`
  - `app/src/main/java/ru/profia/app/ui/screens/RoomScanScreen.kt`

## BuildConfig keys

Configured keys in `app/build.gradle.kts`:

- `AUTH_SERVER_URL`
- `SUPPORT_SERVER_URL`
- `SCAN_SERVER_URL`
- `VERIFICATION_SERVER_URL`

## Server contract baseline

See **[BACKEND_API_CONTRACTS.md](BACKEND_API_CONTRACTS.md)** for versioned request/response contracts for production.

Summary of expected endpoints:

- `POST {AUTH_SERVER_URL}/auth/login` — JSON: login, password → userId, accessToken, refreshToken?
- `POST {AUTH_SERVER_URL}/auth/register` — same
- `POST {AUTH_SERVER_URL}/account/change-password` — JSON: oldPassword, newPassword
- `POST {AUTH_SERVER_URL}/account/2fa` — JSON: enabled
- `POST {AUTH_SERVER_URL}/account/delete` — empty or {}
- `POST {SUPPORT_SERVER_URL}/support/tickets` — JSON: phone, email, description
- `POST {SCAN_SERVER_URL}/api/v1/scan/process` — multipart (project_id, room_id, scan_id, frames, trajectory?, depth?)
- `POST {SCAN_SERVER_URL}/api/v1/scan/finish` — JSON: scan_id, project_id, room_id
