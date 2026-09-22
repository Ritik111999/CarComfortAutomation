#!/usr/bin/env bash
# Car Comfort - controlled business-lifecycle run. Owner-authorized only.
# Usage: ./scripts/run-lifecycle.sh BOOKING_LIFECYCLE_HAPPY_PATH
set -euo pipefail
if [ "${RUN_BUSINESS_LIFECYCLE_TESTS:-false}" != "true" ]; then
  echo "REFUSED: set RUN_BUSINESS_LIFECYCLE_TESTS=true to run business lifecycles" >&2
  exit 2
fi
if [ -z "${BUSINESS_CASE:-}" ] && [ -z "${1:-}" ]; then
  echo "REFUSED: set BUSINESS_CASE=<case> (e.g. BOOKING_LIFECYCLE_HAPPY_PATH)" >&2
  exit 2
fi
CASE="${BUSINESS_CASE:-$1}"
cd "$(dirname "$0")/.."
set -a; source .env 2>/dev/null || true; set +a
mvn test -DsuiteXmlFile=src/test/resources/suites/business-lifecycle.xml \
  -DRUN_BUSINESS_LIFECYCLE_TESTS=true -DBUSINESS_CASE="$CASE"
