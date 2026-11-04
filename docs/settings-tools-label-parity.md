# Tools Settings label metrics

Row27's small downward offset came from native TextView measurement differences:
the migrated14sp header measured45px instead of42px, and21sp labels69px instead
of61px on API37/420dpi/font1.0. Disabling the target35 locale minimum alone did
not fix it; that failed experiment is retained.

`NativeSettingsLabel` now uses the same target29 BoringLayout/fallback-spacing
distinction as the accepted Settings text leaves, and rounds sp dimensions to
pixels as XML inflation does. Wrapped/RTL fallback spacing and system font scale
remain active. Row dimensions, switches, semantics and selection rules are intact.

API37 `ToolSettingsTest.bilingualSelectionLastToolGuardAndRecreation` passes with
the measured61px English-label assertion, EN/HE toggles, last-tool guard, unknown
tool preservation and recreation checks. Final debug/test assembly and both lints
pass. Opened matched EN top/disabled-Contractions and bottom pairs show aligned
labels, switches and dividers across all nine options.

Evidence: private `settings-measured-parity/native-alerts-final/`,
`pair-5942-en-tools-top-disabled-contractions.png`, `pair-5942-en-tools-bottom.png`
and `test-5942-ToolSettingsTest.log`. No additional baseline matrix was repeated.
