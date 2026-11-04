# Combined host accessibility regression (#26)

Application source: `c3e19ef4`; coordinator's immutable
`final-ui/artifacts/app-debug.apk` remained installed throughout diagnosis.
Pulled installed APKs on owned 5980/5982 match those exact bytes. Only the test
APK changed; no application or release build was requested for this correction.

The original host test passed alone (API24 8.346s, API37 9.494s), but failed on
API24 after the ten activation/chat cases. Diagnostics at the failing assertion
showed `rootInActiveWindow == null`, service flags 65554, activity window focus
true and logical page 0. The screenshot showed the form with its keyboard.
After enabling interactive-window retrieval, the active, focused application
window (ID 3920 in the recorded run) exposed real visible EditText nodes, e.g.
`Rect(42,411 - 1038,558)`; the active-root lookup then returned 3920 as well.
This was a sequence-dependent active-window lookup failure, not zero-sized form
nodes, an inset/layout regression, or the earlier keyboard-targeted swipe issue.

The fixture now explicitly retrieves the unique active, focused application
window, verifies the target package and applies the unchanged visible/nonempty
EditText bounds assertion to that root. Accessibility service flags are restored
in finally. Real directional swipes, selected-tab checks and recreation checks
remain unchanged. No retry, sleep, timeout increase or bounds tolerance was added.

Using the frozen application and corrected test APK, the original ordered
17-case sequence passed on API24 EN36.306s / HE35.568s and API37 EN46.933s /
HE45.048s. Evidence lives at
`~/.local/share/opencode/evidence/integrations/combined-host-accessibility/`:
`diagnostic-sequence-5980-en.*`, `window-diagnostic-5980-en.txt`,
`fixed-sequence-*.log`, `fixed-test.apk`, and `verified-artifacts.txt`.
Original coordinator failures and private feature commits remain untouched.
