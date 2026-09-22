#!/usr/bin/env bash
# Car Comfort - physical Android device status (read-only, never mutates state)
set -euo pipefail
ADB="${ANDROID_SDK_ROOT:-$HOME/Library/Android/sdk}/platform-tools/adb"
if [ ! -x "$ADB" ]; then ADB="adb"; fi
echo "== adb version =="
"$ADB" version | head -n 3
echo ""
echo "== adb devices -l =="
"$ADB" devices -l
echo ""
echo "== env (masked) =="
echo "ANDROID_DEVICE_UDID=${ANDROID_DEVICE_UDID:+***SET***}"
echo "ANDROID_DEVICE_NAME=${ANDROID_DEVICE_NAME:-<unset>}"
echo "ANDROID_APP_PACKAGE=${ANDROID_APP_PACKAGE:-<unset>}"
echo "ANDROID_APP_ACTIVITY=${ANDROID_APP_ACTIVITY:-<unset>}"
echo "AUTOMATION_OWNER_ID=${AUTOMATION_OWNER_ID:-<unset>}"
echo ""
echo "== device locks =="
ls -la artifacts/device-locks/ 2>/dev/null || echo "<no lock dir yet>"
