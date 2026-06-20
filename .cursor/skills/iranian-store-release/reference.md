# Iranian Store Release — Reference

## Environment variables

Add to project `.env` (loaded by Secrets Gradle Plugin for billing keys; upload scripts read via `os.environ`):

```bash
# Package name (default: ir.m4tinbeigi.taskreminder)
APP_PACKAGE_NAME=ir.m4tinbeigi.taskreminder

# Myket Partner API — from developer panel → in-app products → verification token
MYKET_ACCESS_TOKEN=your-x-access-token

# Cafe Bazaar Pishkhan API — from developer panel → API settings
CAFEBAZAAR_PISHKHAN_API_SECRET=your-api-secret

# In-app billing public keys (Gradle / BuildConfig)
MYKET_IAB_PUBLIC_KEY=base64-public-key-from-myket-panel
BAZAAR_RSA_KEY=rsa-key-from-bazaar-panel

# Release signing (already used by build.gradle.kts)
RELEASE_KEY_FILE=my-upload-key.jks
RELEASE_STORE_PASSWORD=
RELEASE_KEY_ALIAS=upload
RELEASE_KEY_PASSWORD=
```

## Myket Partner API

Base: `https://developer.myket.ir/api/partners/applications/{packageName}`

| Step | Method | Endpoint |
|------|--------|----------|
| Create/edit bundle | PUT | `/release-bundle` |
| Upload APK | PUT | `/release-bundle/upload` (multipart) |
| Commit for review | POST | `/release-bundle/commit` |
| List bundles | GET | `/release-bundle?status={status}` |
| Delete draft | DELETE | `/release-bundle` |
| Revert review | DELETE | `/release-bundle/revert` |

Header: `X-Access-Token: {MYKET_ACCESS_TOKEN}`

Commit body:

```json
{
  "isManualPublish": false,
  "message": "Release notes for admin"
}
```

Status values: `JustCreated`, `WaitingForApproval`, `Rejected`, `Approved`, `RolledBack`.

Docs: https://myket.ir/kb/pages/developer-cd/

## Cafe Bazaar Pishkhan API v1

Base: `https://api.pishkhan.cafebazaar.ir/v1`

Header: `CAFEBAZAAR-PISHKHAN-API-SECRET: {secret}`

| Step | Method | Endpoint |
|------|--------|----------|
| Check uncommitted | GET | `/apps/releases/last-uncommitted` |
| Create release | POST | `/apps/releases/` |
| Upload APK/AAB | POST | `/apps/releases/upload/` (multipart: `apk`, `architecture=0`) |
| Commit | POST | `/apps/releases/commit/` |

Commit body:

```json
{
  "auto_publish": true,
  "changelog_fa": "...",
  "changelog_en": "...",
  "developer_note": "...",
  "staged_rollout_percentage": 100
}
```

For AAB: sign offline with [bundle-signer](https://github.com/cafebazaar/bundle-signer) before upload.

## In-app billing

### Myket (`myket` flavor)

- SDK: `com.github.myketstore:myket-billing-client:1.6` (JitPack)
- Permission: `ir.mservices.market.BILLING`
- Public key in `BuildConfig.IAB_PUBLIC_KEY`
- Docs: https://myket.ir/kb/pages/java/

### Cafe Bazaar (`bazaar` flavor)

- SDK: `com.github.cafebazaar.Poolakey:poolakey:2.2.0` (JitPack)
- Permission: `com.farsitel.bazaar.permission.PAY_THROUGH_BAZAAR`
- RSA key in `BuildConfig.IAB_PUBLIC_KEY`
- Docs: https://github.com/cafebazaar/Poolakey

### Abstraction pattern

```kotlin
interface BillingProvider {
  suspend fun connect(): Result<Unit>
  suspend fun purchase(productId: String): Result<Purchase>
  suspend fun queryPurchases(): Result<List<Purchase>>
  fun disconnect()
}

// Wire in DI/ViewModel using BuildConfig.STORE_TYPE
```

Product IDs must match SKUs created in **each** store's developer panel separately.

## Rejection triage

| Symptom / reason | Likely fix |
|------------------|------------|
| `versionCode` already exists / lower than published | Run `./scripts/bump-version.sh` |
| Wrong signing key | Verify keystore matches store registration |
| Target SDK too low | Raise `targetSdk` in `build.gradle.kts` (Myket requires ≥32) |
| Missing permission justification | Add `developer_note` / admin message explaining why |
| Billing permission without IAP | Implement billing or remove unused billing permission |
| Wrong store SDK in APK | Rebuild correct flavor; verify `STORE_TYPE` |
| Privacy policy / data safety | Add URL in manifest or store listing |
| Crashed on review device | Fix crash; test release APK on device before upload |
| `EditNotPossible` (Myket) | Publish or cancel pending release in panel first |
| `ReleaseNotFound` (Myket) | Call PUT `/release-bundle` before upload |
| Uncommitted release (Bazaar) | Upload to existing draft or delete via panel |

After fix: bump version → rebuild affected flavor(s) → re-upload.

## Build failures

| Error | Fix |
|-------|-----|
| JitPack resolve failed | Add `maven { url = uri("https://jitpack.io") }` to `settings.gradle.kts` |
| Signing config null | Set `RELEASE_*` env vars or place `my-upload-key.jks` |
| Duplicate class (both billing SDKs) | Use flavor-specific `myketImplementation` / `bazaarImplementation` |
| Manifest merger billing conflict | Use `manifestPlaceholders` per flavor |
| ProGuard (if enabled) | Keep billing classes — see store docs |

Build tasks:

```bash
./gradlew :app:assembleMyketRelease :app:assembleBazaarRelease
```

## Architecture note

Both flavors share the same `applicationId` (`ir.m4tinbeigi.taskreminder`). Store-specific code differs only in billing SDK and manifest placeholders — do **not** change `applicationId` per flavor unless the user explicitly wants separate listings.
