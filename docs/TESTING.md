# Build and verify locally

Use the [README toolchain](../README.md#development). These commands describe
reproduction, not a claim that every task or fixture has run on the current tree.
Keep build success, executed runtime checks, visual inspection and live-service
evidence distinct. Historical receipts remain in owning GitHub issues/PRs and
external archives; this guide does not duplicate their run chronology.

## Build and lint

```sh
JAVA_HOME="<JDK17>" ANDROID_HOME="<SDK37>" ./gradlew \
  :app:assembleDebug :app:assembleDebugAndroidTest \
  --max-workers=2 '-Dorg.gradle.jvmargs=-Xmx3g'

./gradlew :app:testDebugUnitTest :app:lintDebug :app:lintRelease \
  --max-workers=2 '-Dorg.gradle.jvmargs=-Xmx3g'

# Local optimized outputs; exclude the network mapping-upload task.
./gradlew :app:assembleRelease :app:bundleRelease \
  -x :app:uploadCrashlyticsMappingFileRelease \
  --max-workers=2 '-Dorg.gradle.jvmargs=-Xmx4g'
```

Export the same `JAVA_HOME`/`ANDROID_HOME` for subsequent commands. A larger local
R8 heap can be supplied without changing project configuration. Release APKs are
unsigned; externally debug-signing a copy permits disposable-device smoke only.
It proves neither Play signing continuity nor production upgrade compatibility.

Tests use JUnit4.13.2, AndroidX runner1.7.0/ext-junit1.3.0, Espresso3.7.0,
Compose BOM2026.09.00 `ui-test-junit4`/debug `ui-test-manifest`, and test DataStore1.1.7.

## CI build

`Android build` runs on pull requests (including forks) and pushes to `master`.
It uses Ubuntu 24.04, Temurin 17.0.17+10, Android command-line tools 16111833 (23.0),
platform `android-37.0`, build-tools 37.0.0 and the checked-in Gradle wrapper.
Reproduce its build after installing those tools:

```sh
./gradlew :app:assembleDebug :app:assembleDebugAndroidTest \
  --no-daemon --build-cache '-Dorg.gradle.jvmargs=-Xmx3g'
```

Download both APKs from the run's `debug-apks-<SHA>` artifact
(retained two days). These are disposable debug-signed builds, not releases.

The Google Services plugin reads checked-in `app/google-services.json` to generate
build resources; no Firebase credentials or GitHub secrets are supplied. Those
public client identifiers are **production-connected configuration**, not an offline
backend fixture. The build job only compiles/packages; the emulator jobs below run
instrumentation with network isolation. Apply the isolation rules
below before running downloaded APKs; demo ad IDs alone do not isolate Firebase.

The workflow has read-only repository access, does not persist checkout credentials,
and never uses `pull_request_target`. Each run can save the Gradle-managed cache;
GitHub scopes PR caches to their merge ref, allowing retries to reuse them without
exposing them to `master` or other PRs. Gradle uses the runner's available CPU count
rather than the local two-worker limit. No signing keys, `gradle.properties` credentials, Android
user directory or configuration-cache encryption secrets are cached. Superseded
runs are cancelled. No Gradle tasks are excluded: release signing and Crashlytics
mapping-upload configuration remain unchanged; this workflow requests debug tasks only.
The next step runs `:app:testDebugUnitTest :app:lintDebug :app:lintRelease` with
`--continue --no-daemon --build-cache '-Dorg.gradle.jvmargs=-Xmx3g'`. Gradle's
`--continue` collects independent task failures; it does not turn them into success.
Tests and lint errors fail the job; existing lint warnings remain warnings (no new
baseline or blanket suppression). Emulator execution uses separate jobs below.

`mikepenz/action-junit-report` renders JUnit results and failure details in the job
summary and emits annotations without Checks API writes or PR comments, keeping
fork permissions read-only. Missing/malformed results or failed tests fail reporting.
The two-day `quality-reports-<SHA>` artifact contains JUnit XML, the browsable
Gradle test HTML report and native lint HTML/XML. Reports and built APKs upload even
after failures, but not cancellation; an early build failure may leave no reports.
Download/unzip the artifact and open `index.html` for tests or `lint-results-*.html`
for lint. Lint is not converted to JUnit. No live backend or emulator is used here.

## CI emulator smoke

After build/tests/lint pass, two disposable Linux runners exercise API24 and API37
Google APIs x86_64 images (Pixel 2 profile, 720×1280 at 320dpi, 2GB RAM,
requested 2GB userdata). Emulator
versions may enforce a larger userdata minimum: the disposable runner removes unused
preinstalled NDK/.NET tools and requires 10GiB free after image installation, before
boot. API24 uses font scale1.0; API37 uses1.3
and the `37.0;google_apis_ps16k` image, with a required `PAGE_SIZE=16384` check.
With `TEST_CLASSES` unset, the script runs all discovered instrumentation tests with
their default arguments. The original three-test smoke selector is removed.
Tests needing retained PDFs, policy HTML, external audio or staged upgrade setup may
fail or skip until those prerequisites are supplied; report those separately from
application regressions. Discovery does not exercise every argument-dependent route,
and an instrumentation crash can leave later tests unexecuted.

The current diagnostic matrix narrows the next run to API24 PlatformSmoke alone vs.
after the metronome classes, and fresh API37 runners for Audio, ChatDialogGeometry
and VitalsScreen. `TEST_CLASSES` records the exact selectors in each artifact;
focused invocations have a10-minute timeout. This compares fresh state with the
failed full-suite baseline; it is not full-suite coverage or automatic retries.
Dedicated StartupUpgrade jobs invoke `seed`, force-stop the app, then invoke
`verify` in a new test process. AGP retains the APKs/data and uses replace-install
for these phases; each phase's JUnit results are preserved separately. This tests
cold-start migration of the seeded RN preferences, not a Play-signed APK upgrade.
Widget reinstall jobs similarly run `createFixture` before `verifyFixture`, with
replace-install between phases. The same debug APK validates binding retention
across reinstall; historical pre-controls-to-current upgrade acceptance is separate.
AGP9.5 passes `android-test.apk-install-options[emulator-5554]`, but engine1.0.8
reads the unqualified `android-test.apk-install-options`. Staged jobs also supply
that worker property explicitly so API24 receives `-r`. Remove the workaround when
the AGP/engine property contract is fixed. Per-phase commands and verbose installer
logs are retained. Widget host views attach to the Activity content frame rather
than the fragment-only navigation container.

`.github/scripts/emulator-smoke.sh isolate` runs before emulator boot: emulator
processes use a dedicated group whose non-loopback IPv4/IPv6 traffic is rejected
by the host firewall. Android OUTPUT rules are also installed and checked before
app installation. The Gradle process retains dependency-download access. Never run
the isolation setup on a shared machine; it wraps the SDK emulator executable and
is intended only for the disposable GitHub runner.

Animations/spellchecking are disabled, rotation is portrait and font scale explicit;
tests select their own locales. The test command times out after sixty minutes without
automatic retries. APKs are built before boot; Gradle uses one worker and a 2GB heap
while the emulator runs to reduce resource contention. Gradle owns app installation.
Two-minute screenrecord segments start before instrumentation;
on failure, finalized MP4s, a screenshot and Android ANR traces join the logcat ring buffers, package/activity
state, selectors, SHA256 APK identities, source SHA and device facts in the two-day
`emulator-api<API>-<SHA>` artifact. JUnit XML and HTML results are retained on pass
and failure and reported through the same read-only JUnit action. Recorder failures
are logged and do not replace the original test exit status. Runner termination or
loss of ADB can prevent final capture; cancelled runs do not claim passing results.
Individual ADB diagnostic commands are bounded to30 seconds; live TestRunner logs
retain test progress even when instrumentation never produces JUnit XML.
Graphics use the emulator's supported `-gpu software` selection rather than the
deprecated `swiftshader_indirect` backend. Native tombstones accompany ANR traces
on failure to distinguish system-process crashes from application assertions.
API37 now tests explicit `-gpu swiftshader`, following Android's slow-emulator
troubleshooting guidance, rather than automatic software-backend selection.
`-accel on` requires working KVM. Host vmstat samples cover boot; memory/CPU
pressure and window diagnostics accompany results. Although the AVD requests
2GB RAM, current API37 images raise it to 4GB at startup; use logs for actual RAM.
SDK setup uses the pinned action to download tools, then a shell pipeline for
license answers: an early sdkmanager exit cannot crash Node with `EPIPE`, and a
failed sdkmanager command still fails the job.
The Vitals diagnostic preserves cold-boot logcat and performs one warm reboot
before instrumentation to distinguish first-boot initialization pressure. Host
network isolation survives reboot; guest firewall rules are installed afterward.
A fresh System UI ANR fails setup instead of producing misleading focus failures.
APKs are assembled before boot; the harness does not repeat the standalone
assemble invocation after boot. Public run `42534397767` still showed a Google16KB
KeyguardService ANR (SystemUI main thread: 0.58s executing, 20.68s scheduler wait),
so public runners/SwiftShader alone are not yet a verified boot-stability fix.
Failure cleanup requests an ART thread dump with SIGQUIT when the target process
still exists, including test timeouts that never produce an OS ANR. Process state
and the existing ANR directory/logcat evidence accompany that request.
A phase watchdog also captures ART traces/logcat 30 seconds before the test
deadline, before Gradle can terminate instrumentation. It is cancelled when the
phase finishes and does not retry tests or change their timeout/result.
This is a boot experiment, not a retry of failed tests or full-suite acceptance.
An additional Vitals job uses the API37 AOSP `default` image without bundled
Google services, with the same screen, memory, font scale and test selectors.
Its observed page size is recorded; it does not replace the required 16KB image.
This compares image configurations, not Google services independently of page size.
A third Vitals job uses API37 `google_apis` and asserts a 4096-byte page size.
Together these jobs compare AOSP 4KB, Google-services 4KB and Google-services 16KB
with identical test selectors, display settings and warm-boot preparation.

Boot/image provisioning logs live in the emulator action step. Device evidence
records actual page size when `getconf` exists; an image name alone does not prove
16KB runtime support. Physical audio/OEM checks and actual-PDF fixture distribution are
outside this initial smoke slice; their existing #99 requirements remain open.

## Isolate an owned emulator and select a test

Use an exclusively owned disposable rootable API24+ emulator. Establish host-side
network isolation before starting it and retain that protection over guest restarts.
Reject both guest IP families before any app launch; Wi-Fi/data toggles alone do
not reliably block SDK traffic. No production writes, SMS approval or actual FCM
sends are part of these local checks.

```sh
export SERIAL="<owned-emulator-serial>"
adb -s "$SERIAL" root
adb -s "$SERIAL" wait-for-device
adb -s "$SERIAL" shell 'iptables -I OUTPUT ! -o lo -j REJECT; ip6tables -I OUTPUT ! -o lo -j REJECT'
adb -s "$SERIAL" shell svc wifi disable
adb -s "$SERIAL" shell svc data disable
adb -s "$SERIAL" shell iptables -S OUTPUT
adb -s "$SERIAL" shell ip6tables -S OUTPUT
adb -s "$SERIAL" install -r app/build/outputs/apk/debug/app-debug.apk
adb -s "$SERIAL" install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk

adb -s "$SERIAL" shell am instrument -w -r --no-window-animation \
  -e class atlow.chemi.mymada.ToolsBackNavigationTest,atlow.chemi.mymada.settings.SettingsNavigationTest \
  atlow.chemi.mymada.debug.test/androidx.test.runner.AndroidJUnitRunner
```

Use a fully qualified class or `Class#method`, not the full connected suite to
select one check. Shell suites exercise drawer/navigation; child tests entering
`displayFragment` are not equivalent. Shell tags are `shell-navigation`,
`shell-detection`, `shell-destination-<resource ID>` and `shell-action-<resource ID>`.
`--no-window-animation` avoids known API37 invisible Dim Layer touch interference;
it does not establish animated-window visual acceptance. Do not compensate with
application sleeps or relaxed assertions. Use each suite's own locale/fixture args.

For launch-only smoke after isolation/install:

```sh
adb -s "$SERIAL" shell am start -W -n atlow.chemi.mymada.debug/atlow.chemi.mymada.HomePage
```

Record source/artifact identity and restore original APKs, app data, permissions,
locale, font/animation settings and isolation after shared-fixture use. Keep failed
attempts distinct. Existing SHA-verified hardlinked archives are immutable: write
fresh filenames or atomically replace new working outputs, never edit archive
inodes in place. Avoid full AVD copies and remove transient rollback snapshots
after verified restoration.

## Local client fixtures and their limits

Existing injection seams drive real client models; no Firebase Emulator Suite,
new dependencies or release mock toggle is required. Install mocks **before** the
operation. Keep host/guest isolation even when callbacks are fake.

| Production boundary | Existing local substitute |
| --- | --- |
| Database activation/code writes | `ActivationBackend`, controlled token/request/activation Tasks |
| Database chat/listeners/writes | `ChatBackend`, controlled identity/listener/send/name Tasks |
| Firestore dictionary | `DictionarySource` snapshot/submission callbacks |
| Database Vitals | `remoteVitalRange` parsing and `VitalsScreen` range-state input; SDK listener mapping is separate |
| Storage documents | `DocumentStore.load(download=...)`, local actual PDFs and retained authorized download evidence |
| FCM | Fake token Tasks and local Intent/routing checks, not push delivery |
| Ads | `HomeActions` initialization/load function parameters and fake interstitial callbacks; SDK defaults remain unchanged |

There are no repository Firebase emulator/security-rule files. Fake completion
does not establish server acknowledgment, deployed rules, token provisioning,
persistence across queued-write process death, authorization, moderation or FCM
delivery. Pinned chat identity does not prove token-to-conversation server mapping.
Backend messages/SeenByAdmin are not a local unread counter to seed.

`FirebaseClientLocalAcceptanceTest` uses TaskCompletionSource for token failure/
retry, acknowledgment-gated identity, typed user/admin/WhatsApp/seen events,
malformed-event rejection, pending duplicate suppression/recreation, rejected-send
draft retention, retry acknowledgment and listener cleanup. Existing
`ActivationContractTest`/`ActivationIntegrationTest` and
`ChatContractTest`/`ChatIntegrationTest` cover related deferred-Task protocols.

**Chat visual limitation:** passing client state/semantics does not prove that
the error window disappeared. An independent ADB capture and WindowManager dump
confirmed a visible API24 **EXITING dialog surface**, not a cached bitmap, at the
retry acknowledgment frame. Those images are excluded from visual-dismissal
acceptance. The originating lifecycle/window-removal cause remains unproved;
the proposed wait-before-recreation hypothesis is not a verified fix. State PASS
remains valid within its scope. No further patch/rerun is implied by this guide.

Medical local fixtures distinguish Vitals parsing/rebinding from actual SDK
delivery, and Dictionary fake pending/rejection/retry/success from backend outcomes.
Test-ad IDs and local navigation callbacks do not prove ad delivery, impressions
or uploaded Analytics/Crashlytics/Performance events. End-to-end checks require
separately authorized nonproduction services/accounts; checked-in configuration
does not authorize production use or require a new manual-phone matrix.

## Privacy and document content

Synthetic `PrivacyScreenTest#localLoadedPolicyScrollsAndNavigatesWithinTheRealHost`
has **no external-file prerequisite**. It exercises real PrivacyActivity/WebView
scrolling, local navigation, offline error and recreation using synthetic HTML.

Authentic `retainedAuthenticPoliciesRenderAndScrollInBothLocales` requires exact
retained EN/HE HTML; its SHA checks reject substitutes. Obtain the authorized
`policy-en.html` and `policy-he.html` from the retained authentic-content fixture,
not a newly fetched or edited substitute. Keep the emulator externally REJECTed:

```sh
export POLICY_FIXTURES="<directory-with-retained-exact-policy-files>"
adb -s "$SERIAL" shell mkdir -p /sdcard/Android/data/atlow.chemi.mymada.debug/files/
adb -s "$SERIAL" push "$POLICY_FIXTURES/policy-en.html" /sdcard/Android/data/atlow.chemi.mymada.debug/files/policy-en.html
adb -s "$SERIAL" push "$POLICY_FIXTURES/policy-he.html" /sdcard/Android/data/atlow.chemi.mymada.debug/files/policy-he.html
adb -s "$SERIAL" shell am instrument -w -r \
  -e class atlow.chemi.mymada.privacy.PrivacyScreenTest#retainedAuthenticPoliciesRenderAndScrollInBothLocales \
  atlow.chemi.mymada.debug.test/androidx.test.runner.AndroidJUnitRunner
```

The retained content has successful host HTTP200/exact-byte evidence and actual
offline WebView EN/HE rendering/scrolling. This is distinct from device-online
transport and third-party CSS/font delivery. Build success alone proves neither.
The accepted historical Privacy/communications reference gaps stay disposed;
unknown old loading behavior does not invalidate the current indicator or reopen
completed fixes.

`DocumentsTest` actual-object checks require read-only actual files under
`/sdcard/Android/data/atlow.chemi.mymada.debug/files/live-documents/` and fail if
absent. Retained BLS/ALS/Kuntres files have 137/290/30 pages and cover first/middle/
last, offline rendering and zoom/pan; authorized actual Android Storage download
evidence is separate and need not be repeated merely because docs move. Do not
commit PDFs, tokens or authenticated responses. Rendering compatibility is not
 live SDK transport. Deferred viewer work is tracked in
[#145](https://github.com/atlowChemi/MyMada/issues/145).

## Focused fixture requirements

Dispatch pager gestures start/end at the inner quarter points, outside system Back
gesture edges. Run `42539403774` recorded a return-to-home Back navigation during
the old reverse swipe; tab-selection and recreation assertions remain required.
Firebase local-error assertions explicitly select the native dialog root. Home's
dispatch-choice test invokes the real dialog button on the main thread, avoiding
Espresso's post-click Compose-idle wait after navigation to the native lock screen;
the destination monitor, focus and actual Back-navigation checks remain intact.
The real offline document-artwork check uses main-thread callbacks and WebView's
visual-state callback rather than idle waits while `err.html` continuously animates
its ECG bar. The production animation remains enabled, including on API24.
Run `42580327819` captured the remaining API24 hang in `ActivityScenario.close()`:
its pre-close idle wait runs while the error artwork still animates. After the
artwork capture, the test finishes the Activity on main so Compose releases the
WebView before scenario teardown waits for idle.
Run `42584103354` attempt 2 completed all seven API24 document tests, including
the offline-artwork check in about 65 seconds. The combined job still timed out:
its ART trace now points to `PrivacyScreenTest.verifyScreen` calling
`ActivityScenario.onActivity`, and retained-policy loading also failed.
The same run exposed About's return-state query racing the intermediate RESTARTED
stage. About lifecycle assertions now sample on main between lifecycle transactions;
the requested state and timeout remain unchanged. Run `42590403416` showed that
`ActivityScenario.state` itself waits for idle and rejects main-thread access.
The corrected assertions query `ActivityLifecycleMonitor` on main for the exact
Activity instance (captured after recreation), including its DESTROYED stage.
Dictionary native dialog buttons, editors and caption assertions explicitly select
the dialog root after that run selected an unfocused base Activity window instead.
Both corrections await emulator verification.
Run `42599045842` verified About on both APIs and both tools/dictionary jobs.
Its Privacy trace still blocked inside `waitForBody`'s `onActivity` idle wait.
Privacy now tracks the current Activity across recreation, performs WebView reads
directly on main, waits for lifecycle stages, and finishes before scenario cleanup.
JavaScript body strings are JSON-decoded before matching, including escaped Hebrew.
The real animated artwork and all body/viewport assertions remain enabled.
That run also found the chat name dialog without window focus and Home's scrolled
contact node reporting not displayed before a later bounds query showed it inside
the viewport. Bounded focus/display waits now precede those assertions. These are
synchronization candidates awaiting verification, not accepted parity fixes.
The separate API24 Home prompt teardown failure remains unresolved.
Inspection of `42599045842` narrows that teardown failure: AndroidX launches
`EmptyActivity`, Home pauses at 10:36:09.816, and its finish request follows at
10:36:09.873. Home is not destroyed until the next Activity launch at
10:36:59.248, after the teardown deadline. The prompt-metrics test now finishes
the resumed Home directly and requires its exact instance to reach DESTROYED
before scenario close. This candidate fix is awaiting emulator verification.
Run `42681459354` passed both document/policy jobs and the API37 chat-focus check.
API24 Home did not execute: emulator package download failed during setup.
API37 home/integrations reached 57/58, failing the ad test's global lifecycle scan.
That scan included DESTROYED destination instances from preceding About tests.
The ad check now records destination PRE_ON_CREATE callbacks during its own actions,
requiring zero creations as well as the existing exact blocking-monitor hit counts.
Run `42686300456` passed the Home prompt teardown check and API37 home/integrations.
API24 Home reached 57/58: its large-text scroll still failed after five seconds.
Video shows the grid never leaving its top rows during that wait. Home test navigation
now uses the lazy grid's `performScrollToNode` rather than `performScrollTo` on an
offscreen item, then retains the displayed/bounds assertions. Verification is pending.
The Google-4KB Vitals comparison failed all three focus checks with a visible
"Pixel Launcher isn't responding" dialog; actual-16KB and AOSP Vitals passed.

`PlatformSmokeTest` seeds its oxygen-tool selection and grants overlay access before
navigation. `ChatDialogGeometryTest` supplies the app's XML theme as well as Compose
MaterialTheme because the rename field embeds a native Material text input.
The audio stop/write check waits for the writer's actual full-buffer sleep before
stopping it; thread startup alone does not establish that PCM encoding has finished.
The notification/widget Stop check explicitly grants notification permission on
API33+ and asserts access rather than skipping after a permission-denial test.

| Area | Selectors and nonobvious prerequisites |
| --- | --- |
| Home/memory | `HomeScreenTest` covers onboarding/locale/navigation; `HomeMemoryTest` checks Activity collection after the real sequence on the stock API24 heap. Optional `dumpHeap=true` is diagnostic, not normal pass/fail (it forces GC). An isolated About launch is insufficient. |
| Offline ads | `AdsLocalAcceptanceTest` injects initialization/loading before invoking real callbacks and checks banner-slot layout. API24 requires class-matching blocking destination monitors; an overridden no-argument monitor does not reliably intercept launches. This does not prove real SDK loading or fullscreen presentation. |
| Settings/upgrade startup | `StartupUpgradeTest`: separate `startupFixture=seed` and `startupFixture=verify` invocations with force-stop/new process between; reject IPv4/IPv6 before both. |
| Audio settings | `SoundSettingsTest` permission route starts denied; revoking permission during instrumentation kills the process. `ExternalAudioDispatchTest` uses `externalAudio=true` after selecting/granting a >1s MediaStore file; dispatch is not speaker audibility. |
| Permission | `NotificationPermissionTest` uses `permissionRoute=grant\|deny\|dismiss\|settings`, initially ungranted, no adb grant. Listener Settings route separately pregrants notifications but starts without listener access. `CallRouteTest` pregrants notifications and is not permission-UX proof. |
| Overlay/listener | `OverlayLifecycleTest` needs SYSTEM_ALERT_WINDOW except `deniedOverlayStopsWithoutWindows`; `MonitorLifecycleTest` needs listener grant. Interactive-window/node visibility limitations must not become empty passing assertions. |
| Recovery | `RecoveryWindowTest`/`CallRecoveryTest` cover freshness/persisted epochs; real OS reconnect requires the call-source fixture below, not direct callbacks alone. |
| Dictionary/Vitals | `DictionaryContractTest`/`DictionaryScreenTest` use in-memory source, optional `language=en\|he`, `capture=true`. `VitalsTest`/`VitalsScreenTest`/`MedicalLocalAcceptanceTest` distinguish parser/rendered binding from SDK traffic. |
| Calculators/history | APGAR/Glasgow scoring checks exhaust combinations. `PulseStateTest`/`PulseScreenTest` cover deadline continuation/no replay. `ContractionHistoryTest`, `ContractionRecoveryTest`, `ContractionNavigationTest` cover import/policy/navigation. |
| Dispatch/chat | Use blocking ACTION_VIEW/sms instrumentation monitor; never approve a real composer. Hide IME before each real pager swipe and use the unique active/focused app accessibility window. Native editor IME actions require real View input, not Compose-only semantics. |
| Metronome/popup | `MetronomeControlTest`, `MetronomeNotificationTest`, `MetronomePermissionTest`, `CallRouteTest`, `CallPopupComposeTest`. Wait for both playback and notification before Stop. `MetronomeCallVisualTest`: `screen=metro\|call`, `language=en\|he`, `landscape=true\|false`; set font scale externally. AudioTrack writes do not prove physical sound/tempo. |

For the standalone contraction policy check, compile
`:app:compileDebugUnitTestKotlin`, then run
`atlow.chemi.mymada.contractions.ContractionPolicyCheck` with debug/test compiled
classes and Kotlin stdlib on the JVM classpath.

Build the offline notification source fixture:

```sh
./gradlew -p app/src/androidTest/fixtures/call-source assembleDebug
```

Its package `atlow.mymadaadmin` has no INTERNET permission. `SummaryNotificationTest`
uses `listeners=<full-component[:full-component]>`, source notification permission
on API33+, target overlay grant and initially cleared listener grants. Grant after
instrumentation starts and revoke before teardown; force-stopping a pending API24
bind can leave stale system_server registration. Native-only, RN-only and both
grants are distinct cases.

## Packaging, upgrades and acceptance boundaries

Use Android's [official alignment checks](https://developer.android.com/guide/practices/page-sizes#elf-alignment).
No NDK is needed to compile app-owned code; inspection of packaged libraries is separate:

```sh
"$ANDROID_HOME/build-tools/37.0.0/zipalign" -c -P 16 4 app/build/outputs/apk/debug/app-debug.apk
"$ANDROID_HOME/build-tools/37.0.0/zipalign" -c -P 16 4 app/build/outputs/apk/release/app-release-unsigned.apk
unzip app/build/outputs/bundle/release/app-release.aab 'base/lib/*' -d "<empty-inspection-directory>"
# Repeat for each arm64-v8a/x86_64 library: every LOAD alignment >= 2**14.
"$ANDROID_HOME/ndk/28.2.13676358/toolchains/llvm/prebuilt/<host>/bin/llvm-objdump" -p "<library.so>" | grep LOAD
adb -s "$SERIAL" shell getprop ro.build.version.sdk
adb -s "$SERIAL" shell getconf PAGE_SIZE
```

Inspect libraries from both APKs (`lib/*`) too. A 16KB runtime claim requires
actual `PAGE_SIZE=16384`, not merely targetSdk37 or packaging success. API24 may
lack `getconf`; do not label it a verified 16KB device. Agreed local coverage uses
API24 and API37; a new API33–36 matrix is not implied. If a newly created private
API37 AVD incorrectly records `target=android-0`, correct only its own ini target
to android-37 rather than resetting shared ADB/emulators.

For local upgrades, use known baseline provenance and the **same local signer**.
Reset/uninstall only before baseline setup; measured `install -r` intervals must
retain UID, data, listener grant and actual widget binding. Compare pulled installed
bytes, settings/original backups/history/reopen, activation/name/chat identity,
ringtone/region/tools and explicit entry-point/widget actions. Identify seeded RN
fixtures separately from UI-authored history. Never fabricate backend unread data.
Actual Play/production-signed upgrade remains deferred to release readiness #11.

Activity recreation, SavedStateHandle rehydration, actual process death and reboot
are separate checks. Unsent draft restoration does not prove pending-write recovery;
app-process death does not prove picker-process recovery. Match visual comparisons
by source/artifact, state, locale, IME, orientation and effective font scale. Open
the images: semantics/bounds alone do not prove painting or window removal.
Native API24 Hebrew references may require actual system Hebrew; changing `lng`
alone can leave English strings. Respect approved scaling/rounding policy and
scoped results; candidate-only images are not production parity.

Hardware audibility/tempo, TalkBack with OEM keyboard, audio contention and prolonged
idle/screen-off/reboot/regrant behavior require separately authorized physical
checks. Live-service receipt, rules, token rotation and queue recovery need their
own nonproduction fixtures. These boundaries preserve evidence scope, not a new
roadmap or blanket implementation pause.
