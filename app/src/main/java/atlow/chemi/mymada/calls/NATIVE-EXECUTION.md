# Native call execution

Applicatio and GrobootRec retain their installed identities and now execute Kotlin.
Application registers the process-local receiver before listener rebind. Only the
manifest boot receiver handles OS boot/update; a LocalBroadcastManager registration
cannot receive system broadcasts and was removed.

Receiver payload, source PendingIntent and decision variables are per delivery.
The unused static test flag/source slot and its last caller are gone. Home test-call
dialogs already use the real event route and keep their per-type filtering. Calls
create their existing notification channel before background delivery instead of
depending on Home having run. Replacement clears only group Calls, preserving
metronome/background controls. New IDs advance past active Calls notifications
after process restart rather than overwriting them.

Removed NotificationServiceOld: its manifest enabled flag was false from API19
onward (`values-v19/listenerO=false`); the supported minimum is24. Its accessibility
fallback could not run on supported devices. Both actual native and RN notification
listener component identities remain declared; their OS grants are untouched. No
active accessibility grant is migrated or manufactured. A vendor OS lacking the
notification-access settings activity needs separate compatibility assessment;
starting the disabled pre19 implementation was not a functioning fallback.

CallRouteTest starts without the call channel, exercises actual Application
registration/listener callbacks and verifies call replacement retains an unrelated
control notification. This is not production-signed/reboot/latest-target evidence.
Shared ActionReceiver is now Kotlin, preserving the metronome action contracts
owned by #25 and the runInBG detection-stop behavior fixed in #67.
