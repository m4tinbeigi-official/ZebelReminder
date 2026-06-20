#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

git -C "$ROOT_DIR" config core.hooksPath .githooks
chmod +x "$ROOT_DIR/.githooks/"* "$ROOT_DIR/scripts/"*.sh

echo "Git hooks enabled. Commits on any branch will auto-push to origin."
