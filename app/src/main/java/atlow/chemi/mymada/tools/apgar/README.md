# APGAR migration (#23)

`fragments.ApgarCalc` remains the Tools destination and restored Fragment class.
Compose owns layout, scrolling, score animation and saveable five-choice state.
Native spinner/text interop retains the shipped dropdowns, Open Sans Hebrew font,
text metrics and accessibility. Existing localized strings, info icon and host
window artwork are reused. No settings, navigation or backend contracts change.
Portrait keeps the pinned result/info cards; large text scrolls the whole screen
so the choices cannot be squeezed out. Landscape keeps the native 64/36 split.

## Scoring/source comparison

Native `b36f4a9c` and RN `be72a163` both assign each resource option index 0/1/2
to appearance, pulse, grimace, activity and respiration. Sum range is 0–10;
red 0–3, yellow 4–6, green 7–10. Native colors are #bf1516/#ddbe42/#46b23a;
RN changes only the red to #c11516. This slice retains native colors and the
one-second transition. Pure calculation rejects malformed arrays/out-of-range
choices rather than returning a plausible medical score.

Unresolved existing wording (unchanged, needs maintainer/clinical decision):
English says “above 7” while both calculations and Hebrew include 7 as green;
the English pulse choices say “Less than 100”/“More than 100”, leaving exactly
100 unstated. These are not new scoring rules. No clinical correctness claim is
made by verifying migration parity.

## Checks

Use README's JDK17/SDK setup and disposable-emulator IPv4/IPv6 firewall **before
launch**. Run on an explicitly selected owned serial (no personal devices):

```sh
sh ./gradlew :app:assembleDebug :app:assembleDebugAndroidTest --no-daemon
adb -s "$SERIAL" install -r app/build/outputs/apk/debug/app-debug.apk
adb -s "$SERIAL" install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb -s "$SERIAL" shell am instrument -w -r \
  -e class atlow.chemi.mymada.tools.apgar.ApgarScoreTest,atlow.chemi.mymada.tools.apgar.ApgarScreenTest \
  atlow.chemi.mymada.debug.test/androidx.test.runner.AndroidJUnitRunner
```

The pure JUnit scoring check uses the existing instrumentation dependency (no
new Gradle dependency): all 243 inputs, score distribution, boundaries, colors
and invalid inputs. UI checks use actual localized popup choices, score updates,
Activity recreation, restored selections/title, drawer Back and system Back in
English/Hebrew. `-e capture true` writes actual screenshots to debug app files.
Repeat with system font scale 1.5 for scroll reachability. Android Studio previews
cover English, Hebrew, large text and landscape.

Integration depends on #22's selected-tool/locale migration. This independent
one-layer stack is based on the published `m2/contraction-state` tip; do not merge
ahead of its prerequisites. #23 covers other tools too and remains open. Runtime
and screenshot evidence belongs to this slice's draft PR; maintainer visual
approval remains required (#15/#35).
