# Listener-only experiment (#24)

Android binds the existing native/RN NotificationListenerService identities. The
idle SMSMonitorService no longer starts a foreground service, schedules work, or
keeps the app foreground. Its manifest identity remains as a non-sticky retiring
shim for installed explicit intents; boot, update, Application and enable paths
refresh ordinary notification78 instead. Listener connection also refreshes it.
Channel runInBg, Home tap and runInBG Stop PendingIntent identities are preserved.

Stop clears Mapp and cancels only status78. OS listener grants remain intact; source
callbacks are ignored by the existing parser while disabled. Enable still requires
both actual grants, requests rebind, and restores status. Revoking the last listener
grant clears detection/status; granting access alone does not silently opt back in.
Notification denial hides status; a permitted explicit enable remains the recovery
path. The status is not proof of uninterrupted OS binding or delivery.

An ordinary ongoing notification is not a keep-alive mechanism, and newer Android
versions may let the user dismiss it. This experiment does not claim equivalent OEM
battery behavior or guaranteed delivery during process death. Compare device logs
against #67/#73 foreground-monitor kill/reboot evidence. Physical/OEM battery and
production-signature upgrade acceptance remain required; target33 emulator results
do not establish target37 enforcement on the coordinated common candidate.

## Isolated target33 evidence

Debug/test/optimized release, JVM5/5 and both lints passed. Final routes, grant and
ordinary-controls suite10/10 passed on API24 and API37 (16KB). Actual API37 Settings
grant/revoke and notification-grant/listener-cancel each passed1/1. Same-signer
install-r retained exact listener settings on both devices.

SIGKILL from a verified OS-bound state recovered both processes in approximately
two seconds; screen-off source delivery followed. Initial API24 setup after harness
force-stop was unbound and did not recover within30s; retained evidence is not a
force-stop pass. Reapplying grants in the harness established the valid kill trial.
Real kernel reboots on both APIs restored ordinary78 and the OS listener without
opening UI. Two identical screen-off source callbacks produced distinct calls5001
and5002. Forced-idle source delivery produced5003 on both APIs. No idle FGS appeared
in service dumps. These checks use an offline, shell-triggered source fixture, not
real push arrival or physical-device battery behavior.

Raw evidence: evidence/calls/listener-only-*. The retained #73 comparison uses
monitor-kill-{pids,before,after}.txt and reboot37-services-settled.txt: that version
restored foreground78 after BOOT_COMPLETED, while this version restores ordinary78.
