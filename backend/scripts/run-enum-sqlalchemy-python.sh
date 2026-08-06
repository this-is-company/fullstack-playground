#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
APP_DIR="${ROOT_DIR}/enum-sqlalchemy-python-app"

cd "$APP_DIR"
if [[ ! -d .venv ]]; then
  python3 -m venv .venv
  .venv/bin/pip install -e ".[dev]"
fi

echo "Starting enum-sqlalchemy-python-app on :8097 ..."
exec .venv/bin/uvicorn app.main:app --host 0.0.0.0 --port 8097
