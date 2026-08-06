#!/usr/bin/env bash
set -euo pipefail

TOKEN_URL="http://localhost:8180/realms/demo/protocol/openid-connect/token"
API_URL="${1:-http://localhost:8091/api/me}"

echo "Requesting access token from Keycloak..."
RESPONSE="$(curl -s -X POST "$TOKEN_URL" \
  -d 'grant_type=password' \
  -d 'client_id=security-sso-app' \
  -d 'client_secret=sso-app-secret' \
  -d 'username=demo' \
  -d 'password=demo123')"

ACCESS_TOKEN="$(python3 -c 'import json,sys; print(json.load(sys.stdin)["access_token"])' <<<"$RESPONSE")"

echo "Calling $API_URL ..."
curl -s -H "Authorization: Bearer ${ACCESS_TOKEN}" "$API_URL"
echo
