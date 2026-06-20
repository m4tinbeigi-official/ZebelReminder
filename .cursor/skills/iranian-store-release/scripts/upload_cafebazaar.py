#!/usr/bin/env python3
"""Upload release APK to Cafe Bazaar via Pishkhan API v1."""

from __future__ import annotations

import argparse
import json
import os
import sys
from pathlib import Path

try:
    import requests
except ImportError:
    print("Install requests: pip install requests", file=sys.stderr)
    sys.exit(1)

ROOT = Path(__file__).resolve().parents[4]
ENV_FILE = ROOT / ".env"
BASE_URL = "https://api.pishkhan.cafebazaar.ir/v1"


def load_env() -> None:
    if not ENV_FILE.is_file():
        return
    for line in ENV_FILE.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, _, value = line.partition("=")
        os.environ.setdefault(key.strip(), value.strip())


def headers() -> dict[str, str]:
    secret = os.environ.get("CAFEBAZAAR_PISHKHAN_API_SECRET")
    if not secret:
        sys.exit("CAFEBAZAAR_PISHKHAN_API_SECRET is missing from .env")
    return {
        "CAFEBAZAAR-PISHKHAN-API-SECRET": secret,
        "Accept": "application/json",
    }


def read_changelog(path: Path | None, fallback: str) -> str:
    if path and path.is_file():
        return path.read_text(encoding="utf-8").strip()
    return fallback


def check_uncommitted() -> dict:
    resp = requests.get(f"{BASE_URL}/apps/releases/last-uncommitted", headers=headers(), timeout=60)
    resp.raise_for_status()
    return resp.json()


def create_release() -> dict:
    resp = requests.post(f"{BASE_URL}/apps/releases/", headers=headers(), json={}, timeout=60)
    data = resp.json()
    if data.get("type") != "success":
        sys.exit(f"Cafe Bazaar create release failed: {json.dumps(data, ensure_ascii=False)}")
    return data


def upload_apk(apk_path: Path) -> dict:
    with apk_path.open("rb") as apk_file:
        resp = requests.post(
            f"{BASE_URL}/apps/releases/upload/",
            headers=headers(),
            files={"apk": (apk_path.name, apk_file, "application/vnd.android.package-archive")},
            data={"architecture": "0"},
            timeout=600,
        )
    data = resp.json()
    if data.get("type") != "success":
        sys.exit(f"Cafe Bazaar upload failed: {json.dumps(data, ensure_ascii=False)}")
    return data


def commit_release(
    changelog_fa: str,
    changelog_en: str,
    developer_note: str,
    auto_publish: bool = True,
    rollout: int = 100,
) -> dict:
    body = {
        "auto_publish": auto_publish,
        "changelog_fa": changelog_fa,
        "changelog_en": changelog_en,
        "developer_note": developer_note,
        "staged_rollout_percentage": rollout,
    }
    resp = requests.post(f"{BASE_URL}/apps/releases/commit/", headers=headers(), json=body, timeout=60)
    data = resp.json()
    if data.get("type") != "success":
        sys.exit(f"Cafe Bazaar commit failed: {json.dumps(data, ensure_ascii=False)}")
    return data


def main() -> None:
    load_env()
    parser = argparse.ArgumentParser(description="Upload APK to Cafe Bazaar")
    parser.add_argument("--apk", type=Path, help="Path to Bazaar-flavor APK")
    parser.add_argument("--changelog-fa", type=Path, default=ROOT / "release-notes/CHANGELOG_FA.txt")
    parser.add_argument("--changelog-en", type=Path, default=ROOT / "release-notes/CHANGELOG_EN.txt")
    parser.add_argument("--developer-note", default="")
    parser.add_argument("--check-status", action="store_true", help="Check uncommitted release status")
    parser.add_argument("--no-auto-publish", action="store_true")
    parser.add_argument("--rollout", type=int, default=100)
    args = parser.parse_args()

    if args.check_status:
        data = check_uncommitted()
        print(json.dumps(data, ensure_ascii=False, indent=2))
        return

    if not args.apk or not args.apk.is_file():
        sys.exit("--apk is required and must exist (unless using --check-status)")

    changelog_fa = read_changelog(args.changelog_fa, "به‌روزرسانی و رفع اشکالات")
    changelog_en = read_changelog(args.changelog_en, "Bug fixes and improvements")

    uncommitted = check_uncommitted()
    has_draft = uncommitted.get("type") != "not-exists"
    print(f"Uncommitted release exists: {has_draft}")

    if not has_draft:
        print("Creating new release...")
        create_release()

    print(f"Uploading {args.apk}...")
    upload_result = upload_apk(args.apk)
    print(json.dumps(upload_result, ensure_ascii=False, indent=2))

    print("Committing release...")
    commit_result = commit_release(
        changelog_fa,
        changelog_en,
        args.developer_note,
        auto_publish=not args.no_auto_publish,
        rollout=args.rollout,
    )
    print(json.dumps(commit_result, ensure_ascii=False, indent=2))
    print("Cafe Bazaar upload complete")


if __name__ == "__main__":
    main()
