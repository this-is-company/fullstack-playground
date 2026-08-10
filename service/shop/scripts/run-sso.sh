#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT/sso"
echo "Starting SSO on :8100 ..."
exec ./gradlew --no-daemon bootRun
