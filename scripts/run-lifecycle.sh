#!/usr/bin/env bash
# Car Comfort - controlled business-lifecycle run. Owner-authorized only.
# Usage: ./scripts/run-lifecycle.sh BOOKING_LIFECYCLE_HAPPY_PATH [MAX_AUTHORIZED_PHASE]
#   MAX_AUTHORIZED_PHASE=1 → Phase 1 only (submit + read-only receipt, HARD STOP before accept).
#   May also be provided via env MAX_AUTHORIZED_PHASE.
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
MAX_PHASE="${MAX_AUTHORIZED_PHASE:-${2:-}}"
cd "$(dirname "$0")/.."
set -a; source .env 2>/dev/null || true; set +a
EXTRA_ARGS=""
if [ -n "$MAX_PHASE" ]; then
  EXTRA_ARGS="-DMAX_AUTHORIZED_PHASE=$MAX_PHASE"
fi
# shellcheck disable=SC2086
mvn test -DsuiteXmlFile=src/test/resources/suites/business-lifecycle.xml \
  -DRUN_BUSINESS_LIFECYCLE_TESTS=true -DBUSINESS_CASE="$CASE" $EXTRA_ARGS
