#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../../.." && pwd)"
cd "$ROOT_DIR"

if [[ -f "$ROOT_DIR/.env" ]]; then
  set -a
  # shellcheck disable=SC1091
  source "$ROOT_DIR/.env"
  set +a
fi

echo "==> Building Myket release APK..."
./gradlew :app:assembleMyketRelease --no-daemon

echo "==> Building Cafe Bazaar release APK..."
./gradlew :app:assembleBazaarRelease --no-daemon

MYKET_APK="$ROOT_DIR/app/build/outputs/apk/myket/release/app-myket-release.apk"
BAZAAR_APK="$ROOT_DIR/app/build/outputs/apk/bazaar/release/app-bazaar-release.apk"

for apk in "$MYKET_APK" "$BAZAAR_APK"; do
  if [[ ! -f "$apk" ]]; then
    echo "ERROR: Expected APK not found: $apk" >&2
    echo "Ensure product flavors 'myket' and 'bazaar' exist in app/build.gradle.kts" >&2
    exit 1
  fi
done

echo ""
echo "Build successful:"
echo "  Myket:       $MYKET_APK"
echo "  Cafe Bazaar: $BAZAAR_APK"
