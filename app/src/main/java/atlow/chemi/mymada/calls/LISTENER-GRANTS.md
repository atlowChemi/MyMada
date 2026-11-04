# Installed listener identities

The native `.NotificationService` and RN v4
`.recieversAndServices.NotificationService` remain declared with the OS binding
permission. Both execute the same Kotlin listener; no JS runtime is required.
Grant checks compare full component names and re-read secure OS state. Rebinding
never disables/re-enables components (which can invalidate existing grants).

When both are granted, native is selected deterministically. The other component
does not route calls; repeated events from the selected component are preserved,
including identical keys/text. No time-window or content deduplication is used.
Listener disconnect requests rebind only for a currently granted identity.

The original RN declaration excluded silent/ongoing sources; this bridge uses
native filtering for both identities pending the existing classification policy
decision in #24. User-controlled OS notification-access/filter settings remain OS
state. An OS or OEM that removes a grant still requires user re-enablement in
notification-access settings; the application does not grant itself access.

`ListenerAccessTest` exercises native-only, RN-only, both, unrelated-component and
revoked grants using the emulator's secure setting and restores the original
value. This is component/grant verification, separate from fresh unpregranted
POST_NOTIFICATIONS UI tests. Production-signed in-place upgrade and real reboot
acceptance are not implied by these checks.
