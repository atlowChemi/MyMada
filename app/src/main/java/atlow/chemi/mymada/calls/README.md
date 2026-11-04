# Call ingestion / decisions — #24

Base: published #53, `6cc97c09`. This slice replaces the native listener's parsing
and the duplicated receiver/popup audio decisions with Kotlin. `GrobootRec` and
`smsRes` call the same policy; they remain temporary Java platform/UI bridges.

## Actual route and ownership

`NotificationService.onNotificationPosted` → `CallParser` → local `MADA_APP`
broadcast → Application-registered `GrobootRec` → call notification, `smsRes`
popup, or `FloatingWidgetService`. Each listener broadcast carries its own source
PendingIntent, including null, so queued calls cannot steal each other's details
action. Home test calls use the second registered local action and existing
synthetic text/type; the legacy accessibility receiver emits a global broadcast
and is not connected to this local receiver. Its retirement belongs to the
background/component slice, after grant/entry-point review.

Receiver and popup both use `CallAlerts`; overlay consumes the receiver's resolved
audio extras. Existing platform code still owns loop/auto-stop/volume, notification
accumulate/replace, dictation and source-app actions. No new settings store or data
migration. Settings are normalized by `PreferenceMigration` before Application
startup. Current Home/Settings UI and installed listener class name are retained.

## Explicit compatibility, not new product policy

- Source families still use substring matching for `mdaemergency` / `mda.health`;
  admin is exactly `atlow.mymadaadmin`. Narrowing the families requires a verified
  installed-package inventory. Unknown non-null titles are ignored; null title
  defaults to on-call and null text retains the native error label.
- Native event precedence: service → administrative → on-call → group → shift →
  legacy → on-call fallback. Administrative-title legacy overrides service.
  RN puts legacy first and accepts unfamiliar titles. Approval remains open.
- Old syntax uses T3, on-call T1, group T2, shift T3. Service has independent T6
  enablement but shares T4 audio. Unsplit ordinary calls use general audio and
  ignore per-type enablement. RN's always-truthy group branch is not ported.
- Native inclusive night test (`hour <= end || hour >= start`) and nine-state
  combined `sleep` suppression are retained. RN separate day/night values may
  disagree with combined sleep, and its OR-assignment cannot suppress enabled
  audio. Settings already writes both on edits; this slice does not choose a new
  precedence for pre-existing conflicting values or daytime-spanning windows.
- Native `statVib` differs by output: popup permits non-silent, overlay permits
  silent; RN permits vibrate-only. Both native results are named and tested until
  the owner chooses the intended rule. Sound respects normal mode when `stat`.
- Repeat source callbacks, including identical keys/text, remain distinct alerts.
  No guessed time-window/text deduplication can silently discard a real call.
  Accumulate/replace follows existing `ride`; malformed/duplicate fixtures are
  synthetic and contain no patient/address/code data.

## History reviewed

Compared native `b36f4a9c` and RN `be72a163`, plus actual diffs:
`43cba244` correct foreground action (retain semantics in service slice);
`dd1501d4`/`2346da53` parser/headless rewrite (native execution replaces JS;
classification conflicts above); `f89a00d9` context ownership/compat (no new static
Context; platform compat review remains); `18a7fcbd` service lookup after attachment
and safe headless args (typed native input; localization already owned by #22);
`91ed29ef` null-safe cold/warm activity extras (RN delegate obsolete; native
popup/process-state routing still needs migration); `bf09f676` initialized audio
manager (retained before decision/playback).

## Validation and remaining boundary

`testDebugUnitTest` covers sanitized formats, precedence, rejected/null inputs,
repeats, T1/T3 regression, independent enablement, all nine day/night states,
boundaries and all ringer modes. `CallRouteTest` invokes the real listener callback
and Application receiver, checks posted notification text/actions, queued source
identity, repeated keys, replace mode, filters and missing payloads. It uses a
disposable firewalled emulator with notification permission granted; this is NOT
notification-permission UX, OS listener binding/grant or lifecycle certification.

Run the focused route class with `am instrument --no-window-animation -w -e class
atlow.chemi.mymada.calls.CallRouteTest` and the debug test runner. Build debug,
optimized release, instrumentation and lint; skip only Crashlytics mapping upload
via the local command line. Runtime evidence lives in
`~/.local/share/opencode/evidence/calls` and the PR.

## Popup-state layer

`CallEvent` carries type/text/source PendingIntent in the activity launch Intent.
The receiver no longer writes static `smsRes` text/action slots. Cold starts and
recreation read the intent; singleTop warm delivery replaces the intent and
recreates the existing native layout. Old auto-stop callbacks are canceled on
destruction. Popup playback releases even after prepare failure or completion,
and restores alarm volume only when that popup changed it. Overlay playback still
needs the shared lifecycle/audio migration; this does not claim process-death or
background-launch certification.

Additional route tests cover popup cold/warm listener delivery, recreation,
superseded auto-stop, actual details PendingIntent delivery and volume restoration
after failed playback/recreation/close. UI resources and display parsing remain
native pending the production-matched Kotlin UI slice.

Subsequent #24 layers own process-death verification, shared audio cleanup, listener grants
for both native and RN identities, monitoring/boot/permission/overlay restrictions
and Kotlin replacement of remaining bridges. Shared `ActionReceiver`, manifest
and foreground plumbing need #25 coordination. Target stays 33 / compile37 until
#25 service work and #21 latest-target UI verification are complete. Production
UI acceptance, signed grant upgrades and actual-device audibility remain open.
