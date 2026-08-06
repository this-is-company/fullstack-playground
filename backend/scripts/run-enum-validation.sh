#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "${ROOT_DIR}/enum-mybatis-validation-app"
echo "Starting enum-mybatis-validation-app on :8093 ..."
./gradlew --no-daemon bootRun
