# Build and verify locally

Run `43180001381` passed 56/58 jobs. Listener/source, overlay, external audio and
metronome variants passed; both failures were dispatch integration assertions.
Home tests reset the shared device font scale to 1.0, contaminating the later
1.3-scale preview: cleanup now restores the incoming setting. Hebrew pager
gestures crossed editable form content and focused an input instead of paging;
the gesture now uses the bottom action area, retaining all tab/recreation
assertions. These corrections await runtime validation.

Superseded run `43168940596` was cancelled (12 passed, 5 failed, 41 cancelled),
not a completed suite. Monitor boot/update now waits for asynchronous status
delivery using its existing bounded await; logs showed enqueue at assertion time.
The harness now forwards its configured font scale to the dispatch preview's
effective-scale assertion. Hebrew pager navigation remains unresolved: the
recording shows the gesture focusing a form input and opening the IME rather
than changing tabs. Preserve that assertion pending gesture/input investigation.

Run `43168223822` passed 41/48 jobs, including monitor and overlay allowed/denied
on both APIs. API24 source setup timed out in `am start -W` before instrumentation:
the source Details Activity immediately finishes. Source broadcasts now explicitly
include stopped packages, avoiding that unnecessary Activity launch. API37 summary
fallback now uses a fresh ID: a malformed update to an already-valid source is
intentionally suppressed by the retained-content policy. Corrections await CI.

Remaining-variant coverage schedules metronome/call visuals as eight named JUnit
parameters (two screens × two locales × two orientations) on each API. Explicit
`screen`, `language`, and `landscape` arguments still narrow manual parity runs.
API37 notification delivery has separate granted/denied installs, all metronome
control methods are selected, and Hebrew integration/Oxygen jobs complement the
existing defaults. The API33+ permission test is no longer scheduled on API24
where every method skipped; its grant/deny API37 jobs remain. These additions
need remote validation before claiming complete variant coverage.

External-audio jobs copy the existing 4.7-second `alert.mp3` to shared Music
storage, request MediaStore indexing, and select that external content URI using
the opt-in `externalAudioFixture` setup. API24/API37 then run the existing
decode, playback-position and alarm-stream assertions with `externalAudio=true`.
This covers external-storage dispatch, not the picker UI or physical audibility.
Manual selected-audio runs remain available without the fixture argument.
Remote validation is pending.

Listener/source and overlay coverage adds twelve isolated API24/API37 jobs:
native-only, RN-only and both OS listeners with the offline `atlow.mymadaadmin`
source APK; monitor controls/component grants; and separate overlay allowed/denied
states. The source APK is built before emulator boot and installed only after
guest network isolation. `listenerFixture` and `overlayFixture` apply OS grants
inside instrumentation, after its force-stop, and assert the resulting state.
The source fixture has no network permission. Real reboot/kill acceptance remains
separate from these callback/lifecycle checks. First runtime validation is pending.

Run `43105329820` passed 34/36 jobs, including calls, recovery and all eight
permission jobs. API37 navigation selected the unfocused Activity root while
the native add-word dialog was open; its field assertion now targets `isDialog()`.
API24 settings lost `system_server` to SIGSEGV in `android.display`/`memcpy`
(null destination, 720×1280×4-byte copy). Explicit SwiftShader did not eliminate
the recurring guest OS crash. Original streamed logs and artifacts are retained.
API24 now trials ANGLE/SwiftShader (`swangle`), already used by the AOSP
comparison. This is an unverified graphics mitigation; screenshots, videos,
assertions and the API24 jobs remain required, with no instrumentation retries.

Run `43086485490` was superseded by a push, with four permission jobs passing
(deny, dismiss, Settings and listener Settings) before cancellation. Its genuine
navigation failure checked Oxygen before the asynchronous host transaction was
installed. The visual test now uses the existing bounded shell-condition wait
before its fragment assertion and rotations, preserving failure diagnostics.
Runtime verification is pending; cancelled jobs are not test failures or passes.

Notification permission coverage uses eight separate API37 actual16KB jobs:
fresh-install grant/deny/dismiss/Settings routes, listener Settings grant/revoke,
detection cancellation after recreation, and metronome grant/deny playback.
Each job starts with a fresh emulator/install; permission-changing methods are
selected individually so pregrants cannot contaminate an ungranted route.
`PERMISSION_ROUTE` supplies the existing instrumentation `permissionRoute`
argument. These new jobs require remote runtime validation.

The `call-regressions` CI domain selects `CallPopupComposeTest` and
`CallRouteTest` on API24 and API37 actual16KB emulators. `CallRecoveryTest`
runs in separate `call-recovery` jobs because listener revocation asynchronously
turns detection off and must not race the next routing fixture.
These offline checks cover popup/audio ownership, persisted recovery policy and
notification routing through the real app callback/receiver. Recovery grants and
revokes its own listener; route tests grant notification permission where needed.
They require no source APK. Real listener delivery, overlay lifecycle, fresh
notification-permission routes and MediaStore audio fixtures need separate jobs.
Run `43086318106` passed 24/26 jobs; both call jobs failed. Notification checks
now wait for replacement content as well as count, using one observed snapshot,
and count the fixture's untagged notifications rather than Android-generated
tagged aggregate summaries. API37 logs confirmed an aggregate summary alongside
the four retained notifications and a listener-setting change during Home's
test-call check. Runtime validation of these corrections is pending.

Run `43093518194` passed 24/28 jobs. API24 recovery and Google4KB Vitals passed
instrumentation but failed GitHub artifact uploads; API24 settings completed
instrumentation before the overall job timeout. API37 routing hit Android's
notification enqueue rate limit (logged rate 6.36825): replacement cancelled the
old notification and the OS shed the new one. Independent payload-variant checks
now wait one second between verified deliveries, preserving their content/source
assertions. This does not fix or validate burst delivery: loss under OS throttling
remains a production risk requiring separate resolution, not a fixture pass.

Graphics investigation: run `42926381398` captured API24 `system_server` crashing
in `android.display`/`memcpy`, and the API37 AOSP launcher waiting for its render
thread in `glGetProgramiv` over the emulator GLES pipe. CI now explicitly selects
SwiftShader on API24 (rather than automatic software-backend selection) and
ANGLE/SwiftShader on the AOSP comparison. This is a candidate mitigation requiring
runtime validation, not a confirmed app fix; original crash/ANR evidence is retained.

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

Document checks include concurrent rendering/page counts and rejection of non-PDF
replacements before native parsing. Android 7's failed `PdfRenderer` constructor
leaves a finalizer that closes PDFium again; a shared renderer lock cannot protect
against that finalizer. The header check rejects non-PDF responses, not all malformed
PDFs. Validate these paths on API24 as well as API37. Information-toolbar checks
launch asynchronously because Privacy's animated offline WebView can prevent the
main-queue idle condition required by `ActivityScenario.launch`.

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
Tests needing external audio or staged upgrade setup may
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
The smoke script selects the built-in test platform for every shard and passes
the global `android-test.apk-install-options` property via
`JAVA_TOOL_OPTIONS` to its worker JVMs. AGP9.5 sends device-qualified options, but
engine1.0.8 reads the global key; `connectedDebugAndroidTest` is not a Gradle `Test`
task. Verify `--no-streaming` in the logged `Installing with command:` lines, not
just in JVM arguments. Remove this bridge when AGP and the engine agree on the key.
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

CI runs `.github/scripts/download-test-fixtures.sh` on the host before builds and
emulator boot. Five public read-only Storage objects (three PDFs and both policies)
must match the pinned SHA256 values; changed/error responses fail setup. The
ignored `app/src/androidTest/assets/ci-fixtures/` directory is packaged only in the
test APK. `retainedFixture` copies these to the app's external test files when no
local fixture exists. No production application asset or emulator networking is
added. The policy hashes match the previously accepted authentic fixtures.
Dedicated API24/API37 document-policy jobs exercise this path.

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

The CI `tools-and-dictionary` and `home-and-integrations` jobs select whole test
packages through AndroidJUnitRunner's `package` argument on API24 and API37
(actual 16KB). Each invocation has a 20-minute test timeout. These broaden screen
coverage beyond the diagnostic classes; they do not cover the separate settings,
calls, permission, external-audio or historical-upgrade prerequisites below.

`PlatformSmokeTest` seeds its oxygen-tool selection and grants overlay access before
navigation. `ChatDialogGeometryTest` supplies the app's XML theme as well as Compose
MaterialTheme because the rename field embeds a native Material text input.
The audio stop/write check waits for the writer's actual full-buffer sleep before
stopping it; thread startup alone does not establish that PCM encoding has finished.
The notification/widget Stop check explicitly grants notification permission on
API33+ and asserts access rather than skipping after a permission-denial test.
The recreation/background Stop check waits for the recreated Activity to stop after
Home and regain resumed/window-focus state after returning. Fixed 300/600ms sleeps
did not establish those prerequisites on API24; accessibility Stop remains required.
Run `42539403774` still failed accessible Stop after these waits. `MetronomeTest`
logs now retain trees before/after recreation and after focused return to locate
the unexpected open drawer; the failure is not waived or auto-dismissed.
The recreated screen must also have window focus and a laid-out clock before Home
is pressed: run `42549862874` reached RESUMED with no active accessibility root.
About artwork/navigation checks decline the permission prompt caused by a stale
enabled detection preference without OS access, then wait for window focus. A
dimmed permission dialog is not a valid sample of the underlying artwork.

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

`ContractionPolicyCheck` is a JUnit test and runs with `:app:testDebugUnitTest`.
The settings/history domain runs the settings package except separately staged
startup, isolated sound-settings, and external-audio fixture checks. Navigation/
compatibility runs the root-package drawer, visual, entry-point and receiver tests.
Sound-settings runs on fresh emulators so its denied-permission prerequisite is
not contaminated by other domains. Expanded validation is still incomplete;
listener/overlay/permission and external-audio jobs remain
unfinished until their fixture setup is wired.

The first expanded run (`42999185238`) passed both sound jobs but exposed test
fixture defects: contraction navigation must drive its native recovery dialog
through Espresso and preserve the configured system font scale (including 1.3),
while the Settings status-bar capture requires overlay access before launch.
Corrections retain the dialog action, font-scale equality and pixel assertions.
An API24 document/policy timeout also left empty guest diagnostics; its cause is
unresolved and is not attributed to those fixture defects.
Run `43008928177` passed 23/24 jobs, including settings/history and both document
jobs. The remaining API37 tablet Back test stayed in portrait despite a user
rotation lock. Its fixture now enables fixed-to-user rotation while testing the
landscape tablet and restores the platform default afterward; landscape, tablet
drawer visibility and Back-exit assertions remain required.
Run `43016477054` passed the tablet case and 23/24 jobs. Pulse navigation failed
immediately after resuming/recreating the backgrounded host because its shell
node was not yet available. The shared navigation helper now waits for a visible
navigation button or permanent drawer, retaining a bounded failure and window
diagnostics. Timer/deadline/beep assertions are unchanged.

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
