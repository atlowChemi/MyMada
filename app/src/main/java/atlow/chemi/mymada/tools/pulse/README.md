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

## Lifecycle decisions requiring acceptance

Inputs/error/output and monotonic timer deadline survive saved Fragment state and
Activity recreation. Backgrounding the Activity does not pause the countdown.
An already-expired restored deadline displays zero without replaying an old beep.
Removing the Fragment (another tool, Back, Activity finish) cancels its timer:
the old retained Fragment could continue referencing destroyed views/Activity.
This deliberate ownership correction needs maintainer acceptance; it is not a
claim of exact parity with the legacy detached-timer bug. No service/alarm or
reboot persistence is introduced. Shared host navigation is unchanged.

## Verification

`PulseStateTest`: every 0–99 count, leading zero, invalid values, native countdown
rounding and expiration. `PulseScreenTest`: EN/HE fields/error, retained result,
Activity recreation during countdown, real completion/reset, drawer/system Back.
Run the Pulse package with the existing AndroidJUnitRunner. Pure state test also
runs directly with cached Kotlin/JUnit, without a shared build dependency change.

Persistent screenshots, build/test logs and acceptance gaps:
`~/.local/share/opencode/evidence/pulse/EVIDENCE.md`.
