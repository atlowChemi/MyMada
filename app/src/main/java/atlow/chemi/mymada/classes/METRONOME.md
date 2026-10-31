# Metronome migration (#25)

Baseline: native `b36f4a9c`, RN `be72a163`; stack starts at #53 `6cc97c09`.
Settings compatibility from #22 is included. Current target remains 33 pending
#24 permission/service work and #21 latest-target UI verification.

## Audio layer

- Preserve native alarm-stream tones (1,000 frames at 8 kHz, 9440/9350 Hz source
  frequencies) plus 3,800 silent frames: nominal 100 BPM. The output sample clock
  is monotonic; no wall-clock sleeps determine the beat period. These historical
  above-Nyquist frequencies intentionally retain the existing aliased sound.
- Native accent is every 30 beats. RN's `++currentBeat > 30` creates a 31-beat
  cycle; retain native 30, as recorded in #25, pending maintainer acceptance.
- Replace native static track and unsynchronized stop/write with independently
  owned tracks, serialized nonblocking writes/release, partial-write handling,
  idempotent cleanup and stop-before-start handling. RN's channel mask correction
  is retained. A stopped write cannot continue into a recreated player.
- Java PulseCalc and parallel Kotlin PulseTimer use the same public API. Their
  intentional background countdown/tone is unchanged. #60 calls use MediaPlayer;
  concurrent alarm-volume overrides remain a separate integration concern.
- Instrumentation checks cover exact wave samples, 30-beat grouping, nominal
  frame duration, real AudioTrack stop/write/recreate and independent Pulse tone.
  Emulator writes do not establish physical audibility or real-device tempo.

## Follow-through

RN fixes `43cba244` (distinct start/action intents), `f89a00d9` (context lifetime),
`2346da53` (silent notification channel/media-playback service type) apply to the
service/control layer; RN headless JS infrastructure is not needed. `dd1501d4`
listener/boot rewrite belongs to #24. Retain RN tile listening-state refresh and
volume cleanup, native widget identity, and both native/RN tile component names.
Service/UI elapsed time must use elapsedRealtime; notification denial must leave
Stop accessible. Process recreation, widget/tile upgrades, lifecycle, shared
permission integration and production screenshots require their own evidence.

Physical audio/tempo verification requires explicitly authorized user-device
access and remains an acceptance gate. Do not close #25 from emulator checks.

## Service/control layer

The service now owns one worker, uses elapsedRealtime for the UI origin, retains
that origin and pre-playback volume across same-boot sticky process recreation,
and ignores duplicate starts. Stop is an explicit command that releases audio
even while the screen is bound. Worker failure stops the foreground service;
cleanup is idempotent and does not overwrite a different later stream volume.
Overlapping Pulse/call volume leases are still a cross-feature integration gate.

The existing layout, font, circle/progress/clock and animation timings are kept.
The Kotlin Fragment has no static views or handlers, removes named service
listeners and scheduled renders on stop, and announces the toggle as a focusable
Start/Stop button. Notification navigation reaches metronome even when that tool
was deselected. Native widget and native/RN tile component names are preserved;
external controls use service state, not a cached tile boolean.

The controls layer changes only metronome mediaPlayback permission/type and RN
tile compatibility declarations in the manifest. #24 owns POST_NOTIFICATIONS,
listener/boot/background work, runInBG and the eventual target bump.

The permission follow-up imports #24's API-only commit `670c9d16`, then connects
foreground Start to that shared, recreation-safe prompt. Grant or declining the
notification request completes the original playback request; denial does not
remove CPR playback. Stop remains on the screen, widget and tiles. External
widget/tile starts do not attempt to request runtime permission from a service.
The helper's system-dialog dismissal follow-through remains owned by #24.

Controls validation: audio/control/notification suite 6/6 on API24 and API37 with
16-KB pages; real same-boot process-kill/restart preserves origin/volume on both;
local signed native-widget install-r fixture retains binding and functional
PendingIntent on both. RN's original ON_CLICK_TAG action is checked, but a full
RN signed-install widget/tile upgrade is not claimed. API37 denied notification
checks pass, including Hebrew/RTL/font1.5. Actual native/RN tile clicks synchronize
both tiles, including Stop without notification permission. Screenshots were
opened/inspected; the production layout XML is byte-identical to `b36f4a9c`.
Evidence and artifact hashes: `~/.local/share/opencode/evidence/metronome`.
