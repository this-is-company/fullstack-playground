#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

bash "${ROOT_DIR}/scripts/start-infra.sh"
bash "${ROOT_DIR}/scripts/publish.sh"

echo
echo "Starting consumer-app in background..."
bash "${ROOT_DIR}/scripts/run-consumer.sh" > "${ROOT_DIR}/consumer-app.log" 2>&1 &
APP_PID=$!
echo "consumer-app pid=${APP_PID}"

echo "Waiting for consumer-app..."
for i in $(seq 1 60); do
  if curl -sf http://localhost:8080/api/users/count >/dev/null 2>&1; then
    break
  fi
  if ! kill -0 "$APP_PID" 2>/dev/null; then
    echo "consumer-app exited early. See consumer-app.log" >&2
    tail -n 80 "${ROOT_DIR}/consumer-app.log" >&2 || true
    exit 1
  fi
  sleep 3
done

echo
echo "===== DB query via Nexus library ====="
echo
echo "GET /api/users/count"
curl -s http://localhost:8080/api/users/count
echo
echo
echo "GET /api/users"
curl -s http://localhost:8080/api/users | python3 -m json.tool
echo
echo "GET /api/users/1"
curl -s http://localhost:8080/api/users/1 | python3 -m json.tool
echo
echo "GET /api/query"
curl -s --get "http://localhost:8080/api/query" \
  --data-urlencode "sql=SELECT name, department FROM users WHERE department = 'Engineering' ORDER BY name" \
  | python3 -m json.tool

echo
echo "Demo complete. consumer-app is still running (pid=${APP_PID})."
echo "Stop with: kill ${APP_PID}"
echo "Stop infra: docker compose -f ${ROOT_DIR}/docker-compose.yml down"
