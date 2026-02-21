# Backend API Contracts (Production)

Version: **v1** (baseline). Android app expects these endpoints when `BuildConfig` server URLs are set.

---

## Base URLs (BuildConfig)

| Key | Purpose |
|-----|--------|
| `AUTH_SERVER_URL` | Auth and account (login, register, change password, 2FA, delete account) |
| `SUPPORT_SERVER_URL` | Support ticket submission |
| `SCAN_SERVER_URL` | Room scan process & finish |
| `VERIFICATION_SERVER_URL` | Purchase/subscription verification (out of P0 scope here) |

All URLs are used without trailing slash; paths are appended directly (e.g. `{AUTH_SERVER_URL}/auth/login`).

---

## 1. Auth & Account API

**Base:** `AUTH_SERVER_URL`

### 1.1 POST `/auth/login`

**Request (JSON):**
```json
{
  "login": "string",
  "password": "string"
}
```

**Response 200 (JSON):**
```json
{
  "userId": "string",
  "accessToken": "string",
  "refreshToken": "string | null"
}
```

- `accessToken` is required; missing → client treats as failure.
- Client uses `refreshToken` only if present (optional for v1).

---

### 1.2 POST `/auth/register`

**Request (JSON):** Same as login.

**Response 200 (JSON):** Same as login.

---

### 1.3 POST `/account/change-password`

**Request (JSON):**
```json
{
  "oldPassword": "string",
  "newPassword": "string"
}
```

**Response:** 200 OK with empty body or JSON. Non-2xx → client shows generic error.

**Note:** Production should require `Authorization: Bearer {accessToken}`. Android client will need to send token when implemented.

---

### 1.4 POST `/account/2fa`

**Request (JSON):**
```json
{
  "enabled": true | false
}
```

**Response:** 200 OK. Non-2xx → client keeps local 2FA state and shows backend error message.

**Note:** Production should require `Authorization: Bearer {accessToken}`.

---

### 1.5 POST `/account/delete`

**Request (JSON):** `{}` (empty body allowed).

**Response:** 200 OK. Non-2xx → client treats as failure.

**Note:** Production should require `Authorization: Bearer {accessToken}`.

---

## 2. Support API

**Base:** `SUPPORT_SERVER_URL`

### 2.1 POST `/support/tickets`

**Request (JSON):**
```json
{
  "phone": "string",
  "email": "string",
  "description": "string"
}
```

**Response:** 200 OK (body ignored). Non-2xx → Android falls back to mail client.

---

## 3. Scan Processing API

**Base:** `SCAN_SERVER_URL`

### 3.1 POST `/api/v1/scan/process`

**Content-Type:** `multipart/form-data`

| Part name | Type | Required | Description |
|-----------|------|----------|-------------|
| `project_id` | string | yes | Project ID |
| `room_id` | string | yes | Room ID |
| `scan_id` | string | yes | Scan session ID |
| `frames` | file(s) | yes (≥1) | JPEG images |
| `trajectory` | string | no | JSON trajectory data |
| `depth` | file(s) | no | Depth maps (application/octet-stream) |

**Response 200 (JSON):**
```json
{
  "scan_id": "string",
  "dimensions": {
    "wall_height_m": 2.7,
    "perimeter_m": 12.0,
    "floor_area_m2": 9.0
  },
  "coverage": {
    "percentage": 85.5
  },
  "quality_metrics": {
    "scan_quality": 0.92
  }
}
```

- Client parses `dimensions`, `coverage`, `quality_metrics`; missing fields default to 0.

---

### 3.2 POST `/api/v1/scan/finish`

**Request (JSON):**
```json
{
  "scan_id": "string",
  "project_id": "string",
  "room_id": "string"
}
```

**Response 200 (JSON):** Same shape as process response (scan_id, dimensions, coverage, quality_metrics).

---

## Error Handling (client behaviour)

- **Auth:** Empty `AUTH_SERVER_URL` → demo login (no network call). Non-2xx → `Result.failure`, UI shows generic auth error.
- **Support:** Empty or unreachable `SUPPORT_SERVER_URL` → open mail client with pre-filled subject/body.
- **Scan:** Empty `SCAN_SERVER_URL` or failure → local/placeholder dimensions, flow continues.

---

## Versioning and changes

- This document is the **v1 baseline** for production implementation.
- Backend should preserve compatibility for existing Android clients; new optional fields in responses are allowed.
- Breaking changes (e.g. new required request fields, path changes) should be introduced as a new API version and documented in a later contract revision.
