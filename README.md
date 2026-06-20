<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Run and deploy your AI Studio app

This contains everything you need to run your app locally.

View your app in AI Studio: https://ai.studio/apps/c30bdb49-7acc-40f3-85d9-8602a22a066e

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for an example)
5. Run the app on an emulator or physical device

## Release build

Release artifacts must be signed with a real upload/release keystore when building
locally with signing env vars configured:

```bash
export RELEASE_KEY_FILE=/absolute/path/to/upload-keystore.jks
export RELEASE_STORE_PASSWORD=your_store_password
export RELEASE_KEY_ALIAS=your_key_alias
export RELEASE_KEY_PASSWORD=your_key_password
./gradlew assembleRelease bundleRelease
```

Without signing env vars, `./gradlew assembleRelease` produces an unsigned APK.

## Automatic release pipeline

Every push to `main` triggers GitHub Actions to:

1. Bump `version.properties` (`versionCode` + `versionPatch`)
2. Build and sign release APK/AAB
3. Publish a GitHub Release with both artifacts
4. Commit the version bump back to `main` with `[skip ci]`

Version numbers live in `version.properties` at the repo root.

Required GitHub repository secrets:

- `RELEASE_KEY_BASE64`
- `RELEASE_STORE_PASSWORD`
- `RELEASE_KEY_PASSWORD`
- `RELEASE_KEY_ALIAS`

## Auto-push after commit

Run once to enable automatic `git push` after every local commit:

```bash
./scripts/setup-git-hooks.sh
```

