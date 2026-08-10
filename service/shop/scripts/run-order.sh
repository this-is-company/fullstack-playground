#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PROFILE="${1:-local}"
cd "$ROOT/order-api"
echo "Starting order-api on :8102 (profile=$PROFILE) ..."
exec ./gradlew --no-daemon bootRun --args="--spring.profiles.active=${PROFILE}"
