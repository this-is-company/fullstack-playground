#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "${ROOT_DIR}/security-sso-gradle-app"
echo "Starting security-sso-gradle-app on :8092 ..."
./gradlew --no-daemon bootRun
