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

# Force resolution from Nexus (no local install of the lib)
rm -rf "${HOME}/.m2/repository/com/example/db-query-lib" || true

echo "Resolving db-query-lib from Nexus and starting consumer-app..."
"$MVN" -f "${ROOT_DIR}/consumer-app/pom.xml" \
  -s "$SETTINGS" \
  clean spring-boot:run \
  -DskipTests
