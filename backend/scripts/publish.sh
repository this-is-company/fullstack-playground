#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
MVN="${ROOT_DIR}/apache-maven-3.9.9/bin/mvn"
SETTINGS="${ROOT_DIR}/settings.xml"
PASSWORD_FILE="${ROOT_DIR}/.nexus-password"

if [[ ! -x "$MVN" ]]; then
  echo "Maven not found at $MVN" >&2
  exit 1
fi

if [[ ! -f "$PASSWORD_FILE" ]]; then
  echo "Nexus password file missing. Run scripts/start-infra.sh first." >&2
  exit 1
fi

export NEXUS_PASSWORD
NEXUS_PASSWORD="$(cat "$PASSWORD_FILE")"

echo "Publishing db-query-lib (jar + sources + javadoc) to Nexus..."
"$MVN" -f "${ROOT_DIR}/db-query-lib/pom.xml" \
  -s "$SETTINGS" \
  clean deploy \
  -DskipTests

echo
echo "Published:"
echo "  com.example:db-query-lib:1.0.2"
echo "  sources : db-query-lib-1.0.2-sources.jar"
echo "  Nexus   : http://localhost:8081/#browse/browse:maven-releases"
