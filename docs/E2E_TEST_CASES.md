# E2E Test Cases (Critical Flows)

Baseline E2E scenarios that must pass for each release (see [QUALITY_GATES.md](QUALITY_GATES.md)).

## 1. Auth (Login / Register)

**Goal:** Auth screen works with backend when URL is set; demo fallback when empty.

| # | Step | Expected |
|---|------|----------|
| 1.1 | Open app, complete onboarding until Auth screen (or navigate to Auth). | Auth screen shows login/register tabs. |
| 1.2 | Enter valid credentials and submit (when `AUTH_SERVER_URL` is set). | Login/register succeeds; user proceeds to Home. |
| 1.3 | When backend is unavailable or URL empty: submit any login. | Demo fallback: user proceeds to Home (or clear error message). |
| 1.4 | Register new user (when backend available). | Registration succeeds or shows backend error. |

**Fallback rule:** If `BuildConfig.AUTH_SERVER_URL` is empty or request fails, app must not crash and must allow continuing (e.g. demo login).

---

## 2. Support (Ticket Submission)

**Goal:** Submit support request to server first; fall back to mail client if server unavailable.

| # | Step | Expected |
|---|------|----------|
| 2.1 | From Profile/Drawer open Support. | Support screen shows contact info and form (phone, email, description). |
| 2.2 | Fill required fields, tap "Send request" with `SUPPORT_SERVER_URL` reachable. | Request sent to server; user sees success (e.g. "Request sent to server"). |
| 2.3 | With server unreachable: tap "Send request". | Fallback: mail client opens with pre-filled subject/body (or toast "Mail client opened"). |
| 2.4 | On device without mail app. | Clear message (e.g. "Install email app to send"). |

**Fallback rule:** Server-first; on failure open mail client with support email and body.

---

## 3. Room Scan (Server Processing)

**Goal:** Room scan uses server when available; local/placeholder fallback when not.

| # | Step | Expected |
|---|------|----------|
| 3.1 | Create/open project, add room, open Room Scan. | Scan screen opens; camera/scan UI available. |
| 3.2 | Perform scan with `SCAN_SERVER_URL` reachable. | Scan sent to server; preliminary dimensions (e.g. wall height) from server. |
| 3.3 | With server unreachable: perform scan. | Local fallback: placeholder/default dimensions or clear "offline" state. |
| 3.4 | Mark on plan / continue flow. | No crash; user can proceed with local or server data. |

**Fallback rule:** Server success preferred; on failure use local processing or safe defaults and keep flow usable.

---

## Running and Expanding

- **Manual:** Execute the steps above on a device/emulator per release.
- **Automation:** `AuthSupportScanE2ETest` (Compose UI) covers: open Auth from Splash; open Support from drawer after demo mode. RoomScan E2E — см. docs/SCAN_SERVER_AND_DEVICE_TESTS.md (реальное устройство/камера).
- **CI:** Quality gate workflow runs build and lint; E2E execution can be added as a separate job (device/emulator).

Instrumented tests: `ExampleInstrumentedTest` (package), `AuthSupportScanE2ETest` (Auth + Support flows). Run: `./gradlew :app:connectedDebugAndroidTest`.
