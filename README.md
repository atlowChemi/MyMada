# דיווחי מד"א - MyMada

## Native Android development

This branch restores the native source from `b36f4a9c` (`v3.5.7`) for
incremental Kotlin/Compose modernization. See the [project](https://github.com/users/atlowChemi/projects/1)
and [baseline assessment](https://github.com/atlowChemi/MyMada/issues/19).

### Build and run locally

Requires JDK 17, Android SDK platform 37 (`platforms;android-37.0`), build-tools
37.0.0 and platform-tools. Use the checked-in Gradle 9.6.0 wrapper with AGP 9.5.0
and built-in Kotlin (compiler 2.4.21). Minimum Android is **API 24**, approved by
the maintainer; target 33 remains a temporary development hold for #21/#24/#25,
not Play-submission compatibility. Device/behavior verification is tracked in #27.
Java bytecode targets 17; built-in Kotlin inherits that target, so no separate
`jvmTarget` override is needed. JDK/bytecode versions do not set Android minSdk.
AGP's [compatibility table](https://developer.android.com/build/releases/agp-9-5-0-release-notes)
selects Gradle 9.6.0 and JDK 17; [built-in Kotlin](https://developer.android.com/build/migrate-to-built-in-kotlin)
replaces the old Kotlin Android plugin. Dependency versions live in the Gradle
files; [#14](https://github.com/atlowChemi/MyMada/issues/14) holds the historical
inventory and executed validation results.
Legacy Java resource-ID switches require `nonFinalResIds=false` and
`enableAppCompileTimeRClass=false`, with legacy resource shrinking
(`android.r8.optimizedResourceShrinking=false`). Migrate those handlers before
AGP 10 removes the final-ID compatibility option.
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
# Unsigned LOCAL validation only: skip network upload, never lint.
sh ./gradlew clean :app:assembleDebug :app:assembleRelease :app:bundleRelease :app:lintDebug \
  -x :app:uploadCrashlyticsMappingFileRelease --no-daemon

# Disposable, rootable API 24+ emulator only; block production SDK egress.
adb root
adb wait-for-device
adb shell 'iptables -I OUTPUT ! -o lo -j REJECT; ip6tables -I OUTPUT ! -o lo -j REJECT'
adb shell svc wifi disable
adb shell svc data disable
sh ./gradlew :app:connectedDebugAndroidTest --no-daemon
```

Actual releases retain Crashlytics mapping uploads; omit the local `-x` flag for
the release workflow. Kotlin instrumentation covers home/settings/navigation,
oxygen calculation, metronome UI/notification controls and a DataStore native
shared-counter round trip. On API 33+, it pre-grants notification permission;
to check denied-permission UI start/stop separately, revoke POST_NOTIFICATIONS
on the disposable test install and run `MetronomeNotificationTest` with runner
argument `grantNotifications=false`. Neither mode verifies permission-request UX
or audio tempo. There are no JVM unit tests yet (`testDebugUnitTest` is `NO-SOURCE`).

For native packaging, follow Android's
[official alignment checks](https://developer.android.com/guide/practices/page-sizes#elf-alignment):

```sh
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

RN string preferences can crash startup (#22). This is a
development baseline, not a release-ready rollback. The RN build/deployment
workflow is removed; native CI follows working local modernization in #13.

Documentation will expand alongside implementation in #28.

### Privacy Compose slice (#21)

The privacy feature lives in `app/src/main/java/atlow/chemi/mymada/privacy`.
Compose compiler **2.4.21** matches AGP's built-in Kotlin; stable Compose BOM
**2026.09.00** (UI/runtime **1.12.1**, Material 3 **1.4.0**), Activity **1.13.0**,
and Lifecycle **2.11.0** provide the UI, previews, and lifecycle integration.
Instrumentation pins stable Espresso **3.7.0** for current-platform input
injection compatibility. No Kotlin Android plugin is applied.

The installed `atlow.chemi.mymada.PrivacyActivity` component remains a Java
compatibility wrapper, so home, about and existing explicit intents still work.
The Compose toolbar delegates detection/service/permission actions to the
unchanged Java handler in `PrivacyDetectionActivity`; migrate this bridge with
the shared detection work in #24. Settings navigation and both back actions
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
locale-return and detection behavior; `home/HomeActions.java` temporarily retains
the original dispatch gate, test-call branches, contact/about ads and destinations.
Tools still opens `UtillitiesAndTools`, preserving its selected-tool preferences.
Banner/interstitial IDs and Firebase configuration are unchanged.

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
RN string preferences can still fail before Home starts, and RN application-locale
storage can supersede `lng`; both remain #22. Native onboarding order (settings,
overlay, privacy) is retained pending a decision versus RN's privacy-first order.
Dialog/process restoration, full settings/tools migration and current-target
permission/service behavior remain #21/#24/#27 work.

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
