#!/usr/bin/env bash
# Car Comfort - gated one-time run. Requires explicit authorization.
# Usage: ./scripts/run-gated.sh CARD_SETUP
set -euo pipefail
if [ "${RUN_GATED_TESTS:-false}" != "true" ]; then
  echo "REFUSED: set RUN_GATED_TESTS=true to run gated flows" >&2
  exit 2
fi
if [ -z "${GATED_CASE:-}" ] && [ -z "${1:-}" ]; then
  echo "REFUSED: set GATED_CASE=<case> (e.g. CARD_SETUP)" >&2
  exit 2
fi
CASE="${GATED_CASE:-$1}"
cd "$(dirname "$0")/.."
mvn test -DsuiteXmlFile=src/test/resources/suites/gated-one-time.xml \
  -DRUN_GATED_TESTS=true -DGATED_CASE="$CASE"
