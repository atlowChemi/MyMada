# Call popup Compose content (#24)

`smsRes` retains its installed activity identity, Intent payload, alert policy,
player/volume cleanup, timeout and explicit source PendingIntent action.
`calls/CallPopupScreen` owns the Compose layout and logo rendering.

Native text leaves preserve the bundled font, scrolling address, code spans and
administrative blink/color timing. Native buttons preserve the installed
background, capitalization and physical-pixel drag-cancellation thresholds.
No whole-layout XML inflation is used. The floating overlay remains separate.

`CallRouteTest` covers cold/warm payloads, recreation, source action and failed
audio cleanup. `CallPopupComposeTest` checks loop-volume restoration on close
after recreation. `MetronomeCallVisualTest` accepts `screen=call` for matched
EN/HE, portrait/landscape captures with externally selected font scale.

Native button gravity centers text. Popup-local inset painting
and light system icons retain production's red bars under enforced edge-to-edge;
the shared Tools window is not modified. Native text uses the same target29
fallback-spacing distinction as the contraction history.

Private validation: final API24 routes 14/14; API37 covers all 14 distinct routes
with the permission-granted control pair passing 2/2 after one assumption skip.
Both devices pass 16/16 visual fixtures. Final optimized release on both APIs
passes actual-source auto-open, four locale/font payload/details actions and
loop-close player cleanup/volume restoration. Installed bytes match SHA256
`fdfc697667e345d9f85c1bbd53573906eda642c1888c030871e57b08cabbd62a`.
Normal-font Return glyph bounds match production exactly; settled popup icons
are white on red. Release, both lints and 18 JVM tests pass.
Both APIs also pass the original 45-second recovery recipe: first-grant baseline,
fresh/updated revision recovery, original source action, already-handled
suppression, expired/withdrawn/header/future/zero rejection and unfiltered live
delivery of an old-timestamp event. Service/listener/recovery code is unchanged.

Evidence: `~/.local/share/opencode/evidence/metronome-call-compose/`, with
`candidate-d352562a/` screenshots and `WORKLOG.md` provenance. Normal-font
portrait comparisons use actual 12822; forced popup landscape is stress coverage
because production locks this activity to portrait. The user explicitly approved
preserving Android font scaling rather than production's API24 suppression.
Readability, button reachability, clipping and RTL remain validation requirements;
font policy is no longer an acceptance blocker. Publication still requires
coordinated integration and its combined checks.
