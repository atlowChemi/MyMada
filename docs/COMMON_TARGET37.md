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

## Observed gates on application source1352cdca

Debug/test/optimized release, JVM5/5 and both lints pass. Common API24/API37 runs
cover Platform2/2, metronome controls2/2, Oxygen3/3, and corrected Tools back1/1
on each API; API37 actual notification grant/deny passes1/1 each. Real API37 back
gestures close the open drawer, then leave Tools. The test-only corrections await
normal root-task backgrounding/stop, not immediate Activity.isFinishing.

Target37 typography is **not accepted**: runtime throws IllegalAccessException
when legacy FontsOverride tries to assign static-final Typeface fields. The
Oxygen spinner consequently renders monospace; enforced edge-to-edge also changes
the status-bar background. Retain evidence and migrate to supported explicit
font/theme/inset handling before affected-screen visual acceptance. These issues
are not waived by passing functional tests or isolated target33 screenshots.

Calls owner additionally observed a pre-existing rebind loss window in a direct
API37 comparison: both the prior FGS and listener-only versions missed a source
event sent immediately after SIGKILL, before rebinding, then restored controls.
This is not zero-loss delivery evidence. No replay/deduplication policy is inferred.
Common-target37 calls results remain a separate owner handoff, not assumed from
that isolated comparison or from successful compilation.
