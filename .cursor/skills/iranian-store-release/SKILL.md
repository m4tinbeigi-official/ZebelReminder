---
name: iranian-store-release
description: Builds separate Myket and Cafe Bazaar release APKs with store-specific in-app billing, uploads new versions via store APIs, triages rejections, fixes issues, and retries until both stores accept the release. Use when the user mentions Myket, Cafe Bazaar, کافه بازار, مایکت, Iranian app store release, store upload, or dual-store billing builds.
---

# Iranian Store Release (Myket + Cafe Bazaar)

Automate dual-store Android releases for this project: two signed APKs (one per store), store-native billing, upload, rejection handling, and retry.

## Prerequisites

Before first release, confirm:

- [ ] `version.properties` exists at repo root
- [ ] Release keystore configured (`RELEASE_KEY_FILE`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`)
- [ ] Store credentials in `.env` (see [reference.md](reference.md#environment-variables))
- [ ] Product flavors `myket` and `bazaar` exist (see Setup below if missing)
- [ ] Changelog files exist: `release-notes/CHANGELOG_FA.txt`, `release-notes/CHANGELOG_EN.txt`

## End-to-end workflow

Copy and track progress:

```
Release Progress:
- [ ] Phase 0: Verify billing flavors + credentials
- [ ] Phase 1: Bump version (if new release)
- [ ] Phase 2: Build both store APKs
- [ ] Phase 3: Upload to Myket
- [ ] Phase 4: Upload to Cafe Bazaar
- [ ] Phase 5: Monitor / triage rejections
- [ ] Phase 6: Fix, rebuild, re-upload (loop until both accepted)
- [ ] Phase 7: Deliver artifacts + summary
```

### Phase 0 — Verify setup

1. Read `app/build.gradle.kts` and confirm `productFlavors { myket { ... } bazaar { ... } }` with `flavorDimensions += "store"`.
2. Confirm billing abstraction exists (`BillingProvider` or equivalent) wired per flavor.
3. Load `.env` — never commit secrets. If credentials missing, stop and ask the user.

### Phase 1 — Bump version

Only bump when publishing a **new** version (not when retrying the same version after a fix):

```bash
./scripts/bump-version.sh
```

Record `versionCode` and `versionName` from output.

### Phase 2 — Build both store APKs

```bash
bash .cursor/skills/iranian-store-release/scripts/build-store-variants.sh
```

Expected outputs:

| Store | APK path |
|-------|----------|
| Myket | `app/build/outputs/apk/myket/release/app-myket-release.apk` |
| Cafe Bazaar | `app/build/outputs/apk/bazaar/release/app-bazaar-release.apk` |

If build fails, fix Gradle/billing/manifest issues before upload. Common fixes in [reference.md](reference.md#build-failures).

### Phase 3 — Upload to Myket

```bash
python3 .cursor/skills/iranian-store-release/scripts/upload_myket.py \
  --apk app/build/outputs/apk/myket/release/app-myket-release.apk \
  --changelog-fa release-notes/CHANGELOG_FA.txt \
  --changelog-en release-notes/CHANGELOG_EN.txt
```

Uses official Partner API (`X-Access-Token`). Flow: create bundle metadata → upload APK → commit for review.

### Phase 4 — Upload to Cafe Bazaar

```bash
python3 .cursor/skills/iranian-store-release/scripts/upload_cafebazaar.py \
  --apk app/build/outputs/apk/bazaar/release/app-bazaar-release.apk \
  --changelog-fa release-notes/CHANGELOG_FA.txt \
  --changelog-en release-notes/CHANGELOG_EN.txt
```

Uses Pishkhan API v1 (`CAFEBAZAAR-PISHKHAN-API-SECRET`). Flow: create release → upload APK → commit.

### Phase 5 — Monitor rejections

Poll store status:

```bash
# Myket — list bundles by status
python3 .cursor/skills/iranian-store-release/scripts/upload_myket.py --status Rejected
python3 .cursor/skills/iranian-store-release/scripts/upload_myket.py --status WaitingForApproval

# Cafe Bazaar — check last uncommitted / recent releases
python3 .cursor/skills/iranian-store-release/scripts/upload_cafebazaar.py --check-status
```

Also check developer panel tickets/messages if API status is unclear.

### Phase 6 — Fix → rebuild → re-upload loop

When either store rejects:

1. **Parse rejection reason** from API response, panel message, or email.
2. **Map to fix category** using [reference.md](reference.md#rejection-triage).
3. **Apply minimal fix** in code/manifest/permissions/changelog.
4. **Bump version** (`./scripts/bump-version.sh`) — stores require higher `versionCode` on resubmit.
5. **Rebuild** only the affected flavor if rejection is store-specific; rebuild both if shared code changed.
6. **Re-upload** to the rejected store(s).
7. Repeat until both show `WaitingForApproval` or `Approved`.

Do not stop after first upload unless the user explicitly asked for upload-only.

### Phase 7 — Deliver artifacts

Report to the user:

```markdown
## Store Release Summary

| Store | Version | APK | Upload status |
|-------|---------|-----|---------------|
| Myket | {versionName} ({versionCode}) | `app/.../app-myket-release.apk` | {status} |
| Cafe Bazaar | {versionName} ({versionCode}) | `app/.../app-bazaar-release.apk` | {status} |

### Changelog (FA)
{summary}

### Fixes applied this cycle
- {fix 1}
```

Copy both APKs to a dated folder if the user wants deliverables:

```bash
DIST="dist/release-$(date +%Y%m%d)-v${versionName}"
mkdir -p "$DIST"
cp app/build/outputs/apk/myket/release/app-myket-release.apk "$DIST/zebel-myket-v${versionName}.apk"
cp app/build/outputs/apk/bazaar/release/app-bazaar-release.apk "$DIST/zebel-bazaar-v${versionName}.apk"
```

## One-command orchestrator

For full pipeline (build + upload both):

```bash
bash .cursor/skills/iranian-store-release/scripts/release_stores.sh
```

Options: `--skip-bump`, `--myket-only`, `--bazaar-only`, `--build-only`.

## Setup (first time only)

If product flavors or billing are missing, implement before release:

### 1. Gradle — two store flavors

In `settings.gradle.kts`, add JitPack:

```kotlin
maven { url = uri("https://jitpack.io") }
```

In `gradle/libs.versions.toml`, add:

```toml
poolakey = "2.2.0"
myketBilling = "1.6"
```

In `app/build.gradle.kts`:

```kotlin
flavorDimensions += "store"

productFlavors {
  create("myket") {
    dimension = "store"
    manifestPlaceholders["marketApplicationId"] = "ir.mservices.market"
    manifestPlaceholders["marketBindAddress"] = "ir.mservices.market.InAppBillingService.BIND"
    manifestPlaceholders["marketPermission"] = "ir.mservices.market.BILLING"
    buildConfigField("String", "STORE_TYPE", "\"MYKET\"")
    buildConfigField("String", "IAB_PUBLIC_KEY", "\"${project.findProperty("MYKET_IAB_PUBLIC_KEY") ?: ""}\"")
  }
  create("bazaar") {
    dimension = "store"
    manifestPlaceholders["marketApplicationId"] = "com.farsitel.bazaar"
    manifestPlaceholders["marketBindAddress"] = "com.farsitel.bazaar.InAppBillingService.BIND"
    manifestPlaceholders["marketPermission"] = "com.farsitel.bazaar.permission.PAY_THROUGH_BAZAAR"
    buildConfigField("String", "STORE_TYPE", "\"BAZAAR\"")
    buildConfigField("String", "IAB_PUBLIC_KEY", "\"${project.findProperty("BAZAAR_RSA_KEY") ?: ""}\"")
  }
}
```

Dependencies per flavor:

```kotlin
"myketImplementation"(libs.myket.billing.client)
"bazaarImplementation"(libs.poolakey)
```

### 2. Billing abstraction

Create `BillingProvider` interface + `MyketBillingProvider` / `BazaarBillingProvider`. Inject via `STORE_TYPE` BuildConfig. Never ship both SDKs in one APK.

Details: [reference.md](reference.md#in-app-billing).

### 3. Environment variables

Add to `.env` (from developer panels). Template in [reference.md](reference.md#environment-variables).

### 4. Release notes

Create `release-notes/CHANGELOG_FA.txt` and `release-notes/CHANGELOG_EN.txt` before each release.

## Rules

- **Two APKs always** — never upload the Myket APK to Bazaar or vice versa.
- **Never commit** `.env`, keystore, or API tokens.
- **Server-side only** for Myket `X-Access-Token` — scripts run locally/CI, not in the app.
- **Bump versionCode** on every resubmit after rejection.
- **Minimal fixes** — address rejection reason only; don't refactor unrelated code.
- **Persian changelog** required for both stores.

## Additional resources

- API details, rejection triage, billing setup: [reference.md](reference.md)
