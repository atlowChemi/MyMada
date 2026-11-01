# Final-candidate local native/RN upgrades (#27)

Production-signed/Play upgrades remain explicitly deferred to release readiness
#11 while account access is locked. The following is local migration evidence,
not proof of Play certificate continuity, remote authorization or live receipts.

## Fixtures and measured boundary

Owned isolated emulators only: integration24/5962 and integration37/5960. Both
IPv4 and IPv6 non-loopback OUTPUT are rejected before launch. No physical phone,
live SMS/backend write, global ADB reset or merge.

- Native: supplied production12822 universal APK, SHA256
  `b08c3d812f4781cfc667edf4240c917350c8083ea91cdb95bc971e0524c09179`.
- RN: source `be72a163`, optimized Hermes12827 fixture with dummy Firebase config,
  SHA256 `3fb6bb6c3b28f4bd407715b05087eea0e0dba89eb793e002ba62dce67b8da25d`.
- Both use local signer SHA256
  `9c2b6863e7a135c9fe990e33a36e3b2fbc9a583e9ed5c4b04eb2825e9784ce8d`;
  candidate is the optimized release signed with the same local key.
- Reset/uninstall happens only during baseline setup. The measured interval is
  **install -r**, with unchanged UID, original data, OS listener grant and an actual
  bound widget ID/provider. Installed candidate bytes are pulled and hash-compared.

Native history was originally authored through the production UI in retained
history-upgrades/fixed/native-before.xml, then restored as a fixture here. RN history
and settings are seeded representative legacy strings, not RN UI-authored history.
Activation flag/key, name, prior FCM conversation token and region are seeded offline
values for both versions. These do not assert server approval. The separate external
widget-host fixture creates a real OS binding before upgrade and retains it afterward.

## Regression found by the final upgrade

Draft #120 passed its payload suites (picker/team **4/4 EN and HE on both APIs**).
Its RN upgrade then failed an actual explicit `MainReactActivity` launch: missing
component, `Error type 3`. Inspection also found the former `MadaCallActivity`
component missing. Original failures/artifacts remain immutable.

The follow-up uses Android activity aliases to retain these installed names without
restoring RN runtime. Home handles retained RN notification targets: Dispatch/Form,
ContactUs, ActivateCode (`firebaseKey`) and MetronomeActivity. The existing volunteer
gate and backend activation acknowledgement remain authoritative. The popup reads
RN numeric `callType` (including JS doubles), while native `callT` retains priority.
Aliases retain the historical exported status and add no duplicate launcher icon.

RN's source-action UUID indexed an in-memory PendingIntent map. That vanished process
state cannot survive an upgrade; no reconstruction or zero-loss promise is made.
New native events continue to carry their real source PendingIntent separately.

## Checks and limits

The executable external evidence harness captures baseline/candidate package data,
UI XML/screenshots, exact shared preferences before/after/reopen, history imports and
original backups, widget binding/control results and listener grant strings. It
asserts retained activation/name/conversation/ringtone/region/tools, history identity,
same UID and installed-byte equality. The changed alias/router has a focused
instrumentation check; historical push validation remains under the JVM suite.

Backend chat messages and SeenByAdmin receipts are not local preferences and cannot
be manufactured by seeding an unread counter: neither historical client has that
local schema. Offline name/identity preservation is checked; actual remote history,
receipt synchronization, live FCM and queued writes still require authorized
nonproduction fixtures. Existing fake-backend tests establish UI/contracts only.

Evidence:
`~/.local/share/opencode/evidence/m2-integration/approved-policy/` retains #120 tests
and the initial upgrade attempt; `../upgrade-entrypoints/` holds corrected artifacts,
checks and per-emulator/family upgrade evidence. See that run's RESULTS.md for exact
final hashes and totals rather than inferring success from this harness description.

## Final executed local results

- Native→candidate and RN→candidate **pass on API24 and API37 (four intervals)**.
  All33 native and22 RN fixture preference keys retain their values or convert to
  expected types with original backups; exact history rows/timestamps retain native
  67ms precision and RN active-record identity. RN's inherited Hebrew app locale
  correctly takes precedence over seeded `lng=en`, retaining the original backup.
- All four intervals preserve an actually granted listener component and widget
  ID/provider. Actual widget taps start foreground metronome playback and stop it.
- All four RN notification targets also reach the correct resumed Activity on the
  optimized release on both APIs, with historical NEW_TASK|CLEAR_TASK flags.
- Final local-signed optimized release SHA256:
  `b09d8d706cfd2a75f257636e363469b454c573082509529ee8af129b85e0afa0`.
  Every measured interval pulled and compared the installed APK to these bytes.
- One build for the compatibility correction passed debug/test/release, both lints
  and20 JVM tests (2m1s). The previous #120 build was not repeated unchanged.
- New alias/route instrumentation plus existing activation/chat regressions passed
  **10/10 each** on API24 (13.883s) and API37 (13.428s).

Excluded setup attempts remain: old RN's API37 16KB compatibility dialog obscured
captures; stale host activity restarted after a baseline reset; direct API37 secure
setting writes were overwritten rather than granting listener access. Final setup
acknowledges the baseline dialog, stops the fixture host before resets and uses
`cmd notification allow_listener` on API37 with explicit before/after presence checks.
The candidate required no workaround for those fixture errors. API24 uses its
supported secure-setting mechanism. Resets precede, never occur within, install-r.
