# Settings volume slider theme

The published12822 slider is inflated from `volume_dialog.xml` with the Settings
activity context before the AlertDialog is built. Its discrete style changes the
tick drawable, not its activity control colors. The APK resource table resolves
activity `colorControlNormal` to `ColorBlue` (`#98194853`); the alert theme instead
uses `Ocean` (`#ff3586b5`). Using the alert theme made the migrated inactive track
and ticks visibly blue despite matching geometry.

The Compose-hosted AppCompat slider uses the activity context with a parentless
overlay that supplies only `Widget.AppCompat.SeekBar.Discrete`. No color value is
copied into code. The shared alert theme and other dialog callers are unaffected.

`AdvancedSettingsTest#volumeSliderRetainsActivityColorsAndDiscreteStyle` compares
the hosted slider's control attributes against the retained activity-inflated
native layout, checks the discrete style/progress, and checks that Cancel discards
the override draft. The focused capture uses override OFF/progress2, matching the
API37 published reference. This is separate from the already accepted volume
geometry and draft-summary correction.

Local verification (2026-11-01): debug/application-test APK assembly and both
debug/release lints passed; the focused instrumentation check passed on API24
and API37. No full Settings/Delay/Mode matrix was repeated.
