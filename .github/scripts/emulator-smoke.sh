#!/usr/bin/env bash
set -euo pipefail

# The hosted runner is disposable. Block the emulator's host sockets before boot,
# independently of Android firewall rules and app-controlled network settings.
if [[ ${1:-} == isolate ]]; then
  sudo groupadd ci-emulator
  gid=$(getent group ci-emulator | cut -d: -f3)
  sudo iptables -I OUTPUT ! -o lo -m owner --gid-owner "$gid" -j REJECT
  sudo ip6tables -I OUTPUT ! -o lo -m owner --gid-owner "$gid" -j REJECT
  mv "$ANDROID_HOME/emulator/emulator" "$ANDROID_HOME/emulator/emulator.real"
  cat > "$ANDROID_HOME/emulator/emulator" <<'WRAPPER'
#!/usr/bin/env bash
exec sudo -E -u "$(id -un)" -g ci-emulator "$(dirname "$0")/emulator.real" "$@"
WRAPPER
  chmod +x "$ANDROID_HOME/emulator/emulator"
  exit
fi

export ANDROID_SERIAL=emulator-5554
out="$PWD/emulator-evidence"
mkdir -p "$out"
recorder=''
cleanup() {
  status=$?
  trap - EXIT
  set +e
  if [[ -n $recorder ]]; then
    adb shell touch /sdcard/ci-stop
    adb shell 'pkill -2 screenrecord'
    timeout 15 tail --pid="$recorder" -f /dev/null
    kill "$recorder" 2>/dev/null
    wait "$recorder" 2>/dev/null
  fi
  adb logcat -d -v threadtime -t 3000 > "$out/logcat.txt"
  adb shell dumpsys activity activities > "$out/activities.txt"
  adb shell dumpsys package atlow.chemi.mymada.debug > "$out/package.txt"
  { sudo iptables -nvL OUTPUT; sudo ip6tables -nvL OUTPUT; } > "$out/host-firewall.txt"
  if (( status != 0 )); then
    adb exec-out screencap -p > "$out/failure.png"
    adb pull /sdcard/ci-video "$out/" || echo 'Video retrieval failed' >> "$out/capture.log"
    if ! find "$out" -name '*.mp4' -size +0c | grep -q .; then
      echo 'No usable video captured; see recorder log' >> "$out/capture.log"
    fi
  fi
  exit "$status"
}
trap cleanup EXIT

adb root
adb wait-for-device
[[ $(adb shell id -u | tr -d '\r') == 0 ]]
adb shell 'iptables -I OUTPUT ! -o lo -j REJECT; ip6tables -I OUTPUT ! -o lo -j REJECT'
adb shell 'iptables -C OUTPUT ! -o lo -j REJECT && ip6tables -C OUTPUT ! -o lo -j REJECT'
adb shell settings put system font_scale "${FONT_SCALE:-1.0}"
adb shell settings put system accelerometer_rotation 0
adb shell settings put system user_rotation 0
adb shell settings put secure show_ime_with_hard_keyboard 0
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
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell appops set atlow.chemi.mymada.debug SYSTEM_ALERT_WINDOW allow
adb shell 'rm -f /sdcard/ci-stop; mkdir -p /sdcard/ci-video'
# Rotating two-minute segments avoid screenrecord's duration ceiling. The bounded
# test command below limits total recording to ten minutes at 1Mbps.
# shellcheck disable=SC2016 # Expand the counter on the device, not the host.
adb shell 'i=0; while [ ! -e /sdcard/ci-stop ]; do screenrecord --bit-rate 1000000 --size 540x960 --time-limit 120 /sdcard/ci-video/segment-$i.mp4 || exit; i=$((i+1)); done' > "$out/recorder.log" 2>&1 &
recorder=$!
selectors='atlow.chemi.mymada.ToolsBackNavigationTest#routesRecreateAndSystemBackInBothLocales,atlow.chemi.mymada.ToolsBackNavigationTest#permissionCompletionUpdatesToolbarWithoutAnotherResume,atlow.chemi.mymada.settings.SettingsNavigationTest'
printf '%s\n' "$selectors" > "$out/selectors.txt"
timeout --signal=TERM --kill-after=30 600 ./gradlew :app:connectedDebugAndroidTest \
  "-Pandroid.testInstrumentationRunnerArguments.class=$selectors" \
  --no-daemon --build-cache '-Dorg.gradle.jvmargs=-Xmx3g'
