#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
APP_DIR="${ROOT_DIR}/consumer-gradle-app"

cd "$APP_DIR"
echo "Resolving db-query-lib from Nexus (Gradle) and starting consumer-gradle-app on :8082..."
./gradlew --no-daemon bootRun
