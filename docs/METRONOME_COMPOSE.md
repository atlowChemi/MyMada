# Metronome Compose content (#25)

`MetroActivity` hosts `tools/metronome/MetronomeScreen` in a lifecycle-disposed
`ComposeView`. The service remains the playback/timestamp owner. Start extras,
notification permission results, binding and the 500ms refresh retain their
existing routes.

Compose owns the full-screen button, square layout, two-minute ring and
650ms/350ms pulse. Native text leaves retain installed font padding, hour spans
and locale fallback. The pulse runs only while the fragment is started.

`MetronomeControlTest`, `MetronomeNotificationTest` and
`MetronomePermissionTest` exercise the Compose accessibility action rather than
the removed XML root. They do not depend on the Tools drawer/toolbar views.
`MetronomeCallVisualTest` captures the same fixtures against retained XML and
Compose APKs; arguments are `screen=metro`, `language=en|he` and
`landscape=true|false`. Font scale is set on the disposable emulator.

The notification/widget test waits for both playback and its notification:
`startForeground` publishes before `running` becomes true. Waiting only for the
notification let Stop pass prematurely and left a widget Start for the next
test. The corrected API37 control pair passes; no service change was needed.

Private validation: final API24 routes 14/14; API37 covers all 14 distinct routes
(13 passes/one permission assumption skip, followed by the granted control pair
2/2). Both devices pass 16/16 visual fixtures. Retained actual-source summary and
OS notification grant/deny evidence remains applicable. Rotation captures disable system
window/transition animations; Activity draw barriers alone do not settle the
rotation compositor. Application pulse animation remains enabled.

The fixture also waits for the API37 starting window to disappear: its temporary
dark system icons can outlive the Activity's first draws. Native text preserves
target29 fallback spacing; final English heading bounds match actual production,
and clock glyph bounds differ by at most one physical pixel from layout rounding.

Evidence: `~/.local/share/opencode/evidence/metronome-call-compose/WORKLOG.md`.
Final optimized release, both lints (zero errors) and 18 JVM tests pass. Both
installed release APKs match SHA256
`fdfc697667e345d9f85c1bbd53573906eda642c1888c030871e57b08cabbd62a`.
Release Start/background/rotation/Stop preserves the start timestamp and cleans
up playback. Production comparisons retain matched heading and <=1px clock
bounds. Acceptance still requires combined shared-shell integration checks,
including light status icons on API37. The user
explicitly approved preserving Android font scaling rather than production's
API24 suppression. Readability, control reachability, clipping and RTL remain
validation requirements; font policy is no longer an acceptance blocker.
