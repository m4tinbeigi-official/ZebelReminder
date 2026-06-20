#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../../.." && pwd)"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

SKIP_BUMP=false
MYKET_ONLY=false
BAZAAR_ONLY=false
BUILD_ONLY=false

while [[ $# -gt 0 ]]; do
  case "$1" in
    --skip-bump) SKIP_BUMP=true ;;
    --myket-only) MYKET_ONLY=true ;;
    --bazaar-only) BAZAAR_ONLY=true ;;
    --build-only) BUILD_ONLY=true ;;
    *) echo "Unknown option: $1" >&2; exit 1 ;;
  esac
  shift
done

if [[ "$MYKET_ONLY" == true && "$BAZAAR_ONLY" == true ]]; then
  echo "Cannot use --myket-only and --bazaar-only together" >&2
  exit 1
fi

cd "$ROOT_DIR"

if [[ -f "$ROOT_DIR/.env" ]]; then
  set -a
  # shellcheck disable=SC1091
  source "$ROOT_DIR/.env"
  set +a
fi

if [[ "$SKIP_BUMP" == false ]]; then
  echo "==> Bumping version..."
  "$ROOT_DIR/scripts/bump-version.sh"
fi

if [[ "$MYKET_ONLY" == true ]]; then
  echo "==> Building Myket release..."
  ./gradlew :app:assembleMyketRelease --no-daemon
elif [[ "$BAZAAR_ONLY" == true ]]; then
  echo "==> Building Cafe Bazaar release..."
  ./gradlew :app:assembleBazaarRelease --no-daemon
else
  bash "$SCRIPT_DIR/build-store-variants.sh"
fi

if [[ "$BUILD_ONLY" == true ]]; then
  echo "Build-only mode — skipping upload."
  exit 0
fi

CHANGELOG_FA="$ROOT_DIR/release-notes/CHANGELOG_FA.txt"
CHANGELOG_EN="$ROOT_DIR/release-notes/CHANGELOG_EN.txt"

if [[ "$BAZAAR_ONLY" != true ]]; then
  echo "==> Uploading to Myket..."
  python3 "$SCRIPT_DIR/upload_myket.py" \
    --apk "$ROOT_DIR/app/build/outputs/apk/myket/release/app-myket-release.apk" \
    --changelog-fa "$CHANGELOG_FA" \
    --changelog-en "$CHANGELOG_EN"
fi

if [[ "$MYKET_ONLY" != true ]]; then
  echo "==> Uploading to Cafe Bazaar..."
  python3 "$SCRIPT_DIR/upload_cafebazaar.py" \
    --apk "$ROOT_DIR/app/build/outputs/apk/bazaar/release/app-bazaar-release.apk" \
    --changelog-fa "$CHANGELOG_FA" \
    --changelog-en "$CHANGELOG_EN"
fi

echo ""
echo "==> Release pipeline finished."
