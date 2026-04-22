#!/usr/bin/env bash
set -euo pipefail

apk="app/build/outputs/apk/hptoy/release/app-hptoy-release-smoke.apk"
package_name="com.hptoy"

check_no_crash() {
  crash_log="$(adb logcat -d -v brief | grep -E 'FATAL EXCEPTION|AndroidRuntime|Process: com.hptoy' || true)"
  if echo "$crash_log" | grep -q "$package_name"; then
    echo "$crash_log"
    exit 1
  fi
}

grant_permission() {
  adb shell pm grant "$package_name" "$1" >/dev/null 2>&1 || true
}

actual_api_level="$(adb shell getprop ro.build.version.sdk | tr -d '\r')"
actual_release="$(adb shell getprop ro.build.version.release | tr -d '\r')"
echo "Emulator Android release: $actual_release"
echo "Emulator API level: $actual_api_level"

if [[ -n "${EXPECTED_API_LEVEL:-}" && "$actual_api_level" != "$EXPECTED_API_LEVEL" ]]; then
  echo "Expected emulator API level $EXPECTED_API_LEVEL, got $actual_api_level"
  exit 1
fi

adb install -r "$apk"
grant_permission android.permission.ACCESS_COARSE_LOCATION
grant_permission android.permission.ACCESS_FINE_LOCATION
grant_permission android.permission.BLUETOOTH_SCAN
grant_permission android.permission.BLUETOOTH_CONNECT
grant_permission android.permission.READ_EXTERNAL_STORAGE

adb logcat -c
adb shell monkey -p "$package_name" -c android.intent.category.LAUNCHER 1
sleep 10

if ! adb shell pidof "$package_name" >/dev/null 2>&1; then
  adb shell ps -A | grep "$package_name" || adb shell ps | grep "$package_name" || {
    echo "$package_name did not stay running after launch"
    adb logcat -d -v brief
    exit 1
  }
fi

check_no_crash
PACKAGE_NAME="$package_name" python3 .github/scripts/ui-crawler.py
check_no_crash
