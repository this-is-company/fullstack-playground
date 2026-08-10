#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
LOG_DIR="${ROOT}/.logs"

for name in sso catalog order; do
  pid_file="${LOG_DIR}/${name}.pid"
  if [[ -f "$pid_file" ]]; then
    pid="$(cat "$pid_file")"
    if kill -0 "$pid" 2>/dev/null; then
      # gradle bootRun spawns children — kill process group if possible
      kill "$pid" 2>/dev/null || true
      echo "stopped $name (pid $pid)"
    fi
    rm -f "$pid_file"
  fi
done
pkill -f 'com.example.shop.(sso|catalog|order)' 2>/dev/null || true
echo "done"
