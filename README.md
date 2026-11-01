# דיווחי מד"א - MyMada

## Native Android development

MyMada is an Android-only emergency-notification and field-tools app for MDA
volunteers. The native restoration from `b36f4a9c` now has Kotlin application
sources and Compose feature screens, published through draft #118; implementation
is not release acceptance. See the [acceptance matrix](docs/FINAL_KOTLIN_INVENTORY.md),
[project](https://github.com/users/atlowChemi/projects/1)
and [baseline assessment](https://github.com/atlowChemi/MyMada/issues/19).

### Build and run locally

Requires JDK 17, Android SDK platform 37 (`platforms;android-37.0`), build-tools
37.0.0 and platform-tools. Use the checked-in Gradle 9.6.0 wrapper with AGP 9.5.0
and built-in Kotlin (compiler 2.4.21). Minimum Android is **API 24**, approved by
the maintainer; compile and target SDK are **37**. Device/behavior and signed-upgrade
acceptance remain tracked in #27; a target bump alone is not release readiness.
Java bytecode targets 17; built-in Kotlin inherits that target, so no separate
`jvmTarget` override is needed. JDK/bytecode versions do not set Android minSdk.
AGP's [compatibility table](https://developer.android.com/build/releases/agp-9-5-0-release-notes)
selects Gradle 9.6.0 and JDK 17; [built-in Kotlin](https://developer.android.com/build/migrate-to-built-in-kotlin)
replaces the old Kotlin Android plugin. Dependency versions live in the Gradle
files; [#14](https://github.com/atlowChemi/MyMada/issues/14) holds the historical
inventory and executed validation results.
Legacy resource compatibility flags remain in `gradle.properties`; their original
Java-switch rationale is historical. Removing build flags is outside this docs slice.
Set `JAVA_HOME` to JDK 17 and `ANDROID_HOME` to your SDK, or set `sdk.dir` in
an untracked `local.properties`. Install/accept SDK packages with Android Studio
or `sdkmanager` before building.

```sh
sh ./gradlew :app:assembleDebug --no-daemon
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -W -n atlow.chemi.mymada.debug/atlow.chemi.mymada.HomePage
```

Debug builds use Android's automatically generated local debug key; no private
key or signing environment variables are required. A debug installation signed
with a different key cannot be updated in place: use a fresh emulator for this
baseline. Release builds are unsigned until signing continuity is established in
[#11](https://github.com/atlowChemi/MyMada/issues/11). Version code `12828` is a
development value above the previous source version, not a verified Play upgrade
version. Confirm the published maximum before release.

The checked-in `app/google-services.json` retains the existing Firebase project
configuration. Disabling emulator Wi-Fi/mobile data alone did not reliably block
SDK traffic during validation. For offline smoke tests, use the disposable-emulator
firewall setup below before launching the app; integration tests require appropriate
backend configuration.

### Validation and known limitations

```sh
# Exact final-ui task set: local unsigned release, no mapping upload.
./gradlew :app:assembleDebug :app:assembleDebugAndroidTest :app:assembleRelease \
  :app:testDebugUnitTest :app:lintDebug :app:lintRelease \
  -x :app:uploadCrashlyticsMappingFileRelease \
  '-Dorg.gradle.jvmargs=-Xmx6g -XX:ActiveProcessorCount=2 -Dfile.encoding=UTF-8' --max-workers=2

# Disposable, rootable API 24+ emulator only; block production SDK egress.
export ANDROID_SERIAL=emulator-5962 # replace with your exclusively owned AVD
adb root
adb wait-for-device
adb shell 'iptables -I OUTPUT ! -o lo -j REJECT; ip6tables -I OUTPUT ! -o lo -j REJECT'
adb shell svc wifi disable
adb shell svc data disable
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w --no-window-animation \
  -e class atlow.chemi.mymada.RemainingBridgeTest \
  atlow.chemi.mymada.debug.test/androidx.test.runner.AndroidJUnitRunner
```

Actual release distribution/signing follows #11/#16; the above is local validation.
The final task set passed with 19 JVM tests and both lints. Select focused classes
with `-e class`, locales with `-e captureLanguage en` or `he`; preserve each suite's
fixture requirements. `MonitorLifecycleTest` requires a listener grant. Shared
notification permission tests exercise actual OS prompts separately; pregranted
route smoke is not permission-UX evidence. API37 instrumentation uses standard
`--no-window-animation` because a diagnosed system dim layer can drop injected taps.
Retain failed logs rather than widening timeouts. For call fixtures see
the [calls slice](app/src/main/java/atlow/chemi/mymada/calls/README.md), which documents
offline integrated route checks and remaining lifecycle/permission gates.

For native packaging, follow Android's
[official alignment checks](https://developer.android.com/guide/practices/page-sizes#elf-alignment):

```sh
# Bundle inspection is separate from the final-ui task set above; used in #14/#56.
./gradlew :app:bundleRelease -x :app:uploadCrashlyticsMappingFileRelease
"$ANDROID_HOME/build-tools/37.0.0/zipalign" -c -P 16 4 app/build/outputs/apk/debug/app-debug.apk
"$ANDROID_HOME/build-tools/37.0.0/zipalign" -c -P 16 4 app/build/outputs/apk/release/app-release-unsigned.apk
unzip app/build/outputs/bundle/release/app-release.aab 'base/lib/*' -d /path/to/empty-inspection-directory
# Repeat for each extracted arm64-v8a/x86_64 .so; every LOAD alignment must be >= 2**14.
"$ANDROID_HOME/ndk/28.2.13676358/toolchains/llvm/prebuilt/<host>/bin/llvm-objdump" -p /path/to/library.so | grep LOAD
```

Also inspect libraries extracted from both APKs (`lib/*`). For actual runtime
coverage, verify `adb shell getprop ro.build.version.sdk` and
`adb shell getconf PAGE_SIZE` (must be `16384` for the 16 KB run), then run the
instrumentation suite. Executed device results are in #14. No NDK is required
to compile app-owned code.

RN scalar/string-set preferences are normalized before Application locale attachment
and startup reads (`settings/PreferenceMigration`). This is a
development baseline, not a release-ready rollback. The RN build/deployment
workflow is removed; native CI follows working local modernization in #13.

### Architecture and compatibility

One app module contains feature packages (`home`, `about`, `privacy`, `settings`,
`calls`, `integrations`) plus retained installed Activity/Fragment/service names.
Compose owns screen content; Activities and Fragments own Android lifecycle and
entry points. ViewModels/SavedStateHandle/saveable state retain screen drafts;
real process-death evidence is distinct from Activity recreation.
`ToolsWindow` hosts existing feature Fragments; MokedPage retains ViewPager for
physical swipes and Fragment state, reversing physical page order in RTL.
WebView policy content and platform/system pickers remain intentional interop.

`settings/PreferenceMigration` normalizes native typed/RN string values before
locale/startup reads. SharedPreferences and installed key/component spellings
remain compatibility contracts. Calls route through OS-bound listeners and bounded
reconnect recovery; metronome audio/service state is shared with widget/tile controls.
SDK/platform work stays outside Compose content; activation requires backend
acknowledgment and chat identity remains pinned across delivery-token changes.
See [dispatch contracts](docs/DISPATCH_CONTRACTS.md),
[shell architecture](docs/tools-shell-compose.md), and
[SDK contracts](docs/SDK_VALIDATION.md).

Live validation requires maintainer-provided **nonproduction** Firebase configuration,
fixture accounts/data and console access; the checked-in configuration is not an
authorization to use production. Request those through the maintainer, not in a PR.
Debug demo-ad IDs are selected automatically; offline resource checks do not prove
ad delivery. Release APKs are optimized but unsigned; locally re-signing with a
debug key proves neither Play signing continuity nor an upgrade of a production-signed install.

The slice notes below preserve earlier evidence scope. Current completion and
superseding results are indexed in the acceptance matrix; historical caveats are
not a fresh backlog.

### Privacy Compose slice (#21)

The privacy feature lives in `app/src/main/java/atlow/chemi/mymada/privacy`.
Compose compiler **2.4.21** matches AGP's built-in Kotlin; stable Compose BOM
**2026.09.00** (UI/runtime **1.12.1**, Material 3 **1.4.0**), Activity **1.13.0**,
and Lifecycle **2.11.0** provide the UI, previews, and lifecycle integration.
Instrumentation pins stable Espresso **3.7.0** for current-platform input
injection compatibility. No Kotlin Android plugin is applied.

The installed `atlow.chemi.mymada.PrivacyActivity` component remains a Kotlin
compatibility wrapper, so home, about and existing explicit intents still work.
The Compose toolbar delegates detection/service/permission actions to the
shared Kotlin handler in `PrivacyDetectionActivity` and `DetectionControls`.
Settings navigation and both back actions
remain available. The hosted Hebrew/English policy documents and their links
remain in a WebView, with the existing local offline error document. Policy
language now follows the app's selected locale, including on API 24+.

The native acknowledgment contract is deliberately retained: `Settings.privacy`
becomes boolean `true` on page completion, **including the offline error page**;
there is no new consent button. RN used string `"true"`; persisted-type migration
remains #22. Changing whether an error counts as acknowledgment requires a
separate behavior decision. WebView navigation state is saved across activity
recreation; this is not a persistent cache of remote policy content.

Open `PrivacyPreview` (English and Hebrew/RTL at 1.5× font scale) and
`PrivacyLoadingPreview` in Android Studio. After applying the disposable-device
egress rules above, run the focused Kotlin/Compose instrumentation:

```sh
sh ./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=atlow.chemi.mymada.privacy.PrivacyScreenTest \
  --no-daemon
```

Both locales check the rendered title, RTL control order, offline HTML content,
local fixture/link rendering, acknowledgment, detection-off persistence,
recreation, settings navigation, toolbar back and system back. Tests use the
real manifest component and a local in-memory HTML fixture; no backend fixture
writes are needed. Detection enable/permission/service coverage, full process
restoration, screenshot baselines and app-wide accessibility remain follow-up
work in #15/#21/#24/#27.

Validated 2026-10-29: debug and optimized unsigned release assemble pass;
`lintDebug` reports 0 errors / 418 warnings. Focused instrumentation passes
2/2 tests on API 24 (4.829s) and 2/2 on API 37 with 16-KB pages (10.739s),
with emulator IPv4/IPv6 egress rejected. This slice does not claim optimized
release runtime, live hosted-policy availability, or target-37 compatibility.

### Home Compose slice (#21)

`home/HomeContent.kt` owns the scrollable two-column Home grid and inset-aware
toolbar. It reuses the original localized bitmap buttons, native rounded/shadowed
cards and background drawables; the 50-dp navy banner area remains visible offline,
matching the supplied production 12822 reference. At larger font settings, readable
text labels supplement the bitmap text. `HomePage` keeps its installed launcher identity and native onboarding,
locale-return and detection behavior; `home/HomeActions.kt` retains
the original dispatch gate, test-call branches, contact/about ads and destinations.
Tools still opens `UtillitiesAndTools`, preserving its selected-tool preferences.
Release ad IDs and Firebase configuration are retained; debug uses demo ad IDs.

Pending Home banner/interstitial loads use application context; interstitials also use a weak bridge callback.
The callback is created in a static scope so Java cannot implicitly capture the old
bridge/Activity across recreation. Offline requests otherwise retain destroyed Home
view trees and both locales' artwork until the 48-MiB API-24 heap is exhausted.
Run the full `HomeScreenTest` suite in one process on the stock heap; an isolated
About launch does not exercise the preceding onboarding/locale/recreation sequence.
`HomeMemoryTest` runs onboarding/locale return, then Hebrew and English recreation → Tools → Settings → About
with actual back navigation, and supports native master for matched comparisons.
Optional `dumpHeap=true` writes pre-About HPROFs for diagnosis; omit it for pass/fail
runs because dumping forces GC. Both tests require the disposable-device firewall.

Open `HomePreview` for English, Hebrew/RTL large text and landscape. With the
disposable-device firewall above applied **before launch**, run:

```sh
sh ./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=atlow.chemi.mymada.home.HomeScreenTest \
  --no-daemon
```

Tests seed native preferences and grant overlay access on the disposable device;
they exercise navigation/back, recreation, detection-off/resume, tool filtering,
onboarding routes, locale return, RTL, 1.5× text and system-bar bounds. They do not
validate live ads/backend delivery or enable the legacy detection service.
RN application-locale storage is imported before Home starts, preserving its priority
over stale `lng`. Native onboarding order (settings,
overlay, privacy) is retained pending a decision versus RN's privacy-first order.
Dialog/process restoration, full settings/tools migration and current-target
permission/service behavior remain #21/#24/#27 work.

Home onboarding/platform prompts now use `home/HomePrompt`, a Kotlin DialogFragment
with saved destination arguments and the existing native dialog style. Recreation
restores an open prompt once; a dismissed prompt stays dismissed for that Activity
instance. Fresh launches retain the existing settings → overlay → privacy order.
`HomeScreenTest` checks open/dismissed restoration in English and Hebrew; OS process
death and the action dialogs in `HomeActions` remain separate acceptance work.

Production-matched screenshots and current validation results are in #34/#35;
visual approval remains required. Local release validation uses
`-Dorg.gradle.jvmargs=-Xmx4g` and skips only `:app:uploadCrashlyticsMappingFileRelease`.

### About Compose slice (#21)

`about/AboutScreen.kt` owns the scrollable About UI; `InfoActivityKt` retains
the manifest component, app-selected locale, privacy interstitial/navigation
and existing detection handler until #24. The original localized version,
credits and disclaimer text is retained. The privacy action uses the original
localized image tile with an accessibility label. `AboutPreview` provides English and Hebrew/RTL large-text
previews; scroll position survives activity recreation.

After applying the disposable-emulator firewall above, run
`AboutScreenTest` with the same instrumentation command and class
`atlow.chemi.mymada.about.AboutScreenTest`. It covers both locales, content,
home entry, privacy/settings round trips, detection-off/resume state, toolbar
and system back, recreation, RTL order and accessible toolbar touch targets.
Also run with the emulator font scale set to 1.5 to check scrolling/recreation.
Executed results belong in the slice PR and #21; live ads and detection-enable
service/permission behavior remain integration work.

About/Privacy retain the published 56dp toolbar, artwork and native text metrics;
`ui/ScreenToolbar.kt` is shared by these two screens. Keep their Scaffold backgrounds
transparent to expose the existing window artwork. WebViews hosted by Compose must
set `MATCH_PARENT` layout parameters: otherwise their HTML viewport can shrink even
when the Compose view fills the screen. The focused tests check toolbar/tile geometry,
rendered mask/gradient pixels and the offline document's viewport/vertical positioning.
Compare actual English/Hebrew captures with the verified production reference in #35
at matching density and font scale; interaction tests alone do not establish visual parity.

The About tile is decoded at source resolution: Compose scales it to its bounds.
Density-decoding the 792×200 image at 420dpi creates a 2079×525 bitmap (4.37 MB
instead of 0.63 MB) per composition. Pending interstitial requests use application
context and a weak Activity callback so offline requests cannot retain destroyed
About view trees.

Run `atlow.chemi.mymada.about.AboutMemoryTest` on a firewalled API-24 emulator
with the stock `dalvik.vm.heapgrowthlimit=48m`. It exercises recreation,
Privacy/Settings returns and Home→About after repeated About creation. Optional
runner argument `dumpHeap=true` writes `files/before-home.hprof` for diagnosis;
omit it for regression runs because heap dumping forces GC. Verify the AVD name
and installed APK hash when comparing builds; use distinct emulator ports for
concurrent work.

### Contributing during modernization

Follow the [project's Execution order](https://github.com/users/atlowChemi/projects/1)
and the selected issue's prerequisites. Keep each PR focused on a complete,
reviewable change; include its local check results and distinguish build success
from runtime or integration verification. Use `Refs #N` for partial progress and
`Fixes #N` when all acceptance criteria for that issue are met. Merging requires
maintainer approval. Agent-specific workflow constraints are in [AGENTS.md](AGENTS.md).

Phone installation through a browser/file manager may be blocked by Play Protect
because this app declares notification-listener/accessibility capabilities. Passing
emulator tests does not establish browser-install compatibility. This distribution
limitation is tracked in #24; Play account/listing recovery is tracked in #9.

אפליקציה למתנדבי מדא, המסייעת בזיהוי אירועי חירום.

## התקנה

חפשו בחנות האפליקציות את "דיווחי מגן דוד אדום".
או פשוט [לחצו כאן](https://play.google.com/store/apps/details?id=atlow.chemi.mymada)!


## שימוש

פשוט להתקין, ולשנות את ההגדרות בהתאם למה שאתם אוהבים!

## תרומה לאפליקציה
ניתן לשנות קבצים, ולבצע עריכה - אך יש לפרט את משמעותה.
רצוי ליצור קשר במידה ויש בעיה באפליקציה ישירות מתוך האפליקציה.

נשמח שתציפו לחברים,ותדרגו ב5 כוכבים בחנות!

## מדיניות פרטיות
[מדיניות נמצאת כאן](https://www.freeprivacypolicy.com/privacy/view/29c6571350f6b515af9691bbccbce669)
