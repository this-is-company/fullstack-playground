#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
MVN="${ROOT_DIR}/apache-maven-3.9.9/bin/mvn"
cd "${ROOT_DIR}/security-form-app"
echo "Starting security-form-app on :8090 ..."
"$MVN" -q spring-boot:run -DskipTests
