#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT/catalog-api"
echo "Starting catalog-api on :8101 ..."
exec ./gradlew --no-daemon bootRun
