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
configuration. For offline smoke tests, disable emulator Wi-Fi/mobile data before
launch; integration tests require appropriate backend configuration.

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
