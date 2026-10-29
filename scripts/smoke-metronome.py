#!/usr/bin/env python3
"""API 33 regression check on an isolated English/Hebrew emulator with debug APK installed.

ANDROID_SERIAL=emulator-5556 python3 scripts/smoke-metronome.py
Requires adb on PATH. Disables emulator networking and grants notification permission
as test setup; this does not verify the app's still-missing permission request flow.
Exercises real foreground service, notification content/action PendingIntents and UI.
"""

import re
import subprocess
import time
import xml.etree.ElementTree as ET

PACKAGE = "atlow.chemi.mymada.debug"
SERVICE = "atlow.chemi.mymada.recieversAndServices.MetroService"


def adb(*args):
    return subprocess.check_output(["adb", *args], text=True, timeout=30)


def nodes():
    result = adb("shell", "uiautomator", "dump", "/sdcard/mymada-smoke.xml")
    assert "dumped to:" in result, f"UI dump failed (do not read stale XML): {result}"
    return list(
        ET.fromstring(adb("shell", "cat", "/sdcard/mymada-smoke.xml")).iter("node")
    )


def tap(node):
    left, top, right, bottom = map(int, re.findall(r"\d+", node.attrib["bounds"]))
    adb("shell", "input", "tap", str((left + right) // 2), str((top + bottom) // 2))


def by_id(name):
    return next(n for n in nodes() if n.get("resource-id") == f"{PACKAGE}:id/{name}")


def foreground(expected):
    deadline = time.monotonic() + 15
    while time.monotonic() < deadline:
        state = adb("shell", "dumpsys", "activity", "services", SERVICE)
        if ("isForeground=true" in state) == expected:
            return
        time.sleep(0.25)
    raise AssertionError(f"Expected foreground={expected}\n{state}")


def notification_nodes():
    adb("shell", "cmd", "statusbar", "expand-notifications")
    return nodes()


assert adb("shell", "getprop", "ro.build.version.sdk").strip() == "33", (
    "Use API 33 emulator"
)
adb("shell", "svc", "wifi", "disable")
adb("shell", "svc", "data", "disable")
adb("shell", "pm", "grant", PACKAGE, "android.permission.POST_NOTIFICATIONS")
adb("shell", "am", "force-stop", PACKAGE)
adb("logcat", "-c")
adb("shell", "am", "start", "-W", "-n", f"{PACKAGE}/atlow.chemi.mymada.HomePage")
for node in nodes():
    if node.get("resource-id") == "android:id/button2":  # First-run 'not now'.
        tap(node)
        break
tap(by_id("buttonChange"))  # Home's first item is Tools / CPR metronome.
control = by_id("bpmW")
tap(control)
foreground(True)
# The running clock updates every 500 ms, so UIAutomator cannot reach idle.
# Reuse this screen's control bounds rather than reading an invalid/stale dump.
tap(control)
foreground(False)
print("PASS UI start/stop")

tap(control)
foreground(True)
adb("shell", "input", "keyevent", "KEYCODE_HOME")
title = next(
    n
    for n in notification_nodes()
    if n.get("text") in ("Metronome running in Background", "מטרונום פעיל ברקע")
)
tap(title)
activity = adb("shell", "dumpsys", "activity", "activities")
assert any(
    "topResumedActivity=" in line and "atlow.chemi.mymada.UtillitiesAndTools" in line
    for line in activity.splitlines()
), "Notification did not return to metronome"
foreground(True)
print("PASS background playback state / notification content navigation")

adb("shell", "input", "keyevent", "KEYCODE_HOME")
visible = notification_nodes()
if not any(n.get("text", "").lower() in ("stop", "עצור") for n in visible):
    tap(next(n for n in visible if n.get("resource-id") == "android:id/expand_button"))
    visible = nodes()
tap(next(n for n in visible if n.get("text", "").lower() in ("stop", "עצור")))
foreground(False)
assert PACKAGE not in adb("logcat", "-d", "-b", "crash"), (
    "App crashed; inspect logcat -b crash"
)
adb("shell", "cmd", "statusbar", "collapse")
print("PASS notification stop action; no app crash")
