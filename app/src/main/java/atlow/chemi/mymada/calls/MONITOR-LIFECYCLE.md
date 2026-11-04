# Monitor lifecycle at target33

The installed SMSMonitorService and BootCompleteReceiver names now resolve to
Kotlin. The monitor retains notification78, runInBg and grpOthers. Repeated starts
reuse the same foreground notification. Notification Stop clears the desired
detection preference and stops this service, fixing the old accidental MetroService
stop. Metronome actions remain owned by #25.

Every monitor start checks the current notification grant and Mapp; services never
request UI permission. Disabled/revoked sticky recreation stops with NOT_STICKY.
Application startup registers the call receiver before requesting listener rebind.
Restricted background starts are logged/deferred rather than crashing; the
OS-bound listener remains responsible for detection. Boot and this package's own
replacement are handled; unrelated installs/restarts cannot start monitoring.

MonitorLifecycleTest covers actual repeated start/foreground notification/Stop
cleanup and receiver opt-in/action dispatch on disposable emulators. Receiver
invocation is not a real reboot test. These tests pregrant notification permission;
the separate NotificationPermissionTest certifies the unpregranted UI routes.

The target remains33. Selecting specialUse with Play-reviewed justification versus
using only an OS-bound listener and ordinary control notification remains the
explicit maintainer decision in #24 before target37 acceptance. No mediaPlayback
type is asserted for this idle monitor. Overlay, production-signed upgrades and
complete process-death/reboot acceptance remain separate work.
