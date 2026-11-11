#!/usr/bin/env bash
set -euo pipefail

# The hosted runner is disposable. Block the emulator's host sockets before boot,
# independently of Android firewall rules and app-controlled network settings.
if [[ ${1:-} == isolate ]]; then
  mkdir -p emulator-evidence
  { nproc; free -m; cat /proc/pressure/cpu /proc/pressure/memory; } > emulator-evidence/host-before-boot.txt
  # Bounded sampling covers boot and initial test setup.
  vmstat -w 2 180 > emulator-evidence/host-boot-vmstat.txt &
  if [[ ${EXPECT_PAGE_SIZE:-} ]]; then
    grep -qx 'target=android-37.0' "$ANDROID_AVD_HOME/test.ini"
  fi
  # Check after system-image installation, before the action starts its boot wait.
  available_kb=$(df -Pk "$ANDROID_AVD_HOME" | awk 'NR == 2 {print $4}')
  if (( available_kb < 10 * 1024 * 1024 )); then
    echo "Insufficient emulator disk space: ${available_kb}KiB free; require 10GiB" >&2
    exit 1
  fi
  sudo groupadd ci-emulator
  gid=$(getent group ci-emulator | cut -d: -f3)
  sudo iptables -I OUTPUT ! -o lo -m owner --gid-owner "$gid" -j REJECT
  sudo ip6tables -I OUTPUT ! -o lo -m owner --gid-owner "$gid" -j REJECT
  mv "$ANDROID_HOME/emulator/emulator" "$ANDROID_HOME/emulator/emulator.real"
  cat > "$ANDROID_HOME/emulator/emulator" <<'WRAPPER'
#!/usr/bin/env bash
exec sudo -n -E setpriv --reuid="$(id -u)" --regid="$(getent group ci-emulator | cut -d: -f3)" --init-groups "$(dirname "$0")/emulator.real" "$@"
WRAPPER
  chmod +x "$ANDROID_HOME/emulator/emulator"
  [[ $(sudo -n -E setpriv --reuid="$(id -u)" --regid="$gid" --init-groups id -g) == "$gid" ]]
  touch "$RUNNER_TEMP/mymada-isolation-ready"
  # Keep software rendering/recording modest on the shared runner.
  sed -i '/^hw\.lcd\.\(width\|height\|density\)=/d' "$ANDROID_AVD_HOME/test.avd/config.ini"
  cat >> "$ANDROID_AVD_HOME/test.avd/config.ini" <<'DISPLAY'
hw.lcd.width=720
hw.lcd.height=1280
hw.lcd.density=320
DISPLAY
  exit
fi

export ANDROID_SERIAL=emulator-5554
# A disconnected/hung device must not block cleanup until the job is cancelled.
adb() { timeout --signal=TERM --kill-after=5 30 adb "$@"; }
out="$PWD/emulator-evidence"
mkdir -p "$out"
recorder=''
test_log=''
live_log=''
dump_app_threads() {
  # Old Android pidof implementations can miss long process names. Preserve ps
  # and the selected PID so an empty diagnostic cannot masquerade as a dump.
  adb shell 'ps -A 2>/dev/null || ps' | tr -d '\r' > "$out/process-list.txt"
  local pid
  pid=$(awk '$NF == "atlow.chemi.mymada.debug" {print $2; exit}' "$out/process-list.txt")
  if [[ $pid =~ ^[0-9]+$ ]]; then
    # Pre-O ART writes a shared file directly; unlike debuggerd it cannot create
    # that file in /data/anr. Prepare it without truncating existing ANR evidence.
    # shellcheck disable=SC2016 # Query the guest API, not the host.
    adb shell 'if [ "$(getprop ro.build.version.sdk)" -lt 26 ]; then touch /data/anr/traces.txt && chmod 0666 /data/anr/traces.txt && restorecon /data/anr/traces.txt; fi'
    echo "Requesting ART thread dump for app PID $pid"
    adb shell "kill -3 $pid; sleep 2"
  else
    echo 'No target app process found for ART thread dump'
  fi
}
cleanup() {
  status=$?
  trap - EXIT
  set +e
  echo "Collecting emulator evidence (test exit=$status)"
  if [[ -n $live_log ]]; then
    kill "$live_log" 2>/dev/null
    wait "$live_log" 2>/dev/null
  fi
  if [[ -n $test_log ]]; then
    kill "$test_log" 2>/dev/null
    wait "$test_log" 2>/dev/null
  fi
  if [[ -n $recorder ]]; then
    adb shell touch /sdcard/ci-stop
    # shellcheck disable=SC2016 # Resolve and signal the recorder on Android.
    adb shell 'for pid in $(pidof screenrecord); do kill -2 "$pid"; done'
    timeout 15 tail --pid="$recorder" -f /dev/null
    kill "$recorder" 2>/dev/null
    wait "$recorder" 2>/dev/null
  fi
  if (( status != 0 )); then
    # A hung test can time out without an Android ANR. Ask ART for its thread dump.
    dump_app_threads
    adb shell dumpsys activity processes > "$out/processes.txt"
  fi
  adb logcat -b all -d -v threadtime > "$out/logcat.txt"
  adb shell dumpsys activity activities > "$out/activities.txt"
  adb shell dumpsys window windows > "$out/windows.txt"
  { free -m; cat /proc/pressure/cpu /proc/pressure/memory; ps -eo pid,comm,pcpu,rss --sort=-rss | head -20; } > "$out/host-after-tests.txt"
  adb shell dumpsys package atlow.chemi.mymada.debug > "$out/package.txt"
  { sudo iptables -nvL OUTPUT; sudo ip6tables -nvL OUTPUT; } > "$out/host-firewall.txt"
  if (( status != 0 )); then
    adb shell dumpsys activity lastanr > "$out/last-anr.txt"
    adb pull /data/anr "$out/" > "$out/anr-pull.log" 2>&1
    adb pull /data/tombstones "$out/" > "$out/tombstone-pull.log" 2>&1
    adb exec-out screencap -p > "$out/failure.png"
    adb pull /sdcard/ci-video "$out/" || echo 'Video retrieval failed' >> "$out/capture.log"
    if ! find "$out" -name '*.mp4' -size +0c | grep -q .; then
      echo 'No usable video captured; see recorder log' >> "$out/capture.log"
    fi
  fi
  exit "$status"
}
trap cleanup EXIT


[[ -f "$RUNNER_TEMP/mymada-isolation-ready" ]]
# Some images close the transport before `adb root` receives its reply. The
# reconnected shell UID, not that transient transport result, proves root access.
adb root || echo 'adb root disconnected; checking UID after reconnection'
timeout 60 adb wait-for-device
device_uid=$(adb shell id -u | tr -d '\r')
if [[ $device_uid != 0 ]]; then
  echo "Emulator did not reconnect as root (UID=$device_uid)" >&2
  exit 1
fi
# Preserve the last guest messages even if a system crash makes adb dumps time out.
# Start after adb root's transport restart, which would terminate this stream.
command adb logcat -b all -v threadtime > "$out/live-logcat.txt" 2>&1 &
live_log=$!
# Android's network service can hold the xtables lock just after boot.
# API24 supports -w without seconds; bound the wait with the host timeout.
timeout 30 adb shell 'iptables -w -I OUTPUT ! -o lo -j REJECT && ip6tables -w -I OUTPUT ! -o lo -j REJECT'
timeout 30 adb shell 'iptables -w -C OUTPUT ! -o lo -j REJECT && ip6tables -w -C OUTPUT ! -o lo -j REJECT'
if [[ ${EXPECT_PAGE_SIZE:-} ]]; then
  [[ $(adb shell getconf PAGE_SIZE | tr -d '\r') == "$EXPECT_PAGE_SIZE" ]]
fi
adb shell settings put system font_scale "${FONT_SCALE:-1.0}"
adb shell settings put system accelerometer_rotation 0
adb shell settings put system user_rotation 0
adb shell settings put secure show_ime_with_hard_keyboard 0
# The workflow assembles before boot; do not start another Gradle JVM while
# SystemUI is initializing. The connected test task still owns installation.
if [[ ${TEST_CLASSES:-} == atlow.chemi.mymada.tools.vitals.VitalsScreenTest ]]; then
  adb logcat -b events -d > "$out/boot-events.txt"
  if grep -E 'am_anr.*com.android.systemui' "$out/boot-events.txt"; then
    echo 'Boot has a System UI ANR; refusing to run focus tests' >&2
    exit 1
  fi
fi
{
  git rev-parse HEAD
  java -version 2>&1
  adb shell getprop ro.build.fingerprint
  adb shell getprop sys.boot_completed
  adb shell getprop ro.build.version.sdk
  adb shell 'getconf PAGE_SIZE || echo PAGE_SIZE_UNAVAILABLE'
  adb shell settings get system font_scale
  sha256sum app/build/outputs/apk/debug/app-debug.apk app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
} > "$out/build-device.txt"
adb shell 'rm -f /sdcard/ci-stop; mkdir -p /sdcard/ci-video'
# Rotating two-minute segments avoid screenrecord's duration ceiling. The bounded
# test command below bounds recording at 1Mbps (10 minutes focused, 60 full suite).
# shellcheck disable=SC2016 # Expand the counter on the device, not the host.
command adb shell 'i=0; while [ ! -e /sdcard/ci-stop ]; do screenrecord --bit-rate 1000000 --size 540x960 --time-limit 120 /sdcard/ci-video/segment-$i.mp4 || exit; i=$((i+1)); done' > "$out/recorder.log" 2>&1 &
recorder=$!
command adb logcat -v threadtime 'TestRunner:I' 'MetronomeTest:I' '*:S' > "$out/test-progress.txt" 2>&1 &
test_log=$!
# Use the same platform for every shard; the legacy worker drops JAVA_TOOL_OPTIONS.
test_args=('-Pandroid.experimental.androidTest.builtin_test_platform=true')
install_options=--no-streaming
test_timeout=3600
if [[ -n ${TEST_CLASSES:-} ]]; then
  test_args+=("-Pandroid.testInstrumentationRunnerArguments.class=$TEST_CLASSES")
  test_timeout=600
fi
printf '%s\n' "${TEST_CLASSES:-All discovered instrumentation tests; default test arguments}" > "$out/selectors.txt"
# AGP 9.5 qualifies install options by device; engine 1.0.8 reads only the global key.
# JAVA_TOOL_OPTIONS reaches its worker JVM; this task is not a Gradle Test task.
JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:-} -Dandroid-test.apk-install-options=$install_options" \
timeout --signal=TERM --kill-after=30 "$test_timeout" ./gradlew :app:connectedDebugAndroidTest "${test_args[@]}" \
  --no-daemon --build-cache --max-workers=1 '-Dorg.gradle.jvmargs=-Xmx2g'
# AGP can return success after a device-install failure without any test results.
mapfile -t results < <(find app/build/outputs/androidTest-results/connected -name 'TEST-*.xml')
if (( ${#results[@]} == 0 )) || ! grep -Eq 'tests="[1-9][0-9]*"' "${results[@]}"; then
  echo 'Instrumentation produced no executed-test JUnit results' >&2
  exit 1
fi
if grep -Eq '<(failure|error)([ >])' "${results[@]}"; then
  echo 'Instrumentation reported test failures' >&2
  exit 1
fi
