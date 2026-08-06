#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
APP_DIR="${ROOT_DIR}/consumer-mybatis-gradle-app"

cd "$APP_DIR"
echo "Resolving db-query-mybatis-lib from Nexus (Gradle) and starting consumer-mybatis-gradle-app (:8084)..."
./gradlew --no-daemon bootRun
