#!/usr/bin/env bash
# Car Comfort - safe smoke run (never includes gated groups)
set -euo pipefail
cd "$(dirname "$0")/.."
mvn clean test -DsuiteXmlFile=src/test/resources/suites/smoke.xml
