#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROPS_FILE="$ROOT_DIR/version.properties"

if [[ ! -f "$PROPS_FILE" ]]; then
  echo "version.properties not found at $PROPS_FILE" >&2
  exit 1
fi

get_prop() {
  local key="$1"
  grep "^${key}=" "$PROPS_FILE" | cut -d= -f2-
}

set_prop() {
  local key="$1"
  local value="$2"
  if grep -q "^${key}=" "$PROPS_FILE"; then
    sed -i.bak "s/^${key}=.*/${key}=${value}/" "$PROPS_FILE"
  else
    echo "${key}=${value}" >> "$PROPS_FILE"
  fi
}

major="$(get_prop versionMajor)"
minor="$(get_prop versionMinor)"
patch="$(get_prop versionPatch)"
code="$(get_prop versionCode)"

new_code=$((code + 1))
new_patch=$((patch + 1))
version_name="${major}.${minor}.${new_patch}"

set_prop versionPatch "$new_patch"
set_prop versionCode "$new_code"

rm -f "${PROPS_FILE}.bak"

echo "versionCode=$new_code"
echo "versionName=$version_name"
