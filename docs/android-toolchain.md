# Android toolchain and dependency reference

Issue #14, based on restored native commit `ce77419d`, native source `b36f4a9c`
and RN snapshot `be72a163`. Versions and upstream guidance checked 2026-10-29.

## Selected versions and holds

| Component | Selection | Reason / upgrade gate |
| --- | --- | --- |
| Build JVM | JDK 17 (validated with Temurin 17.0.21+9) | AGP requirement; Java/Kotlin bytecode remains 1.8 |
| Gradle / AGP | 8.13 / 8.13.2 | Supports compile SDK 36 and Kotlin 2.3; 16 KB packaging support. AGP 9's built-in Kotlin/new DSL migration is a separate toolchain step |
| Kotlin | 2.3.21 | Within the published Gradle/AGP compatibility range; Android Extensions removed |
| Compile SDK / build tools | 36 / AGP default 35.0.0 | Compile-time SDK upgrade does not opt into target-SDK behavior changes |
| Target SDK | **33, temporary development hold** | #24/#25 must address notifications, foreground services, receivers, edge-to-edge and current-platform testing before moving to 36 |
| Minimum SDK | **21, unchanged** | Android 5+ native users remain eligible; RN required 23 but does not establish that API 21/22 native users disappeared. No Play device-population evidence authorizes dropping them |
| Compose (for #21) | Compiler plugin 2.3.21; UI/runtime 1.8.3 | Compiler plugin must match Kotlin. API-21-compatible UI line; later Compose raises minSdk to 23. No Compose runtime dependency or feature UI is added before its first use |
| NDK | Not required to build this source | No C/C++ source or externalNativeBuild. Transitive native binaries still require artifact inspection (below). If native code is added, use r28+ for default 16 KB alignment |

This is a compatible modernization step, **not a claim to use every latest
release**. Latest Firebase BoM 35.1.0 requires API 24; Mobile Ads 25.5.0 also
requires API 24. Revisit minimum OS with real Play installed-device evidence
and an explicit maintainer decision. Do not use `tools:overrideLibrary` to
bypass library minimums.

## Native dependency inventory

| Restored declaration | Disposition |
| --- | --- |
| Kotlin stdlib-jdk7 1.6.21; Android Extensions | Kotlin plugin supplies stdlib; sole synthetic consumer (`InfoActivityKt`) uses view binding |
| Firebase core 17.2.1; analytics 17.2.1 **and** 17.5.0 | Remove core/duplicate analytics; BoM 33.16.0 aligns the retained main modules |
| Messaging 20.1.0 | BoM-managed messaging; replace Instance ID token API with `FirebaseMessaging.getToken()`, retain `Settings/fcm` and token-refresh callback |
| Database 19.2.0; Firestore 21.3.1 | BoM-managed; existing database paths/queries and Firebase project unchanged |
| In-App Messaging Display 19.0.2; Crashlytics 17.2.1 | BoM-managed; retain automatic SDK behavior even without explicit source calls |
| Functions 19.0.1 | Remove: no functions API use; remove unused OkHttp field that depended on it transitively |
| Firebase Ads 18.3.0 | Replace with Mobile Ads 23.6.0, last major line supporting API 21. Migrate both interstitial callers to load/full-screen callbacks and banner initialization to manifest app ID; keep ad-unit IDs and navigation destinations |
| Play Core 1.6.4 | Feature Delivery 2.1.0 retains language downloads/SplitCompat. Remove obsolete MissingSplits startup API; modern Play installs enforce required splits |
| AppCompat 1.1.0 | 1.7.1 |
| legacy-support-v4 1.0.0 | Remove umbrella; explicitly retain LocalBroadcastManager 1.1.0 used by receivers/services |
| Material 1.0.0 | 1.12.0, API-21-compatible line. Remove private `TextView.mHint` reflection; Material implements the Meizu workaround. Keep XML class name as a constructor-only subclass |
| Preference 1.1.0 | 1.2.1, retained for `PreferenceThemeOverlay.v14.Material` in styles.xml |
| CardView 1.0.0 | Retain: XML and source references |
| Gson 2.8.5 | 2.13.2, retains serialized models/preferences |
| MultiDex 2.0.1 | Remove support library/install hook: ART natively supports multidex from API 21 |
| ConstraintLayout 1.1.3 | 2.2.1 |
| materialdatetimepicker 4.2.1 | Retain: settings time-picker callback API; avoid unrelated picker behavior changes |
| RecyclerView 1.1.0 | 1.3.2, API-21-compatible stable line |
| Bundletool buildscript 0.10.3 | Remove unused direct plugin classpath; AGP supplies its own packaging tools |
| Google Services 4.3.15; Crashlytics plugin 2.5.2 | 4.4.4 / 3.0.6; compatible stable tooling. Unsigned local release mapping upload disabled |
| JCenter, Maven Local, duplicated Google Maven | Google + Maven Central only; no machine-local dependency resolution or Jetifier |

Firebase BoM 33.16.0 is an intentional API-21 hold (34 raises relevant modules
to 23; 35 to 24). Ads 23.x is deprecated, with upstream sunset June 30, 2027;
it is not a long-term supported monetization solution. Ads 18.x was already
sunset. Backend/ad delivery and consent integration still require live testing
before release; local verification does not send test messages to production.

## RN-era inventory and disposition

The historical Android build used AGP 8.2.2, Gradle-era Kotlin 1.8.0, SDK 34,
minimum 23, React Native 0.73.5 / React 18.2, Node 18+, Yarn 3.6.2,
Google Services 4.4.1, Crashlytics plugin 2.9.9, Firebase Performance plugin
1.4.2 and RN Firebase 19.0.1 modules (app, analytics, crashlytics, database,
firestore, messaging, perf, storage).

Restoration already removed the RN application/toolchain. None is reintroduced:

- React Gradle plugin, autolinking, Metro/Babel/TypeScript, Hermes/JSC,
  Flipper, Fresco WebP, native RN bridges and Yarn patches.
- WorkManager 2.9.0, desugar_jdk_libs 2.0.4, android-retrofuture 1.7.4,
  Guava 27.0.1-jre: historical direct declarations, no restored native callers.
- RN Google Mobile Ads 13.0.2: replaced by direct Android Ads dependency.
- Navigation/drawer/native-stack/top-tabs, portal, slider, dialog/dropdown,
  gesture-handler, pager/tab-view, reanimated, safe-area/screens, gradients,
  SVG, PDF/WebView bridges: no restored Android bridge consumers.
- MobX, inversify/reflection metadata, i18next, color and associated JS
  tooling: no native runtime role. Native resources/locales remain authoritative.
- RN performance/storage Firebase modules had no equivalent restored native
  callers; not added speculatively. Native In-App Messaging remains intentional.

## Android / Play technical gates

- The current official target policy requires **API 36 for phone app updates**
  from August 31, 2026 (extension through November 1 where granted). Target 33
  **cannot be presented as submission-ready**. #9 handles listing diagnosis;
  #24/#25 handle behavior remediation/testing. Compile 36 alone is insufficient.
- Native notifications still have PendingIntent/notification-permission defects;
  no lint baseline or suppression is added to conceal them. Metronome-start
  acceptance remains blocked by #25, not attributed to this tooling upgrade.
- Minimum 21 remains a compatibility promise requiring oldest-device testing;
  successful manifest merging is not an API-21 runtime test.
- Java/Kotlin app code is page-size independent, but Firebase's transitive
  DataStore includes `libdatastore_shared_counter.so`. Inspect every final APK
  and AAB, not just app source. AGP >=8.5.1 supplies correct ZIP packaging.
  ELF LOAD alignment and ZIP alignment checks are necessary; an actual 16 KB
  device smoke remains a separate release gate. Official guidance currently
  states enforcement for applicable Play updates from February 1, 2027.
- Release signing continuity/versionCode verification remain #11. No production
  signing keys are required for local release-mode packaging. Existing broad
  ProGuard keep rules are inherited; packaging success does not prove shrinking
  effectiveness. No new blanket warning suppression is permitted.

## Local commands

Set `JAVA_HOME` to JDK 17 and `ANDROID_HOME` to an isolated SDK installation.
Install `platforms;android-36`, `build-tools;35.0.0`, `platform-tools` with
`sdkmanager --sdk_root="$ANDROID_HOME"` (or Android Studio).

```sh
sh ./gradlew clean :app:assembleDebug :app:assembleRelease :app:bundleRelease --no-daemon
sh ./gradlew :app:testDebugUnitTest :app:lintDebug --no-daemon
sh ./gradlew :app:dependencies --configuration releaseRuntimeClasspath
python3 scripts/check-native-alignment.py app/build/outputs/apk/debug/app-debug.apk app/build/outputs/apk/release/app-release-unsigned.apk app/build/outputs/bundle/release/app-release.aab
"$ANDROID_HOME/build-tools/35.0.0/zipalign" -c -P 16 4 app/build/outputs/apk/debug/app-debug.apk
"$ANDROID_HOME/build-tools/35.0.0/zipalign" -c -P 16 4 app/build/outputs/apk/release/app-release-unsigned.apk
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -W -n atlow.chemi.mymada.debug/atlow.chemi.mymada.HomePage
```

Run lint independently and record its failures. Do not disable release lint to
make `assembleRelease` green. Any explicitly excluded lint task in an exploratory
packaging command must be reported as **packaging-only**, not a passing release
build. Offline navigation smoke: home → info → privacy; home → contact (no-ad
fallback); settings/locales; tools → oxygen calculator; metronome start (known
#25 failure). Use a disposable emulator with Wi-Fi/mobile data disabled.

## Executed validation (2026-10-29)

| Check | Result |
| --- | --- |
| Clean build: `clean :app:assembleDebug :app:assembleRelease :app:bundleRelease :app:lintDebug :app:testDebugUnitTest --continue --no-daemon` | Debug APK built successfully. Overall command **FAILED**: release lint and debug lint reject 8 missing PendingIntent mutability flags |
| `:app:testDebugUnitTest` | **NO-SOURCE**, not passing unit-test coverage |
| `:app:lintDebug` | **8 errors, 421 warnings, 1 hint**; no baseline, severity downgrade or disabled checks |
| `:app:assembleRelease :app:bundleRelease -x :app:lintVitalRelease --no-daemon` | **Packaging-only PASS**, unsigned APK/AAB generated with release-mode R8/resource shrinking; explicitly **not** a passing normal release build |
| `scripts/check-native-alignment.py` on debug APK, release APK and AAB | PASS: each has 2 64-bit `libdatastore_shared_counter.so` binaries (arm64-v8a / x86_64); every ELF LOAD segment aligned to 16384 bytes |
| SDK 35 `zipalign -c -P 16 4` on both APKs | PASS |
| API 33 arm64 emulator, offline, install and cold home launch | PASS (`am start -W`: Status ok, 1427 ms) |
| Home → info → privacy | PASS; view binding renders version text, no-ad fallback reaches PrivacyActivity |
| Home → contact | PASS; no-ad fallback retains volunteer gate and reaches contact name dialog; no message sent |
| Tools/metronome screen | Opens; **start reproduces baseline crash** in `MetroService.java:47` (missing PendingIntent flags), #25 remains open |

The new Material lint identified eight ImageView tint issues; these now use
`app:tint`. Suspicious indentation was corrected without changing control flow.
Private hint reflection and vulnerable monolithic Play Core were removed.
Firebase's merged manifest now supplies `POST_NOTIFICATIONS`, which removes the
old missing-declaration lint findings, **but does not implement runtime permission
requests**. #24/#25 must still implement/test notification permission behavior.
The eight fatal findings span GrobootRec (2), MetroService, MetroWidget,
MyFirebaseMessagingService, SMSMonitorService, and homeBAdapter (2). Audit all
11 PendingIntent creation sites, including the three with UPDATE_CURRENT that
lint does not flag; there is no inline-reply/RemoteInput usage in this source.

Warning triage: 150 nonconstant resource-ID switch warnings (the existing
`android.nonFinalResIds=false` is retained), 111 unused resources, 26 obsolete
SDK checks, and smaller groups for hardcoded text/RTL, accessibility labels,
orientation locks, deprecated APIs, scoped storage and stale dependencies.
Do not delete resources or change user-facing behavior solely to reduce the
count. #21/#23 cover UI/accessibility; #24/#25 platform/service behavior;
#27 covers device/integration parity. Lint's lower count is not runtime parity.

**Open acceptance gates:** normal clean release build; metronome start/stop;
API 21 and current-Android runtime matrix; real 16 KB device/emulator smoke;
online Firebase/Ads and language-split delivery; release signing/upgrade proof.
No current-target, loaded-ad dismissal/failure, or Play-delivered split test is
claimed. Keep #14 open until its build gates are resolved; downstream feature
and release gates remain tracked in their owning issues.

## Official references

- [AGP 8.13 compatibility](https://developer.android.com/build/releases/agp-8-13-0-release-notes)
- [Kotlin/Gradle/AGP matrix](https://kotlinlang.org/docs/gradle-configure-project.html)
- [AGP 9 migration](https://developer.android.com/build/releases/agp-9-0-0-release-notes)
- [Compose compiler setup](https://developer.android.com/develop/ui/compose/compiler)
- [Compose UI releases / minimum OS](https://developer.android.com/jetpack/androidx/releases/compose-ui)
- [Firebase Android releases / BoM](https://firebase.google.com/support/release-notes/android)
- [Ads API migration / minimum OS](https://developers.google.com/admob/android/migration)
- [Ads deprecation schedule](https://developers.google.com/admob/android/deprecation)
- [Play Core migration](https://developer.android.com/guide/playcore#playcore-migration)
- [Play target requirements](https://developer.android.com/google/play/requirements/target-sdk)
- [16 KB checks](https://developer.android.com/guide/practices/page-sizes)
- [Material 1.12 Meizu implementation](https://github.com/material-components/material-components-android/blob/1.12.0/lib/java/com/google/android/material/textfield/TextInputEditText.java)
