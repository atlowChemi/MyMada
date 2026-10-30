# Pulse migration (#23)

`PulseCalc` remains the existing tools/drawer entry point. Compose owns portrait
and 64/36 landscape layout; AndroidView preserves the published floating-label
fields, disabled output, Open Sans Hebrew metrics and native input behavior.
Previews cover EN, HE, large Hebrew text and landscape.

## Contract

- Native `b36f4a9c` and actual production `12822`: initially empty fields;
  0–99 pulses counted over 15 seconds → count ×4 beats/minute. Leading zeroes
  are accepted. Invalid/empty edited input shows the existing error and retains
  the last valid output (including intermediate values while deleting digits).
- Tap the grey area: start 15 seconds, tap again: cancel/reset to 15. Native
  floor-seconds display is preserved (0 can appear before the completion beep).
  Completion is zero; tapping again restarts. Same 8000 Hz sample rate,
  3000-sample 9000.25 Hz wave followed by silence, half alarm-stream volume,
  with original volume restored even on failure.
- RN `be72a163` differs: initial zero, unrestricted digits, clears invalid output,
  ceil-seconds display and no landscape layout. Native is this slice's reference.

## Lifecycle

The user confirmed that counting and the completion beep must continue when
switching tools or backgrounding the app. `PulseTimer` owns one process-wide
monotonic deadline and uses only application context for audio. Removing a
Fragment, finishing an Activity or rotating does not cancel or restart it.
Returning attaches to the same running/completed measurement; stale saved state
cannot replay its beep. Only tapping a running timer cancels/resets it.

Inputs/error/output use saved Fragment state. A fresh process can restore an
unexpired saved deadline from the same boot; an expired snapshot displays zero
without replaying an old beep. Process termination/reboot delivery is not promised
by this in-process timer (no service/alarm added). Shared host/audio code is unchanged.

## Verification

`PulseStateTest`: every 0–99 count, leading zero, invalid values, native countdown
rounding and expiration. `PulseScreenTest`: EN/HE fields/error, retained result,
Activity recreation during countdown, real completion/reset, drawer/system Back;
start → another tool → actual rotation → return before completion → another tool
and background → original-deadline completion. Tests check exactly one successful
AudioTrack write sequence, alarm-volume restoration and no stale-snapshot replay.
Run the Pulse package with the existing AndroidJUnitRunner. Pure state test also
runs directly with cached Kotlin/JUnit, without a shared build dependency change.

Persistent screenshots, build/test logs and acceptance gaps:
`~/.local/share/opencode/evidence/pulse/EVIDENCE.md`.
