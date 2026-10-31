# Enabling detection needs both grants

The retained Enable fragment chains POST_NOTIFICATIONS and notification-listener
access. Mapp/monitor start only after both actual OS checks succeed. Canceling the
listener explanation or returning without access leaves detection off. The
continuation and Activity Result registration survive recreation; the listener
explanation uses the existing localized prompt style. API30+ opens the exact native
component's Settings page; older platforms use notification-access settings.
An already-granted RN component satisfies the check without asking again.

Monitor startup also checks actual component access. An application-scoped observer
of the OS listener-grant setting handles revocation even before a listener binds;
disconnect/destruction provides the same cleanup. With no remaining native/RN grant,
detection is cleared and the monitor stopped. A transient
disconnect with a retained grant requests rebind and does not clear user intent.
No overlay-access gate was introduced.

NotificationPermissionTest separates unpregranted notification UX from the listener
Settings test. The latter seeds POST_NOTIFICATIONS explicitly, then grants and
revokes listener access using actual Android Settings UI. It must start without a
listener grant. The former now verifies that canceling listener access after
granting notifications does not turn detection on.
