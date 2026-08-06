#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
MVN="${ROOT_DIR}/apache-maven-3.9.9/bin/mvn"
SETTINGS="${ROOT_DIR}/settings.xml"
PASSWORD_FILE="${ROOT_DIR}/.nexus-password"

if [[ ! -f "$PASSWORD_FILE" ]]; then
  echo "Nexus password file missing. Run scripts/start-infra.sh first." >&2
  exit 1
fi

export NEXUS_PASSWORD
NEXUS_PASSWORD="$(cat "$PASSWORD_FILE")"

rm -rf "${HOME}/.m2/repository/com/example/db-query-mybatis-lib" || true

echo "Resolving db-query-mybatis-lib from Nexus and starting consumer-mybatis-app (:8083)..."
"$MVN" -f "${ROOT_DIR}/consumer-mybatis-app/pom.xml" \
  -s "$SETTINGS" \
  clean spring-boot:run \
  -DskipTests
