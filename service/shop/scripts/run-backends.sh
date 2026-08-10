#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
LOG_DIR="${ROOT}/.logs"
mkdir -p "$LOG_DIR"

start_one() {
  local name="$1"
  local script="$2"
  shift 2
  echo "→ $name"
  nohup bash "$script" "$@" >"${LOG_DIR}/${name}.log" 2>&1 &
  echo $! >"${LOG_DIR}/${name}.pid"
}

start_one sso "${ROOT}/scripts/run-sso.sh"
start_one catalog "${ROOT}/scripts/run-catalog.sh"
start_one order "${ROOT}/scripts/run-order.sh" local

echo
echo "Backends starting. Logs: ${LOG_DIR}"
echo "  SSO      http://localhost:8100/swagger-ui.html"
echo "  Catalog  http://localhost:8101/swagger-ui.html"
echo "  Order    http://localhost:8102/swagger-ui.html"
echo "Stop: ./scripts/stop-backends.sh"
