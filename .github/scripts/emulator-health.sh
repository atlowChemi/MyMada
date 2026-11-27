#!/usr/bin/env bash
set -euo pipefail

critical_crash() {
  grep -E '(am_crash|am_anr): \[[^,]*,[^,]*,(com\.android\.systemui|system_server|com\.android\.launcher3|com\.android\.launcher|com\.google\.android\.apps\.nexuslauncher),' "$1"
}

ready() {
  grep -qx '1' "$1/boot-completed.txt" &&
    grep -Eq '(^|[[:space:]])system_server$' "$1/processes.txt" &&
    grep -Eq '(^|[[:space:]])com\.android\.systemui$' "$1/processes.txt" &&
    grep -Eq 'mCurrentFocus=.*(com\.android\.launcher3|com\.android\.launcher|com\.google\.android\.apps\.nexuslauncher)/' "$1/windows.txt"
}

if [[ ${1:-} == --self-test ]]; then
  dir=$(mktemp -d)
  trap 'rm -rf "$dir"' EXIT
  printf 'am_crash: [1795,0,com.android.systemui,123,NullPointerException]\n' > "$dir/events.txt"
  critical_crash "$dir/events.txt" >/dev/null
  printf 'am_anr: [0,1624,system_server,123,timeout]\n' > "$dir/events.txt"
  critical_crash "$dir/events.txt" >/dev/null
  printf 'am_crash: [1795,0,atlow.chemi.mymada.debug,123,Exception]\n' > "$dir/events.txt"
  if critical_crash "$dir/events.txt"; then exit 1; fi
  printf '1\n' > "$dir/boot-completed.txt"
  printf 'system 1624 system_server\nu0_a27 1795 com.android.systemui\n' > "$dir/processes.txt"
  printf 'mCurrentFocus=Window{123 u0 com.android.launcher3/.Launcher}\n' > "$dir/windows.txt"
  ready "$dir"
  printf 'mCurrentFocus=Window{123 u0 com.android.systemui/.Keyguard}\n' > "$dir/windows.txt"
  if ready "$dir"; then exit 1; fi
  printf 'mCurrentFocus=null\n' > "$dir/windows.txt"
  if ready "$dir"; then exit 1; fi
  echo 'Emulator health checks passed: crash, ANR, app-crash separation, launcher and keyguard'
  exit
fi

phase=${1:?Usage: emulator-health.sh preflight|postflight EVIDENCE_DIRECTORY}
out=${2:?Missing evidence directory}
[[ $phase == preflight || $phase == postflight ]]
mkdir -p "$out/$phase"
adb() { timeout --signal=TERM --kill-after=5 15 adb "$@"; }
deadline=$((SECONDS + 60))
while :; do
  adb logcat -b events -d > "$out/$phase/events.txt"
  if critical_crash "$out/$phase/events.txt" > "$out/$phase/critical-crashes.txt"; then
    echo "Emulator infrastructure failure: core process crash/ANR ($phase); no retry." | tee "$out/$phase/result.txt" >&2
    exit 1
  fi
  if [[ $phase == postflight ]]; then
    echo 'No recorded core-process crash/ANR during this boot' > "$out/$phase/result.txt"
    exit
  fi
  adb shell getprop sys.boot_completed | tr -d '\r' > "$out/$phase/boot-completed.txt"
  adb shell 'ps -A 2>/dev/null || ps' | tr -d '\r' > "$out/$phase/processes.txt"
  adb shell dumpsys window windows > "$out/$phase/windows.txt"
  if ready "$out/$phase"; then
    echo 'Boot complete; system_server/System UI present; launcher focused' > "$out/$phase/result.txt"
    exit
  fi
  if (( SECONDS >= deadline )); then
    echo 'Emulator infrastructure failure: boot/launcher readiness deadline exceeded; no instrumentation started.' | tee "$out/$phase/result.txt" >&2
    exit 1
  fi
  sleep 2
done
