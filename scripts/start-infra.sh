#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

echo "[1/4] Starting Nexus + PostgreSQL..."
docker compose up -d

echo "[2/4] Waiting for PostgreSQL..."
until docker exec db-query-postgres pg_isready -U demo -d demodb >/dev/null 2>&1; do
  sleep 2
done
echo "PostgreSQL is ready."

echo "[3/4] Waiting for Nexus (first boot can take 1-2 minutes)..."
for i in $(seq 1 90); do
  if curl -sf http://localhost:8081/service/rest/v1/status >/dev/null 2>&1; then
    echo "Nexus is ready."
    break
  fi
  if [[ "$i" -eq 90 ]]; then
    echo "Nexus did not become ready in time." >&2
    exit 1
  fi
  sleep 5
done

echo "[4/4] Preparing Nexus admin password..."
bash "$ROOT_DIR/scripts/setup-nexus.sh"

echo
echo "Infra is ready."
echo "  Nexus UI : http://localhost:8081  (admin / see NEXUS_PASSWORD)"
echo "  Postgres : localhost:5432 / demodb / demo / demo123"
