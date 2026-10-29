# דיווחי מד"א - MyMada

## Native Android development

This branch restores the native source from `b36f4a9c` (`v3.5.7`) for
incremental Kotlin/Compose modernization. See the [project](https://github.com/users/atlowChemi/projects/1)
and [baseline assessment](https://github.com/atlowChemi/MyMada/issues/19).

### Build and run locally

Requires JDK 17, Android SDK platform 33, build-tools 30.0.3 and platform-tools.
Use the checked-in Gradle wrapper (Gradle 8.0 / Android Gradle Plugin 8.0.2).
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
sh ./gradlew :app:testDebugUnitTest :app:lintDebug --no-daemon
```

The restored baseline has no executable unit tests (`NO-SOURCE`). Lint currently
fails with 17 errors and 445 warnings; its report is under `app/build/reports/`.
API 33 baseline smoke covers home/settings/tools navigation and the oxygen
calculator. Starting the metronome currently crashes on a missing PendingIntent
mutability flag (#25); RN string preferences can crash startup (#22). This is a
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
