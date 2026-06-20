#!/usr/bin/env python3
"""Upload release APK to Myket via Partner API."""

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


def load_env() -> None:
    if not ENV_FILE.is_file():
        return
    for line in ENV_FILE.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, _, value = line.partition("=")
        os.environ.setdefault(key.strip(), value.strip())


def api_base(package: str) -> str:
    return f"https://developer.myket.ir/api/partners/applications/{package}"


def headers() -> dict[str, str]:
    token = os.environ.get("MYKET_ACCESS_TOKEN")
    if not token:
        sys.exit("MYKET_ACCESS_TOKEN is missing from .env")
    return {"X-Access-Token": token}


def read_changelog(path: Path | None, fallback: str) -> str:
    if path and path.is_file():
        return path.read_text(encoding="utf-8").strip()
    return fallback


def list_bundles(package: str, status: str | None) -> dict:
    url = f"{api_base(package)}/release-bundle"
    params = {}
    if status:
        params["status"] = status
    resp = requests.get(url, headers=headers(), params=params, timeout=60)
    resp.raise_for_status()
    return resp.json()


def create_bundle(
    package: str,
    title: str,
    changelog_fa: str,
    changelog_en: str,
    rollout: int = 100,
) -> dict:
    url = f"{api_base(package)}/release-bundle"
    body = {
        "title": title,
        "stagedRolloutPercent": rollout,
        "translationInfos": [
            {"description": changelog_en, "language": "en"},
            {"description": changelog_fa, "language": "fa"},
        ],
    }
    resp = requests.put(url, headers=headers(), json=body, timeout=60)
    if resp.status_code >= 400:
        sys.exit(f"Myket create bundle failed ({resp.status_code}): {resp.text}")
    return resp.json()


def upload_apk(package: str, apk_path: Path) -> list:
    url = f"{api_base(package)}/release-bundle/upload"
    with apk_path.open("rb") as apk_file:
        resp = requests.put(
            url,
            headers=headers(),
            files={"": (apk_path.name, apk_file, "application/vnd.android.package-archive")},
            timeout=600,
        )
    if resp.status_code >= 400:
        sys.exit(f"Myket upload failed ({resp.status_code}): {resp.text}")
    return resp.json()


def commit_bundle(package: str, message: str, manual_publish: bool = False) -> dict:
    url = f"{api_base(package)}/release-bundle/commit"
    body = {"isManualPublish": manual_publish, "message": message}
    resp = requests.post(url, headers=headers(), json=body, timeout=60)
    if resp.status_code >= 400:
        sys.exit(f"Myket commit failed ({resp.status_code}): {resp.text}")
    return resp.json()


def main() -> None:
    load_env()
    parser = argparse.ArgumentParser(description="Upload APK to Myket")
    parser.add_argument("--apk", type=Path, help="Path to Myket-flavor APK")
    parser.add_argument("--changelog-fa", type=Path, default=ROOT / "release-notes/CHANGELOG_FA.txt")
    parser.add_argument("--changelog-en", type=Path, default=ROOT / "release-notes/CHANGELOG_EN.txt")
    parser.add_argument("--package", default=os.environ.get("APP_PACKAGE_NAME", "ir.m4tinbeigi.taskreminder"))
    parser.add_argument("--title", default="Zebel")
    parser.add_argument("--status", help="List bundles by status (e.g. Rejected, WaitingForApproval)")
    parser.add_argument("--rollout", type=int, default=100)
    args = parser.parse_args()

    if args.status:
        data = list_bundles(args.package, args.status)
        print(json.dumps(data, ensure_ascii=False, indent=2))
        return

    if not args.apk or not args.apk.is_file():
        sys.exit("--apk is required and must exist (unless using --status)")

    changelog_fa = read_changelog(args.changelog_fa, "به‌روزرسانی و رفع اشکالات")
    changelog_en = read_changelog(args.changelog_en, "Bug fixes and improvements")

    print(f"Creating Myket release bundle for {args.package}...")
    create_bundle(args.package, args.title, changelog_fa, changelog_en, args.rollout)

    print(f"Uploading {args.apk}...")
    upload_result = upload_apk(args.package, args.apk)
    print(json.dumps(upload_result, ensure_ascii=False, indent=2))

    print("Committing for review...")
    commit_result = commit_bundle(args.package, changelog_fa)
    print(json.dumps(commit_result, ensure_ascii=False, indent=2))
    print("Myket upload complete — status: WaitingForApproval")


if __name__ == "__main__":
    main()
