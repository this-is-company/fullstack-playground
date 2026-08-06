#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
PASSWORD_FILE="$ROOT_DIR/.nexus-password"

# Already configured in a previous run
if [[ -f "$PASSWORD_FILE" ]]; then
  export NEXUS_PASSWORD
  NEXUS_PASSWORD="$(cat "$PASSWORD_FILE")"
  if curl -sf -u "admin:${NEXUS_PASSWORD}" \
      http://localhost:8081/service/rest/v1/status >/dev/null 2>&1; then
    echo "Nexus credentials already configured."
    echo "NEXUS_PASSWORD=${NEXUS_PASSWORD}"
    exit 0
  fi
fi

echo "Reading initial Nexus admin password from container..."
INITIAL_PASSWORD=""
for i in $(seq 1 60); do
  INITIAL_PASSWORD="$(docker exec db-query-nexus cat /nexus-data/admin.password 2>/dev/null || true)"
  if [[ -n "$INITIAL_PASSWORD" ]]; then
    break
  fi
  # After password change, admin.password may already be gone
  if curl -sf http://localhost:8081/service/rest/v1/status >/dev/null 2>&1; then
    break
  fi
  sleep 3
done

NEW_PASSWORD="${NEXUS_PASSWORD:-nexusadmin123}"

if [[ -n "$INITIAL_PASSWORD" ]]; then
  echo "Changing default Nexus admin password..."
  curl -sf -u "admin:${INITIAL_PASSWORD}" \
    -X PUT "http://localhost:8081/service/rest/v1/security/users/admin/change-password" \
    -H "Content-Type: text/plain" \
    --data "$NEW_PASSWORD" >/dev/null

  # Acknowledge anonymous access prompt / enable anonymous if needed
  curl -sf -u "admin:${NEW_PASSWORD}" \
    -X PUT "http://localhost:8081/service/rest/v1/security/anonymous" \
    -H "Content-Type: application/json" \
    -d '{"enabled":true,"userId":"anonymous","realmName":"NexusAuthorizingRealm"}' >/dev/null || true
fi

# Verify login with the chosen password
if ! curl -sf -u "admin:${NEW_PASSWORD}" \
    http://localhost:8081/service/rest/v1/status >/dev/null 2>&1; then
  echo "Unable to authenticate to Nexus with password '${NEW_PASSWORD}'." >&2
  echo "Set NEXUS_PASSWORD to the current admin password and re-run." >&2
  exit 1
fi

echo "$NEW_PASSWORD" > "$PASSWORD_FILE"
chmod 600 "$PASSWORD_FILE"
export NEXUS_PASSWORD="$NEW_PASSWORD"
echo "Nexus admin password saved to .nexus-password"
echo "NEXUS_PASSWORD=${NEXUS_PASSWORD}"
