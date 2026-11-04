# Remaining installed-component bridges (#27)

Private language-only slice based on published #106
(`7e4a0501b3debef530cbee0bc8171ecf3eaf2509`).

## Contracts retained

- `atlow.chemi.mymada.PrivacyActivity` remains an open, public, no-argument
  subclass of `atlow.chemi.mymada.privacy.PrivacyActivity`. The manifest component,
  theme, parent metadata, Home/About launchers, and analytics type check are
  unchanged. This is a real installed activity class, not a removable alias.
- `atlow.chemi.mymada.recieversAndServices.ActionReceiver` remains the manifest
  receiver and PendingIntent destination. It reads the `"action"` extra, not
  `Intent.action`: `widget` toggles Metro; `stopM` stops Metro; `runInBG` delegates
  to `DetectionStatus.stop`; missing/unknown extras do nothing.
- Only widget-start exceptions are caught, retaining log tag `MetroWidget` and
  message `Control unavailable`. Stop exceptions still propagate. The unused
  local service Intent was removed with the Metro owner's approval.
- Metro's running-stop path still sends `metronome.stop`; its inactive-stop path
  still calls `stopService`. Detection stop still clears recovery state, disables
  `Settings.Mapp`, stops the legacy monitor, and cancels status notification 78.
- Widget request code 0, Metro stop request code 9, detection stop request code 13,
  channel `runInBg`, notification IDs 13/78, and legacy widget `ON_CLICK_TAG`
  forwarding are untouched. Whole-tree source/manifest/tests/reference searches
  found no additional dynamic class-name loading or XML construction to change.

Calls and Metro owners confirmed the shared receiver contract before editing.
No service, widget, calls implementation, manifest, or UI-owner file changed.

## Focused checks

`RemainingBridgeTest` checks the registered receiver identity, no-argument
construction, all extra routes and unknown/missing extras, legacy widget
forwarding, API-specific service start dispatch, exception behavior, and the
recovery/preference side effects of detection stop. Existing focused tests exercise
actual notification/widget PendingIntents, monitor cancellation, and the installed
Privacy activity's inherited screen/navigation/recreation behavior.

Evidence: `~/.local/share/opencode/evidence/remaining-bridges/`.

The existing focused checks are:

- `MetronomeControlTest#notificationAndWidgetActionsStopWhileUiBound`
- `MonitorLifecycleTest#repeatedStartAndNotificationStopCleanUpOnlyTheMonitor`
- `PrivacyScreenTest#englishPolicyNavigationAndState`

These passed (3/3 each) on owned emulator-5620 (API24) and emulator-5622
(API37, 16-KB pages). Both IPv4 and IPv6 OUTPUT reject non-loopback traffic;
the tests use no phone or production backend. JVM inspection confirms both public
no-argument constructors, original superclass identities, and receiver signature.
The added route test uses a recording ContextWrapper and separate test preference
files rather than starting services for its branch checks.

Build commands use the cached SDK/JDK17, `--no-daemon --max-workers=2`, and
`-Dorg.gradle.jvmargs='-Xmx6g -XX:ActiveProcessorCount=2 -Dfile.encoding=UTF-8'`.
Tasks are `assembleDebug assembleDebugAndroidTest assembleRelease lintDebug`,
with `-x :app:uploadCrashlyticsMappingFileRelease`; uploads are not validation.

All four build/lint tasks passed; final test-APK/lint recheck also passed
(0 errors, 314 warnings, 2 hints). `RemainingBridgeTest` passed on both API24
and API37: four focused runtime checks per API in total, with no skipped tests.
Installed debug APK SHA-256 on both devices matches the retained artifact:
`711c1008e2d6a581f451a6d79d0c118f8d992a144694b2b03353c93110958da8`.
Evidence retains debug/release APKs, the integration and final route test APKs,
hashes, build/lint output, JVM signatures, isolation rules, and runtime results.

## Remaining maintained Java

Six files remain at this base: `MokedPage`, `UtillitiesAndTools`,
`CircularAnimatedDrawable`, `CircularProgressButton`,
`CircularRevealAnimatedDrawable`, and `MainActivity1`. The #26 and shell owners
retain those files; zero maintained Java requires their integration and final audit.
