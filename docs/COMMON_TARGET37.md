# Common target37 experiment

This candidate starts at the verified completed-screen baseline `ac438f61` and
replays the stable calls stack through `4e049d8b` (#74). Original branches remain
unchanged. Shared permission commit `670c9d16` is omitted because its patch is
already in the common baseline (`0a156790`).

The only replay conflicts were in shared `ActionReceiver`: retain the integrated
metronome start/stop paths and route `runInBG` to `DetectionStatus.stop`. Calls
must not restore the older metronome control implementation.

`targetSdkVersion 37` deliberately enables real platform enforcement; compileSdk
and targetSdk are both37. This is an experimental validation branch, not an
assertion of completed edge-to-edge, OEM/background reliability, production
upgrade/signature, visual or product acceptance. The listener-only policy and
limits remain in `calls/LISTENER-ONLY.md`.

Build debug/test/optimized release, JVM checks and both lints with the pinned
SDK/JDK. Verify packaged manifest target37 and exact APK hashes before runtime.
Calls owner owns5920/API37 and5922/API24; common integration owns5960/API37 and
5962/API24. All are isolated disposable emulators. Calls runtime must test the
combined artifacts rather than extrapolate isolated target33 results. Shared
metronome and screen/insets checks belong to the common candidate owner.

Evidence and immutable handoff artifacts:
`~/.local/share/opencode/evidence/m2-integration/common-target37/`.
No merge or reduced-reliability acceptance follows from a passing build/test.

The first target37 build exposed GestureBackNavigation lint: Tools still overrode
Activity.onBackPressed, which modern back gestures bypass. Its Kotlin dispatcher
bridge closes an unlocked drawer, otherwise delegates normal back navigation.
The focused regression covers recreation, closed drawer and locked-open drawer.
